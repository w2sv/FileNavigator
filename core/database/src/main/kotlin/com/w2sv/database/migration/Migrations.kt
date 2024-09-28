package com.w2sv.database.migration

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.anggrayudi.storage.file.child
import com.w2sv.androidutils.database.getStringOrThrow
import com.w2sv.common.utils.log
import slimber.log.i

private const val TABLE_NAME = "MoveEntryEntity"

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
                val cursor = db.query("SELECT * FROM $TABLE_NAME")

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
                Uri.parse(cursor.getStringOrThrow("destinationDocumentUri"))

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
                    table = TABLE_NAME,
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
                renameColumn("destinationDocumentUri", "local_destination")
                renameColumn("movedFileDocumentUri", "local_movedFileDocumentUri")
                renameColumn("movedFileMediaUri", "local_movedFileMediaUri")
                addNonNullStringColumn("external_destination")
                addNullableStringColumn("external_providerPackageName")
                addNullableStringColumn("external_providerAppLabel")
            }
        }
    }
}

private fun SupportSQLiteDatabase.renameColumn(from: String, to: String) {
    execSQL("ALTER TABLE $TABLE_NAME RENAME COLUMN $from TO $to")
}

private fun SupportSQLiteDatabase.addNonNullStringColumn(name: String) {
    execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $name TEXT NOT NULL DEFAULT ''")
}

private fun SupportSQLiteDatabase.addNullableStringColumn(name: String) {
    execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $name TEXT DEFAULT NULL")
}

private fun SupportSQLiteDatabase.addNonNullIntColumn(name: String, default: Int = 0) {
    execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $name INTEGER NOT NULL DEFAULT $default")
}