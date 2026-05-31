package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.kotlinutils.copy
import com.w2sv.kotlinutils.update
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class NavigatorConfigTest {

    @Test
    fun `enabledFileTypes and disabledFileTypes`() {
        val config = PresetFileType.mediaEntries.fold(NavigatorConfig.default) { acc, mediaFileType ->
            acc.updateFileTypeConfig(mediaFileType.toFileType()) { fileTypeConfig ->
                fileTypeConfig.copy(enabled = false)
            }
        }
        assertEquals(PresetFileType.downloadEntries.toSet(), config.enabledFileTypes.map { it.presetTypeOrNull }.toSet())
        assertEquals(PresetFileType.mediaEntries.toSet(), config.disabledFileTypes.map { it.presetTypeOrNull }.toSet())
    }

    @Test
    fun testUpdateFileTypeConfig() {
        val updatedConfig = NavigatorConfig.default.updateFileTypeConfig(PresetFileType.Image.toFileType()) { fileTypeConfig ->
            fileTypeConfig.copy(
                enabled = false,
                sourceTypeConfigMap = fileTypeConfig.sourceTypeConfigMap.copy {
                    update(SourceType.Screenshot) { sourceConfig ->
                        sourceConfig.copy(
                            enabled = false,
                            quickMoveDestinations = listOf(LocalDestination.parse("path/to/destination")),
                            autoMoveConfig = AutoMoveConfig(
                                enabled = true,
                                destination = LocalDestination.parse("path/to/auto/move/dest")
                            )
                        )
                    }
                    update(SourceType.Camera) { sourceConfig ->
                        sourceConfig.copy(enabled = false)
                    }
                }
            )
        }

        val expected = NavigatorConfig(
            fileTypeConfigMap = mapOf(
                PresetFileType.Image.toFileType().id to FileTypeConfig(
                    fileType = PresetFileType.Image.toFileType(),
                    enabled = false,
                    sourceTypeConfigMap = mapOf(
                        SourceType.Camera to SourceConfig(
                            enabled = false,
                            quickMoveDestinations = emptyList(),
                            autoMoveConfig = AutoMoveConfig.Empty
                        ),
                        SourceType.Screenshot to SourceConfig(
                            enabled = false,
                            quickMoveDestinations = listOf(LocalDestination.parse("path/to/destination")),
                            autoMoveConfig = AutoMoveConfig(
                                enabled = true,
                                destination = LocalDestination.parse("path/to/auto/move/dest")
                            )
                        ),
                        SourceType.OtherApp to SourceConfig(),
                        SourceType.Download to SourceConfig()
                    )
                ),
                PresetFileType.Video.toFileType().let { it.id to it.defaultConfig() },
                PresetFileType.Audio.toFileType().let { it.id to it.defaultConfig() },
                PresetFileType.PDF.toFileType().let { it.id to it.defaultConfig() },
                PresetFileType.Text.toFileType().let { it.id to it.defaultConfig() },
                PresetFileType.Archive.toFileType().let { it.id to it.defaultConfig() },
                PresetFileType.APK.toFileType().let { it.id to it.defaultConfig() },
                PresetFileType.EBook.toFileType().let { it.id to it.defaultConfig() }
            ),
            showBatchMoveNotification = true,
            disableOnLowBattery = false,
            startOnBoot = false
        )

        assertEquals(
            expected,
            updatedConfig
        )
    }

    @Test
    fun testUpdatedQuickMoveDestinations() {
        val localDestinationA =
            LocalDestination.parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FGIFs")
        val localDestinationB =
            LocalDestination.parse("content://com.android.externalstorage.documents/document/primary%3AMoved%2FScreenshots")
        val localDestinationC =
            LocalDestination.parse("content://com.android.externalstorage.documents/document/primary%3AMoved")

        // When current destinations empty -> destination is added
        assertEquals(
            listOf(localDestinationA),
            updatedQuickMoveDestinations(
                currentDestinations = emptyList(),
                destination = localDestinationA
            )
        )

        // When new destination equals first destination -> list stays as is
        assertEquals(
            listOf(localDestinationA),
            updatedQuickMoveDestinations(
                currentDestinations = listOf(localDestinationA),
                destination = localDestinationA
            )
        )

        // When new destination equals first destination -> list stays as is
        assertEquals(
            listOf(localDestinationA, localDestinationB),
            updatedQuickMoveDestinations(
                currentDestinations = listOf(localDestinationA, localDestinationB),
                destination = localDestinationA
            )
        )

        // When new destination equals second destination -> list gets reversed
        assertEquals(
            listOf(localDestinationB, localDestinationA),
            updatedQuickMoveDestinations(
                currentDestinations = listOf(localDestinationA, localDestinationB),
                destination = localDestinationB
            )
        )

        // When new destination not in list -> second is removed, first becomes second, new becomes first
        assertEquals(
            listOf(localDestinationC, localDestinationA),
            updatedQuickMoveDestinations(
                currentDestinations = listOf(localDestinationA, localDestinationB),
                destination = localDestinationC
            )
        )

        // When new destination not in list -> first becomes second, new becomes first
        assertEquals(
            listOf(localDestinationB, localDestinationA),
            updatedQuickMoveDestinations(
                currentDestinations = listOf(localDestinationA),
                destination = localDestinationB
            )
        )
    }
}
