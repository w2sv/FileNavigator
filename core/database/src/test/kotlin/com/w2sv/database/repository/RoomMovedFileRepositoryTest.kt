package com.w2sv.database.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.w2sv.common.util.DocumentUri
import com.w2sv.common.util.MediaUri
import com.w2sv.database.AppDatabase
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.movedestination.ExternalDestination
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.domain.repository.MovedFileRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class RoomMovedFileRepositoryTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var db: AppDatabase
    private lateinit var repository: MovedFileRepository

    @Before
    fun createDb() {
        db = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()
        repository = RoomMovedFileRepository(db.getMovedFileDao())
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertionAndRetrieval() = testScope.runTest {
        val movedFiles = listOf(
            MovedFile.Local(
                documentUri = DocumentUri.parse("kjhasdfkjh"),
                mediaUri = MediaUri.parse("kjasdf"),
                name = "someFile.jpg",
                originalName = null,
                type = FileType.Image,
                sourceType = SourceType.Screenshot,
                moveDestination = LocalDestination.parse("kjhasdfkjh"),
                moveDateTime = LocalDateTime.now(),
                autoMoved = false
            ),
            MovedFile.External(
                moveDestination = ExternalDestination(
                    documentUri = DocumentUri.parse("kjhasdfkjh"),
                    providerPackageName = "provider.package.name",
                    providerAppLabel = "Drive"
                ),
                name = "someFile.jpg",
                originalName = "previousName.jpg",
                type = FileType.Image,
                sourceType = SourceType.Screenshot,
                moveDateTime = LocalDateTime.now()
            ),
            MovedFile.External(
                moveDestination = ExternalDestination(
                    documentUri = DocumentUri.parse("kjhasdfkjh"),
                    providerPackageName = null,
                    providerAppLabel = null
                ),
                name = "someFile.jpg",
                originalName = null,
                type = FileType.Image,
                sourceType = SourceType.Screenshot,
                moveDateTime = LocalDateTime.now()
            )
        )

        with(repository) {
            movedFiles.forEach {
                insert(it)
            }

            assertEquals(
                movedFiles.reversed(),
                getAllInDescendingOrder().first()
            )
        }
    }
}