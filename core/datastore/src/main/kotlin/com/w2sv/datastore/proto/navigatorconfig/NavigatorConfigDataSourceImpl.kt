package com.w2sv.datastore.proto.navigatorconfig

import androidx.datastore.core.DataStore
import com.w2sv.common.utils.DocumentUri
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
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
        fileType: FileType,
        sourceType: SourceType,
        destination: DocumentUri
    ) {
        navigatorConfigProtoDataStore.updateData { configProto ->
            NavigatorConfigMapper.toProto(
                NavigatorConfigMapper
                    .toExternal(configProto)
                    .copyWithAlteredSourceConfig(
                        fileType,
                        sourceType
                    ) {
                        it.copy(lastMoveDestinations = listOf(destination))
                    }
            )
        }
    }

    override fun lastMoveDestination(
        fileType: FileType,
        sourceType: SourceType
    ): Flow<List<DocumentUri>> =
        navigatorConfig.map { it.sourceConfig(fileType, sourceType).lastMoveDestinations }
}



