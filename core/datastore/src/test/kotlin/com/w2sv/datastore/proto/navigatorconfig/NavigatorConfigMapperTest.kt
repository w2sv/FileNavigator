package com.w2sv.datastore.proto.navigatorconfig

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
        val nonDefaultConfig = NavigatorConfig.default
            .copy(disableOnLowBattery = true, startOnBoot = true)
            .updateFileTypeConfig(PresetFileType.Video) {
                it.copy(enabled = false)
            }
            .updateAutoMoveConfig(PresetFileType.APK, SourceType.Download) {
                AutoMoveConfig(
                    enabled = true,
                    destination = LocalDestination.parse("some/move/destination")
                )
            }
            .updateAutoMoveConfig(PresetFileType.Image, SourceType.Download) {
                AutoMoveConfig(
                    enabled = true,
                    destination = LocalDestination.parse("some/other/move/destination")
                )
            }
            .updateSourceConfig(PresetFileType.Audio, SourceType.Recording) {
                it.copy(
                    enabled = false,
                    quickMoveDestinations = listOf(
                        LocalDestination.parse("last/move/destination"),
                        LocalDestination.parse("before/last/move/destination")
                    )
                )
            }
        assertEquals(nonDefaultConfig, nonDefaultConfig.backAndForthMapped())
    }
}

private fun NavigatorConfig.backAndForthMapped(): NavigatorConfig =
    toProto(true).toExternal()
