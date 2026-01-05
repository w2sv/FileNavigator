package com.w2sv.database.migration

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.anggrayudi.storage.file.child
import com.w2sv.androidutils.database.getStringOrThrow
import com.w2sv.common.logging.log
import slimber.log.i

private const val PRE_VERSION_5_TABLE_NAME = "MoveEntryEntity"
private const val VERSION_5_TABLE_NAME = "MovedFileEntity"

internal object Migrations {
    class Migration2to3(private val context: Context) : Migration(2, 3) {

        override fun migrate(db: SupportSQLiteDatabase) {
            i { "Running migration 2 to 3" }

            // Add columns to table
            db.addNonNullStringColumn("movedFileDocumentUri")
            db.addNonNullStringColumn("movedFileMediaUri")

            // Attempt to dynamically migrate each row
            db.beginTransaction()
            try {
                val cursor = db.query("SELECT * FROM $PRE_VERSION_5_TABLE_NAME")

                cursor.use {
                    while (it.moveToNext()) {
                        migrateRow(it, db)
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }

        private fun migrateRow(cursor: Cursor, db: SupportSQLiteDatabase) {
            val fileName =
                cursor.getStringOrThrow("fileName")
            val destinationDocumentUri =
                cursor.getStringOrThrow("destinationDocumentUri").toUri()

            val documentFile = try {
                DocumentFile
                    .fromSingleUri(context, destinationDocumentUri)
                    ?.child(
                        context = context,
                        path = fileName,
                        requiresWriteAccess = false
                    )
            } catch (e: SecurityException) {
                null
            }

            if (documentFile == null) {
                i { "Couldn't find moved file - aborting row update" }
            } else {
                db.update(
                    table = PRE_VERSION_5_TABLE_NAME,
                    conflictAlgorithm = SQLiteDatabase.CONFLICT_ABORT,
                    values = ContentValues().apply {
                        put(
                            "movedFileDocumentUri",
                            documentFile.uri.toString()
                        )
                        put(
                            "movedFileMediaUri",
                            MediaStore.getMediaUri(
                                context,
                                documentFile.uri
                            )
                                .toString()
                        )
                    },
                    whereClause = "dateTime = ?",
                    whereArgs = arrayOf(
                        cursor.getStringOrThrow("dateTime")
                    )
                )
                    .log { "Updated $it row(s)" }
            }
        }
    }

    val Migration3to4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            with(db) {
                addNonNullIntColumn("autoMoved")
                renameColumn("fileSourceKind", "sourceType")
            }
        }
    }

    val Migration4to5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            with(db) {
                // Create the new table MovedFileEntity with the new schema
                execSQL(
                    """CREATE TABLE IF NOT EXISTS $VERSION_5_TABLE_NAME (
                    documentUri TEXT NOT NULL,
                    name TEXT NOT NULL,
                    originalName TEXT DEFAULT NULL,
                    type TEXT NOT NULL,
                    sourceType TEXT NOT NULL,
                    moveDateTime TEXT NOT NULL,
                    autoMoved INTEGER NOT NULL,
                    local_mediaUri TEXT,
                    local_moveDestination TEXT,
                    external_providerPackageName TEXT,
                    external_providerAppLabel TEXT,
                    PRIMARY KEY(moveDateTime))"""
                        .trimIndent()
                )

                // Copy the data from the old table (MoveEntryEntity) to the new one (MovedFileEntity)
                execSQL(
                    """INSERT INTO $VERSION_5_TABLE_NAME
                        (documentUri, name, type, sourceType, moveDateTime, autoMoved, local_mediaUri, local_moveDestination)
                        SELECT
                            movedFileDocumentUri AS documentUri,
                            fileName AS name,
                            fileType AS type,
                            sourceType AS sourceType,
                            dateTime AS moveDateTime,
                            autoMoved AS autoMoved,
                            movedFileMediaUri AS local_mediaUri,
                            destinationDocumentUri AS local_moveDestination
                        FROM MoveEntryEntity"""
                        .trimIndent()
                )

                // Delete the old table MoveEntryEntity
                execSQL("DROP TABLE IF EXISTS $PRE_VERSION_5_TABLE_NAME")
            }
        }
    }
}

private fun SupportSQLiteDatabase.renameColumn(from: String, to: String, tableName: String = PRE_VERSION_5_TABLE_NAME) {
    execSQL("ALTER TABLE $tableName RENAME COLUMN $from TO $to")
}

private fun SupportSQLiteDatabase.addNonNullStringColumn(
    name: String,
    tableName: String = PRE_VERSION_5_TABLE_NAME,
    default: String = "''"
) {
    execSQL("ALTER TABLE $tableName ADD COLUMN $name TEXT NOT NULL DEFAULT $default")
}

private fun SupportSQLiteDatabase.addNonNullIntColumn(name: String, default: Int = 0, tableName: String = PRE_VERSION_5_TABLE_NAME) {
    execSQL("ALTER TABLE $tableName ADD COLUMN $name INTEGER NOT NULL DEFAULT $default")
}
