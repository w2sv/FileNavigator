package com.w2sv.datastore.proto.navigatorconfig

import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.model.SourceType
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
            .copyWithAlteredFileTypeConfig(FileType.Video) {
                it.copy(enabled = false)
            }
            .copyWithAlteredAutoMoveConfig(FileType.APK, SourceType.Download) {
                AutoMoveConfig(
                    enabled = true,
                    destination = MoveDestination.Directory.parse("some/move/destination")
                )
            }
            .copyWithAlteredAutoMoveConfig(FileType.Image, SourceType.Download) {
                AutoMoveConfig(
                    enabled = true,
                    destination = MoveDestination.Directory.parse("some/other/move/destination")
                )
            }
            .copyWithAlteredSourceConfig(FileType.Audio, SourceType.Recording) {
                it.copy(
                    enabled = false,
                    lastMoveDestinations = listOf(MoveDestination.Directory.parse("last/move/destination"))
                )
            }
        assertEquals(nonDefaultConfig, nonDefaultConfig.backAndForthMapped())
    }
}

private fun NavigatorConfig.backAndForthMapped(): NavigatorConfig =
    toProto(true).toExternal()
