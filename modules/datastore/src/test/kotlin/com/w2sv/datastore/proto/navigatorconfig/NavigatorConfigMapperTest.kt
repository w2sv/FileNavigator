package com.w2sv.datastore.proto.navigatorconfig

import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.domain.model.filetype.FileType
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
            .updateFileTypeConfig(PresetFileType.Video.toFileType()) {
                it.copy(enabled = false)
            }
            .updateAutoMoveConfig(PresetFileType.APK.toFileType(), SourceType.Download) {
                AutoMoveConfig(
                    enabled = true,
                    destination = LocalDestination.parse("some/move/destination")
                )
            }
            .updateAutoMoveConfig(PresetFileType.Image.toFileType(), SourceType.Download) {
                AutoMoveConfig(
                    enabled = true,
                    destination = LocalDestination.parse("some/other/move/destination")
                )
            }
            .updateSourceConfig(PresetFileType.Audio.toFileType(), SourceType.Recording) {
                it.copy(
                    enabled = false,
                    quickMoveDestinations = listOf(
                        LocalDestination.parse("last/move/destination"),
                        LocalDestination.parse("before/last/move/destination")
                    )
                )
            }
            .addCustomFileType(FileType.custom("Html", listOf("html"), 124145, 1009), enabled = true)
            .addCustomFileType(FileType.custom("Json", listOf("json"), 235236, 1003), enabled = false)
            .editFileType(PresetFileType.Image.toFileType()) { it.withColor(92357) }
            .editFileType(PresetFileType.EBook.toFileType()) {
                (it as FileType.ConfigurablePreset)
                    .withColor(237598)
                    .withExcludedExtensions(setOf("epub", "azw", "azw1", "azw2", "azw3", "mobi", "iba", "rtf", "tpz", "mart"))
            }
        assertEquals(nonDefaultConfig, nonDefaultConfig.backAndForthMapped())
    }

    @Test
    fun `file type config persists via existing ordinal keyed proto fields`() {
        val customFileType = FileType.custom("Html", listOf("html"), 124145, 1009)
        val config = NavigatorConfig
            .default
            .addCustomFileType(customFileType, enabled = true)
            .editFileType(PresetFileType.Image.toFileType()) { it.withColor(92357) }
            .editFileType(PresetFileType.EBook.toFileType()) {
                (it as FileType.ConfigurablePreset)
                    .withColor(237598)
                    .withExcludedExtensions(setOf("epub", "azw"))
            }

        val proto = config.toProto(hasBeenMigrated = true)

        assertEquals(92357, proto.extensionPresetFileTypesMap.getValue(PresetFileType.Image.ordinal).color)
        assertEquals(237598, proto.extensionConfigurableFileTypesMap.getValue(PresetFileType.EBook.ordinal).color)
        assertEquals(
            setOf("epub", "azw"),
            proto.extensionConfigurableFileTypesMap.getValue(PresetFileType.EBook.ordinal).excludedExtensionsList.toSet()
        )
        assertEquals(customFileType.name, proto.customFileTypesMap.getValue(customFileType.ordinal).name)
        assertEquals(config, proto.toExternal())
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
