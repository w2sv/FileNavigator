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
        val config = NavigatorConfig.default.run {
            copy(
                fileTypeConfigMap = fileTypeConfigMap.copy {
                    PresetFileType.Media.values.forEach { mediaFileType ->
                        update(mediaFileType) { fileTypeConfig ->
                            fileTypeConfig.copy(enabled = false)
                        }
                    }
                }
            )
        }
        assertEquals(PresetFileType.NonMedia.values.toSet(), config.enabledFileTypes)
        assertEquals(PresetFileType.Media.values.toSet(), config.disabledFileTypes)
    }

    @Test
    fun testUpdateFileTypeConfig() {
        val config =
            NavigatorConfig.default.updateFileTypeConfig(PresetFileType.Image) { fileTypeConfig ->
                fileTypeConfig.copy(
                    enabled = false,
                    sourceTypeConfigMap = fileTypeConfig.sourceTypeConfigMap.copy {
                        update(SourceType.Screenshot) {
                            it.copy(
                                enabled = false,
                                quickMoveDestinations = listOf(LocalDestination.parse("path/to/destination")),
                                autoMoveConfig = AutoMoveConfig(
                                    enabled = true,
                                    destination = LocalDestination.parse("path/to/auto/move/dest")
                                )
                            )
                        }
                        update(SourceType.Camera) {
                            it.copy(enabled = false)
                        }
                    }
                )
            }

        val expected = NavigatorConfig(
            fileTypeConfigMap = mapOf(
                PresetFileType.Image to FileTypeConfig(
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
                PresetFileType.Video to FileTypeConfig(
                    enabled = true,
                    sourceTypeConfigMap = mapOf(
                        SourceType.Camera to SourceConfig(),
                        SourceType.OtherApp to SourceConfig(),
                        SourceType.Download to SourceConfig()
                    )
                ),
                PresetFileType.Audio to FileTypeConfig(
                    enabled = true,
                    sourceTypeConfigMap = mapOf(
                        SourceType.Recording to SourceConfig(),
                        SourceType.OtherApp to SourceConfig(),
                        SourceType.Download to SourceConfig()
                    )
                ),
                PresetFileType.PDF to nonMediaFileTypeConfig(),
                PresetFileType.Text to nonMediaFileTypeConfig(),
                PresetFileType.Archive to nonMediaFileTypeConfig(),
                PresetFileType.APK to nonMediaFileTypeConfig(),
                PresetFileType.EBook to nonMediaFileTypeConfig()
            ),
            extensionConfigurableFileTypeToExcludedExtensions = emptyMap(),
            showBatchMoveNotification = true,
            disableOnLowBattery = false,
            startOnBoot = false
        )

        assertEquals(
            expected,
            config
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
