package com.w2sv.datastore.proto.navigatorconfig

import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class NavigatorConfigMapperTest {

    @Test
    fun testDefaultMapping() {
        assertEquals(NavigatorConfig.default, NavigatorConfig.default.backAndForthMapped())
    }

    @Test
    fun testNonDefaultMapping() {
        val nonDefaultConfig = NavigatorConfig
            .default
            .copy(disableOnLowBattery = true, startOnBoot = true)
            .updateFileTypeConfig(PresetFileType.Video.toDefaultFileType()) {
                it.copy(enabled = false)
            }
            .updateAutoMoveConfig(PresetFileType.APK.toDefaultFileType(), SourceType.Download) {
                AutoMoveConfig(
                    enabled = true,
                    destination = LocalDestination.parse("some/move/destination")
                )
            }
            .updateAutoMoveConfig(PresetFileType.Image.toDefaultFileType(), SourceType.Download) {
                AutoMoveConfig(
                    enabled = true,
                    destination = LocalDestination.parse("some/other/move/destination")
                )
            }
            .updateSourceConfig(PresetFileType.Audio.toDefaultFileType(), SourceType.Recording) {
                it.copy(
                    enabled = false,
                    quickMoveDestinations = listOf(
                        LocalDestination.parse("last/move/destination"),
                        LocalDestination.parse("before/last/move/destination")
                    )
                )
            }
            .addCustomFileType(CustomFileType("Html", listOf("html"), 124145, 1009), enabled = true)
            .addCustomFileType(CustomFileType("Json", listOf("json"), 235236, 1003), enabled = false)
            .editFileType(PresetFileType.Image.toFileType()) { it.copy(colorInt = 92357) }
            .editFileType(PresetFileType.EBook.toFileType()) {
                it.copy(
                    colorInt = 237598,
                    excludedExtensions = setOf("epub", "azw", "azw1", "azw2", "azw3", "mobi", "iba", "rtf", "tpz", "mart")
                )
            }
        assertEquals(nonDefaultConfig, nonDefaultConfig.backAndForthMapped())
    }

    /**
     * Empty file type maps occur when updating app from 0.2.5 to 0.3.0 due to inherent navigator proto config changes.
     */
    @Test
    fun `default preset file types are used when proto file type maps are empty`() {
        val protoConfigWithEmptyFileTypeMaps = NavigatorConfigProto.newBuilder()
            .mergeFrom(NavigatorConfig.default.toProto(true)) // clone from default
            .clearExtensionPresetFileTypes()
            .clearExtensionConfigurableFileTypes()
            .clearCustomFileTypes()
            .build()

        assertEquals(NavigatorConfig.default, protoConfigWithEmptyFileTypeMaps.toExternal())
    }
}

private fun NavigatorConfig.backAndForthMapped(): NavigatorConfig =
    toProto(true).toExternal()
