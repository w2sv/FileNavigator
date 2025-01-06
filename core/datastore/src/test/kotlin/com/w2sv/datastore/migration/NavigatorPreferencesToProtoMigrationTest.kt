package com.w2sv.datastore.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import com.w2sv.datastore.navigatorConfigProto
import com.w2sv.datastore.proto.navigatorconfig.toExternal
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

internal class NavigatorPreferencesToProtoMigrationTest {

    @Mock
    private lateinit var mockDataStore: DataStore<Preferences>

    private lateinit var migration: NavigatorPreferencesToProtoMigration

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        migration = NavigatorPreferencesToProtoMigration(mockDataStore)
    }

    @Test
    fun `shouldMigrate returns true if hasBeenMigrated is false`() =
        runBlocking {
            val currentData = navigatorConfigProto {
                hasBeenMigrated = false
            }

            assertTrue(migration.shouldMigrate(currentData))
        }

    @Test
    fun `shouldMigrate returns false if hasBeenMigrated is true`() =
        runBlocking {
            val currentData = navigatorConfigProto {
                hasBeenMigrated = true
            }

            assertFalse(migration.shouldMigrate(currentData))
        }

    @Test
    fun `migrate performs migration`() =
        runBlocking {
            val currentData = navigatorConfigProto {
                hasBeenMigrated = false
            }

            val preferences =
                preferencesOf(
                    PreMigrationNavigatorPreferencesKey.disableOnLowBattery to true,

                    PreMigrationNavigatorPreferencesKey.fileTypeEnabled(FileType.Image) to false,
                    PreMigrationNavigatorPreferencesKey.fileTypeEnabled(FileType.Video) to false,
                    PreMigrationNavigatorPreferencesKey.fileTypeEnabled(FileType.PDF) to false,

                    PreMigrationNavigatorPreferencesKey.sourceTypeEnabled(
                        FileType.Audio,
                        SourceType.Recording
                    ) to false
                )

            Mockito.`when`(mockDataStore.data).thenReturn(flowOf(preferences))

            val migratedNavigatorConfigProto = migration.migrate(currentData)

            assertTrue(migratedNavigatorConfigProto.hasBeenMigrated)
            assertEquals(
                migratedNavigatorConfigProto.toExternal(),
                NavigatorConfig
                    .default
                    .copy(disableOnLowBattery = true)
                    .copyWithAlteredFileTypeConfig(FileType.Image) {
                        it.copy(enabled = false)
                    }
                    .copyWithAlteredFileTypeConfig(FileType.Video) {
                        it.copy(enabled = false)
                    }
                    .copyWithAlteredFileTypeConfig(FileType.PDF) {
                        it.copy(enabled = false)
                    }
                    .copyWithAlteredSourceConfig(FileType.Audio, SourceType.Recording) {
                        it.copy(enabled = false)
                    }
            )
        }
}
