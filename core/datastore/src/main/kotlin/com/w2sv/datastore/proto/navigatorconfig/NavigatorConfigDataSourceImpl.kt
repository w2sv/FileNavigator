package com.w2sv.datastore.proto.navigatorconfig

import android.net.Uri
import androidx.datastore.core.DataStore
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.copy
import com.w2sv.domain.model.FileAndSourceType
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
        navigatorConfigProtoDataStore.data.map { NavigatorConfigMapper.toExternal(it) }

    override suspend fun saveNavigatorConfig(config: NavigatorConfig) {
        navigatorConfigProtoDataStore.updateData {
            NavigatorConfigMapper.toProto(config)
        }
    }

    override suspend fun saveLastMoveDestination(
        fileAndSourceType: FileAndSourceType,
        destination: Uri
    ) {
        navigatorConfigProtoDataStore.updateData {
            it.copy {}  // TODO
        }
    }

    override fun lastMoveDestinations(fileAndSourceType: FileAndSourceType): Flow<List<Uri>> =
        navigatorConfig.map {
            it
                .fileTypeConfigs.find { it.fileType == fileAndSourceType.fileType }!!
                .sourceConfigs.find { it.type == fileAndSourceType.sourceType }!!
                .lastMoveDestinations
        }
}



