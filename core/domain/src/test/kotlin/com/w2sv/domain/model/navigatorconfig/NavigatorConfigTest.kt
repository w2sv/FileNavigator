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
        val config = PresetFileType.Media.values.fold(NavigatorConfig.default) { acc, mediaFileType ->
            acc.updateFileTypeConfig(mediaFileType.toDefaultFileType()) { fileTypeConfig ->
                fileTypeConfig.copy(enabled = false)
            }
        }
        assertEquals(PresetFileType.NonMedia.values.toSet(), config.enabledFileTypes.map { it.wrappedPresetTypeOrNull }.toSet())
        assertEquals(PresetFileType.Media.values.toSet(), config.disabledFileTypes.map { it.wrappedPresetTypeOrNull }.toSet())
    }

    @Test
    fun testUpdateFileTypeConfig() {
        val updatedConfig = NavigatorConfig.default.updateFileTypeConfig(PresetFileType.Image.toDefaultFileType()) { fileTypeConfig ->
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
                PresetFileType.Image.toDefaultFileType() to FileTypeConfig(
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
                PresetFileType.Video.toDefaultFileType() to PresetFileType.Video.defaultConfig(),
                PresetFileType.Audio.toDefaultFileType() to PresetFileType.Audio.defaultConfig(),
                PresetFileType.PDF.toDefaultFileType() to nonMediaFileTypeConfig(),
                PresetFileType.Text.toDefaultFileType() to nonMediaFileTypeConfig(),
                PresetFileType.Archive.toDefaultFileType() to nonMediaFileTypeConfig(),
                PresetFileType.APK.toDefaultFileType() to nonMediaFileTypeConfig(),
                PresetFileType.EBook.toDefaultFileType() to nonMediaFileTypeConfig()
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

private fun nonMediaFileTypeConfig(enabled: Boolean = true): FileTypeConfig =
    FileTypeConfig(
        enabled = enabled,
        sourceTypeConfigMap = mapOf(
            SourceType.Download to SourceConfig()
        )
    )
