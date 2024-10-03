package com.w2sv.domain.model.navigatorconfig

import com.w2sv.common.util.copy
import com.w2sv.common.util.update
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import org.junit.Assert.assertEquals
import org.junit.Test

internal class NavigatorConfigTest {

    @Test
    fun testEnabledDisabledFileTypes() {
        val config = NavigatorConfig.default.run {
            copy(
                fileTypeConfigMap = fileTypeConfigMap.copy {
                    update(FileType.Image) {
                        it.copy(enabled = false)
                    }
                    update(FileType.Video) {
                        it.copy(enabled = false)
                    }
                    update(FileType.Audio) {
                        it.copy(enabled = false)
                    }
                }
            )
        }
        assertEquals(FileType.NonMedia.values, config.enabledFileTypes)
        assertEquals(FileType.Media.values, config.disabledFileTypes)
    }

    @Test
    fun testCopyWithAlteredFileTypeConfig() {
        val config = NavigatorConfig.default.copyWithAlteredFileTypeConfig(FileType.Image) {
            it.copy(
                enabled = false,
                sourceTypeConfigMap = it.sourceTypeConfigMap.copy {
                    update(SourceType.Screenshot) {
                        it.copy(enabled = false)
                    }
                    update(SourceType.Camera) {
                        it.copy(enabled = false)
                    }
                }
            )
        }

        assertEquals(
            "NavigatorConfig(fileTypeConfigMap={Image=FileTypeConfig(enabled=false, sourceTypeConfigMap={Camera=SourceConfig(enabled=false, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null)), Screenshot=SourceConfig(enabled=false, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null)), OtherApp=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null)), Download=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null))}), Video=FileTypeConfig(enabled=true, sourceTypeConfigMap={Camera=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null)), OtherApp=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null)), Download=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null))}), Audio=FileTypeConfig(enabled=true, sourceTypeConfigMap={Recording=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null)), OtherApp=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null)), Download=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null))}), PDF=FileTypeConfig(enabled=true, sourceTypeConfigMap={Download=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null))}), Text=FileTypeConfig(enabled=true, sourceTypeConfigMap={Download=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null))}), Archive=FileTypeConfig(enabled=true, sourceTypeConfigMap={Download=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null))}), APK=FileTypeConfig(enabled=true, sourceTypeConfigMap={Download=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null))}), EBook=FileTypeConfig(enabled=true, sourceTypeConfigMap={Download=SourceConfig(enabled=true, lastMoveDestinations=[], autoMoveConfig=AutoMoveConfig(enabled=false, destination=null))})}, showBatchMoveNotification=true, disableOnLowBattery=false, startOnBoot=false)",
            config.toString()
        )
    }
}