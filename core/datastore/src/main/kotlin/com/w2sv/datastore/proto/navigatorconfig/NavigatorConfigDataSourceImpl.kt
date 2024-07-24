package com.w2sv.datastore.proto.navigatorconfig

import androidx.datastore.core.DataStore
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.datastore.copy
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
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

    override suspend fun unsetAutoMoveConfig(fileType: FileType, sourceType: SourceType) {
        navigatorConfigProtoDataStore.updateData { configProto ->
            NavigatorConfigMapper.toExternal(configProto)
                .copyWithAlteredSourceAutoMoveConfig(fileType, sourceType) {
                    AutoMoveConfig.Empty
                }
                .let { external ->
                    NavigatorConfigMapper.toProto(
                        external = external,
                        hasBeenMigrated = configProto.hasBeenMigrated
                    )
                }
        }
    }

    override suspend fun saveNavigatorConfig(config: NavigatorConfig) {
        navigatorConfigProtoDataStore.updateData {
            NavigatorConfigMapper.toProto(external = config, hasBeenMigrated = it.hasBeenMigrated)
        }
    }

    override suspend fun saveQuickMoveDestination(
        fileType: FileType,
        sourceType: SourceType,
        destination: MoveDestination
    ) {
        navigatorConfigProtoDataStore.updateData { configProto ->
            NavigatorConfigMapper.toProto(
                NavigatorConfigMapper
                    .toExternal(configProto)
                    .copyWithAlteredSourceConfig(
                        fileType,
                        sourceType
                    ) {
                        it.copy(
                            lastMoveDestinations = it.lastMoveDestinations.let { currentDestinations ->
                                when (destination) {
                                    currentDestinations.firstOrNull() -> currentDestinations
                                    currentDestinations.getOrNull(1) -> currentDestinations.reversed()
                                    else -> buildList {
                                        add(destination)
                                        currentDestinations.firstOrNull()
                                            ?.let { firstCurrentElement ->
                                                add(firstCurrentElement)
                                            }
                                    }
                                }
                            }
                        )
                    },
                hasBeenMigrated = configProto.hasBeenMigrated
            )
        }
    }

    override suspend fun unsetQuickMoveDestination(
        fileType: FileType,
        sourceType: SourceType,
    ) {
        navigatorConfigProtoDataStore.updateData { configProto ->
            NavigatorConfigMapper.toProto(
                external = NavigatorConfigMapper
                    .toExternal(configProto)
                    .copyWithAlteredSourceConfig(
                        fileType,
                        sourceType
                    ) {
                        it.copy(lastMoveDestinations = listOf())
                    },
                hasBeenMigrated = configProto.hasBeenMigrated
            )
        }
    }

    override fun quickMoveDestinations(
        fileType: FileType,
        sourceType: SourceType
    ): Flow<List<MoveDestination>> =
        navigatorConfig.map { it.sourceConfig(fileType, sourceType).lastMoveDestinations }
}



