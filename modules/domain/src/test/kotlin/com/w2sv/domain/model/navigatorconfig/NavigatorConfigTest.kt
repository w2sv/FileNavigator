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
}
