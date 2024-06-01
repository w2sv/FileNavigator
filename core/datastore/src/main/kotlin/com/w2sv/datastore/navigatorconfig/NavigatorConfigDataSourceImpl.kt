package com.w2sv.datastore.navigatorconfig

import android.net.Uri
import androidx.datastore.core.DataStore
import com.w2sv.common.utils.reset
import com.w2sv.datastore.AutoMoveConfigProto
import com.w2sv.datastore.FileTypeProto
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.autoMoveConfigProto
import com.w2sv.datastore.fileTypeProto
import com.w2sv.datastore.navigatorConfigProto
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigatorConfigDataSourceImpl @Inject constructor(private val navigatorConfigProtoDataStore: DataStore<NavigatorConfigProto>) :
    NavigatorConfigDataSource {

    override val navigatorConfig: Flow<NavigatorConfig> =
        navigatorConfigProtoDataStore.data.map { it.toExternal() }

    override suspend fun saveNavigatorConfig(config: NavigatorConfig) {
        navigatorConfigProtoDataStore.updateData {
            config.toProto()
        }
    }
}

private fun NavigatorConfig.toProto(): NavigatorConfigProto =
    navigatorConfigProto {
        fileTypes.apply {
            clear()
            addAll(this@toProto.fileTypes.map { it.toProto() })
        }
        disableOnLowBattery = this@toProto.disableOnLowBattery
    }

private fun NavigatorConfigProto.toExternal(): NavigatorConfig =
    NavigatorConfig(
        fileTypes = fileTypesList.map { it.toExternal() },
        disableOnLowBattery = disableOnLowBattery
    )

private fun FileType.toProto(): FileTypeProto =
    fileTypeProto {
        kind = this@toProto.kind.toProto()
        enabled = this@toProto.enabled
        sources.apply {
            clear()
            addAll(this@toProto.sources.map { it.toProto() })
        }
        autoMoveConfig = this@toProto.autoMoveConfig.toProto()
    }

private fun FileTypeProto.toExternal(): FileType =
    FileType(
        kind = kind.toExternal(),
        enabled = enabled,
        sources = sourcesList.map { it.toExternal() },
        autoMoveConfig = autoMoveConfig.toExternal()
    )

private fun FileType.Kind.toProto(): FileTypeProto.Kind = when (this) {
    FileType.Kind.Image -> FileTypeProto.Kind.Image
    FileType.Kind.Video -> FileTypeProto.Kind.Video
    FileType.Kind.Audio -> FileTypeProto.Kind.Audio
    FileType.Kind.PDF -> FileTypeProto.Kind.PDF
    FileType.Kind.Text -> FileTypeProto.Kind.Text
    FileType.Kind.Archive -> FileTypeProto.Kind.Archive
    FileType.Kind.APK -> FileTypeProto.Kind.APK
}

private fun FileTypeProto.Kind.toExternal(): FileType.Kind = when (this) {
    FileTypeProto.Kind.Image -> FileType.Kind.Image
    FileTypeProto.Kind.Video -> FileType.Kind.Video
    FileTypeProto.Kind.Audio -> FileType.Kind.Audio
    FileTypeProto.Kind.PDF -> FileType.Kind.PDF
    FileTypeProto.Kind.Text -> FileType.Kind.Text
    FileTypeProto.Kind.Archive -> FileType.Kind.Archive
    FileTypeProto.Kind.APK -> FileType.Kind.APK
    FileTypeProto.Kind.UNRECOGNIZED -> throw IllegalArgumentException("Unrecognized FileTypeProto.Kind")
}

private fun FileType.Source.toProto(): FileTypeProto.Source =
    FileTypeProto.Source.newBuilder()
        .apply {
            this.kind = this@toProto.kind.toProto()
            this.enabled = this@toProto.enabled
            this.lastMoveDestinationsList.reset(this@toProto.lastMoveDestinations.map { it.toString() })
            this.autoMoveConfig = this@toProto.autoMoveConfig.toProto()
        }
        .build()

private fun FileTypeProto.Source.toExternal(): FileType.Source =
    FileType.Source(
        kind = kind.toExternal(),
        enabled = enabled,
        lastMoveDestinations = lastMoveDestinationsList.map { Uri.parse(it) },
        autoMoveConfig = autoMoveConfig.toExternal()
    )

private fun FileType.Source.Kind.toProto(): FileTypeProto.Source.Kind = when (this) {
    FileType.Source.Kind.Camera -> FileTypeProto.Source.Kind.Camera
    FileType.Source.Kind.Screenshot -> FileTypeProto.Source.Kind.Screenshot
    FileType.Source.Kind.Recording -> FileTypeProto.Source.Kind.Recording
    FileType.Source.Kind.Download -> FileTypeProto.Source.Kind.Download
    FileType.Source.Kind.OtherApp -> FileTypeProto.Source.Kind.OtherApp
}

private fun FileTypeProto.Source.Kind.toExternal(): FileType.Source.Kind = when (this) {
    FileTypeProto.Source.Kind.Camera -> FileType.Source.Kind.Camera
    FileTypeProto.Source.Kind.Screenshot -> FileType.Source.Kind.Screenshot
    FileTypeProto.Source.Kind.Recording -> FileType.Source.Kind.Recording
    FileTypeProto.Source.Kind.Download -> FileType.Source.Kind.Download
    FileTypeProto.Source.Kind.OtherApp -> FileType.Source.Kind.OtherApp
    FileTypeProto.Source.Kind.UNRECOGNIZED -> throw IllegalArgumentException("Unrecognized FileTypeProto.Source.Kind")
}

private fun AutoMoveConfig.toProto(): AutoMoveConfigProto =
    autoMoveConfigProto {
        this.enabled = this@toProto.enabled
        this.destination = this@toProto.destination?.toString() ?: ""
    }

private fun AutoMoveConfigProto.toExternal(): AutoMoveConfig =
    AutoMoveConfig(
        enabled = enabled,
        destination = if (destination.isNotEmpty()) {
            Uri.parse(destination)
        } else {
            null
        }
    )