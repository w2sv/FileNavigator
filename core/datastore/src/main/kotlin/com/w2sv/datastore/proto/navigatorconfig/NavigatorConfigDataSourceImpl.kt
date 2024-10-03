package com.w2sv.datastore.proto.navigatorconfig

import androidx.datastore.core.DataStore
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class NavigatorConfigDataSourceImpl @Inject constructor(private val navigatorConfigProtoDataStore: DataStore<NavigatorConfigProto>) :
    NavigatorConfigDataSource {

    override val navigatorConfig: Flow<NavigatorConfig> =
        navigatorConfigProtoDataStore.data.map { it.toExternal() }

    override suspend fun saveNavigatorConfig(config: NavigatorConfig) {
        updateData { config }
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
            transform(protoConfig.toExternal()).toProto(protoConfig.hasBeenMigrated)
        }
    }

    override suspend fun unsetAutoMoveConfig(fileType: FileType, sourceType: SourceType) {
        updateData {
            it.copyWithAlteredAutoMoveConfig(fileType, sourceType) {
                AutoMoveConfig.Empty
            }
        }
    }

    override suspend fun saveQuickMoveDestination(
        fileType: FileType,
        sourceType: SourceType,
        destination: LocalDestinationApi
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
    ): Flow<List<LocalDestinationApi>> =
        navigatorConfig.map { it.sourceConfig(fileType, sourceType).lastMoveDestinations }
}



