package com.w2sv.datastore.migration

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import com.w2sv.datastore.navigatorConfigProto
import com.w2sv.datastore.proto.navigatorconfig.toExternal
import com.w2sv.datastore.proto.navigatorconfig.toProto
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.test.TimberTestRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class NavigatorPreferencesToProtoMigrationTest {

    @Mock
    private lateinit var mockDataStore: DataStore<Preferences>

    private lateinit var migration: NavigatorPreferencesToProtoMigration

    @get:Rule
    val timberRule = TimberTestRule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        migration = NavigatorPreferencesToProtoMigration(mockDataStore)
    }

    @Test
    fun `shouldMigrate returns the inverse of hasBeenMigrated`() =
        runBlocking {
            suspend fun test(hasBeenMigrated: Boolean) {
                assertEquals(
                    !hasBeenMigrated,
                    migration.shouldMigrate(
                        navigatorConfigProto {
                            this.hasBeenMigrated = hasBeenMigrated
                        }
                    )
                )
            }

            test(true)
            test(false)
        }

    @Test
    fun `migrate correctly performs migration`() =
        runBlocking {
            Mockito.`when`(mockDataStore.data).thenReturn(
                flowOf(
                    preferencesOf(
                        PreMigrationNavigatorPreferencesKey.disableOnLowBattery to true,
                        PreMigrationNavigatorPreferencesKey.fileTypeEnabled(PresetFileType.Image) to false,
                        PreMigrationNavigatorPreferencesKey.fileTypeEnabled(PresetFileType.Video) to false,
                        PreMigrationNavigatorPreferencesKey.fileTypeEnabled(PresetFileType.PDF) to false,
                        PreMigrationNavigatorPreferencesKey.sourceTypeEnabled(PresetFileType.Audio, SourceType.Recording) to false,
                        PreMigrationNavigatorPreferencesKey.lastMoveDestination(PresetFileType.Audio, SourceType.Recording)
                            to "someDestination"
                    )
                )
            )

            val migratedProto = migration.migrate(
                navigatorConfigProto {
                    hasBeenMigrated = false
                }
            )

            assertTrue(migratedProto.hasBeenMigrated)
            assertEquals(
                NavigatorConfig
                    .default
                    .copy(disableOnLowBattery = true)
                    .updateFileTypeConfig(PresetFileType.Image.toDefaultFileType()) {
                        it.copy(enabled = false)
                    }
                    .updateFileTypeConfig(PresetFileType.Video.toDefaultFileType()) {
                        it.copy(enabled = false)
                    }
                    .updateFileTypeConfig(PresetFileType.PDF.toDefaultFileType()) {
                        it.copy(enabled = false)
                    }
                    .updateSourceConfig(PresetFileType.Audio.toDefaultFileType(), SourceType.Recording) {
                        it.copy(
                            enabled = false,
                            quickMoveDestinations = listOf(LocalDestination.parse("someDestination"))
                        )
                    },
                migratedProto.toExternal()
            )
        }

    @Test
    fun `input config with hasBeenMigrated set to true gets returned when no navigator config preference keys exist`() =
        runBlocking {
            Mockito.`when`(mockDataStore.data).thenReturn(flowOf(preferencesOf()))

            val migratedProto = migration.migrate(NavigatorConfig.default.toProto(hasBeenMigrated = false))

            assertTrue(migratedProto.hasBeenMigrated)
            assertEquals(NavigatorConfig.default, migratedProto.toExternal())
        }
}
