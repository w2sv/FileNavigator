package com.w2sv.datastorage.database

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

internal object Migrations {
    class Migration2to3(private val context: Context) : Migration(2, 3) {

        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE MoveEntryEntity ADD COLUMN movedFileDocumentUri TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE MoveEntryEntity ADD COLUMN movedFileMediaUri TEXT NOT NULL DEFAULT ''")

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
                cursor.getString("fileName")
            val destinationDocumentUri =
                Uri.parse(cursor.getString("destinationDocumentUri"))

            val documentFile = DocumentFile
                .fromSingleUri(context, destinationDocumentUri)
                ?.child(
                    context = context,
                    path = fileName,
                    requiresWriteAccess = false
                )

            if (documentFile != null) {
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
                        cursor.getString("dateTime")
                    )
                )
            }
        }
    }
}

private fun Cursor.getString(columnName: String): String =
    getString(getColumnIndexOrThrow(columnName))