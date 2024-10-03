package com.w2sv.database.migration

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.anggrayudi.storage.extension.getString
import com.w2sv.database.AppDatabase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val TEST_DB = "migration-test"

@RunWith(AndroidJUnit4::class)
internal class MigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val migrationTestHelper: MigrationTestHelper = MigrationTestHelper(
        instrumentation,
        AppDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        val fileName = "testFile.jpg"
        val fileType = "Image"
        val sourceType = "Screenshot"
        val destinationDocumentUri = "content://destination"
        val movedFileDocumentUri = "content://movedFile"
        val movedFileMediaUri = "content://media"
        val dateTime = "2023-10-01T12:00:00Z"
        val autoMoved = "1"

        // Insert sample data into version 4 schema
        migrationTestHelper
            .createDatabase(TEST_DB, 4)
            .apply {
                execSQL(
                    "INSERT INTO MoveEntryEntity " +
                            "(fileName, fileType, sourceType, destinationDocumentUri, movedFileDocumentUri, movedFileMediaUri, dateTime, autoMoved) " +
                            "VALUES " +
                            "('$fileName', '$fileType', '$sourceType', '$destinationDocumentUri', '$movedFileDocumentUri', '$movedFileMediaUri', '$dateTime', '$autoMoved')"
                )
                close()
            }

        // Perform the migration and validate the schema
        migrationTestHelper.runMigrationsAndValidate(TEST_DB, 5, true, Migrations.Migration4to5)

        // Open the database with version 5 and verify that the data was migrated correctly
        val migratedDb = Room
            .databaseBuilder(
                instrumentation.targetContext,
                AppDatabase::class.java, TEST_DB
            )
            .addMigrations(Migrations.Migration4to5)
            .build()

        // Fetch the migrated data
        val migratedData = migratedDb.query(
            "SELECT * FROM MovedFileEntity WHERE moveDateTime = '$dateTime'",
            null
        )

        // Validate that the data exists and matches the original
        assertTrue(migratedData.moveToFirst())
        assertEquals(fileName, migratedData.getString("name"))
        assertEquals(fileType, migratedData.getString("type"))
        assertEquals(sourceType, migratedData.getString("sourceType"))
        assertEquals(destinationDocumentUri, migratedData.getString("local_moveDestination"))
        assertEquals(movedFileDocumentUri, migratedData.getString("documentUri"))
        assertEquals(movedFileMediaUri, migratedData.getString("local_mediaUri"))
        assertEquals(autoMoved, migratedData.getString("autoMoved"))

        migratedData.close()
        migratedDb.close()
    }
}