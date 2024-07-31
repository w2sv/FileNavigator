package com.w2sv.datastore.proto.navigatorconfig

import androidx.datastore.core.DataStore
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.MoveDestination
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NavigatorConfigDataSourceImpl @Inject constructor(private val navigatorConfigProtoDataStore: DataStore<NavigatorConfigProto>) :
    NavigatorConfigDataSource {

    override val navigatorConfig: Flow<NavigatorConfig> =
        navigatorConfigProtoDataStore.data.map { NavigatorConfigMapper.toExternal(it) }

    override suspend fun saveNavigatorConfig(config: NavigatorConfig) {
        navigatorConfigProtoDataStore.updateData { protoConfig ->
            NavigatorConfigMapper.toProto(
                external = config,
                hasBeenMigrated = protoConfig.hasBeenMigrated
            )
        }
    }

    /**
     * Convenience method that enables directly modifying a [NavigatorConfig] instance, that has been mapped to from the
     * [NavigatorConfigProto] instance fetched from disk, and is mapped back to [NavigatorConfigProto] after applying the transformation.
     * Also retains [NavigatorConfigProto.hasBeenMigrated_] between the [NavigatorConfigProto] instances.
     *
     * @see DataStore.updateData
     */
    private suspend fun updateData(transform: suspend (NavigatorConfig) -> NavigatorConfig) {
        navigatorConfigProtoDataStore.updateData { protoConfig ->
            NavigatorConfigMapper.toProto(
                external = transform(NavigatorConfigMapper.toExternal(protoConfig)),
                hasBeenMigrated = protoConfig.hasBeenMigrated
            )
        }
    }

    override suspend fun unsetAutoMoveConfig(fileType: FileType, sourceType: SourceType) {
        updateData {
            it.copyWithAlteredSourceAutoMoveConfig(fileType, sourceType) {
                AutoMoveConfig.Empty
            }
        }
    }

    override suspend fun saveQuickMoveDestination(
        fileType: FileType,
        sourceType: SourceType,
        destination: MoveDestination
    ) {
        updateData { config ->
            config.copyWithAlteredSourceConfig(
                fileType,
                sourceType
            ) { sourceConfig ->
                sourceConfig.copy(
                    lastMoveDestinations = sourceConfig.lastMoveDestinations.let { currentDestinations ->
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
            }
        }
    }

    override suspend fun unsetQuickMoveDestination(
        fileType: FileType,
        sourceType: SourceType,
    ) {
        updateData { config ->
            config.copyWithAlteredSourceConfig(
                fileType,
                sourceType
            ) {
                it.copy(lastMoveDestinations = listOf())
            }
        }
    }

    override fun quickMoveDestinations(
        fileType: FileType,
        sourceType: SourceType
    ): Flow<List<MoveDestination>> =
        navigatorConfig.map { it.sourceConfig(fileType, sourceType).lastMoveDestinations }
}



