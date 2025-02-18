package com.w2sv.domain.model.navigatorconfig

import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
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
                    FileType.Media.values.forEach { mediaFileType ->
                        update(mediaFileType) { fileTypeConfig ->
                            fileTypeConfig.copy(enabled = false)
                        }
                    }
                }
            )
        }
        assertEquals(FileType.NonMedia.values, config.enabledFileTypes)
        assertEquals(FileType.Media.values, config.disabledFileTypes)
    }

    @Test
    fun testCopyWithAlteredFileTypeConfig() {
        val config =
            NavigatorConfig.default.copyWithAlteredFileTypeConfig(FileType.Image) { fileTypeConfig ->
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
                FileType.Image to FileTypeConfig(
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
                FileType.Video to FileTypeConfig(
                    enabled = true,
                    sourceTypeConfigMap = mapOf(
                        SourceType.Camera to SourceConfig(),
                        SourceType.OtherApp to SourceConfig(),
                        SourceType.Download to SourceConfig()
                    )
                ),
                FileType.Audio to FileTypeConfig(
                    enabled = true,
                    sourceTypeConfigMap = mapOf(
                        SourceType.Recording to SourceConfig(),
                        SourceType.OtherApp to SourceConfig(),
                        SourceType.Download to SourceConfig()
                    )
                ),
                FileType.PDF to FileTypeConfig(
                    enabled = true,
                    sourceTypeConfigMap = mapOf(
                        SourceType.Download to SourceConfig()
                    )
                ),
                FileType.Text to FileTypeConfig(
                    enabled = true,
                    sourceTypeConfigMap = mapOf(
                        SourceType.Download to SourceConfig()
                    )
                ),
                FileType.Archive to FileTypeConfig(
                    enabled = true,
                    sourceTypeConfigMap = mapOf(
                        SourceType.Download to SourceConfig()
                    )
                ),
                FileType.APK to FileTypeConfig(
                    enabled = true,
                    sourceTypeConfigMap = mapOf(
                        SourceType.Download to SourceConfig()
                    )
                ),
                FileType.EBook to FileTypeConfig(
                    enabled = true,
                    sourceTypeConfigMap = mapOf(
                        SourceType.Download to SourceConfig()
                    )
                )
            ),
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
