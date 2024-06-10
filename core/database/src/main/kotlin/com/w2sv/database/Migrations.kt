package com.w2sv.database

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
import com.w2sv.common.utils.getStringOrThrow
import slimber.log.i

internal object Migrations {
    class Migration2to3(private val context: Context) : Migration(2, 3) {

        override fun migrate(db: SupportSQLiteDatabase) {
            i { "Running migration" }

            // Add columns beset with default values to table
            db.execSQL("ALTER TABLE MoveEntryEntity ADD COLUMN movedFileDocumentUri TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE MoveEntryEntity ADD COLUMN movedFileMediaUri TEXT NOT NULL DEFAULT ''")

            // Attempt to dynamically migrate each row
            db.beginTransaction()
            try {
                val cursor = db.query("SELECT * FROM MoveEntryEntity")

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
                    table = "MoveEntryEntity",
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
                    .also { i { "Updated $it row(s)" } }
            }
        }
    }

    val Migration3to4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE MoveEntryEntity ADD COLUMN autoMoved INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE MoveEntryEntity RENAME COLUMN fileSourceKind TO sourceType")
        }
    }
}