package com.w2sv.datastore.proto.navigatorconfig

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import com.w2sv.common.di.ApplicationIoScope
import com.w2sv.datastore.NavigatorConfigProto
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.repository.NavigatorConfigDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

@Singleton
internal class NavigatorConfigDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<NavigatorConfigProto>,
    @ApplicationIoScope scope: CoroutineScope
) : NavigatorConfigDataSource {

    override val config = dataStore.data
        .map { it.toExternal() }
        .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1) // Share to avoid redundant .toExternal calls

    /**
     * Convenience method that enables directly modifying a [NavigatorConfig] instance, that has been mapped to from the
     * [NavigatorConfigProto] instance fetched from disk, and is mapped back to [NavigatorConfigProto] after applying the transformation.
     * Also retains [NavigatorConfigProto.hasBeenMigrated_] between the [NavigatorConfigProto] instances.
     *
     * @see DataStore.updateData
     */
    override suspend fun update(transform: suspend (NavigatorConfig) -> NavigatorConfig) {
        dataStore.updateData { protoConfig ->
            transform(protoConfig.toExternal()).toProto(protoConfig.hasBeenMigrated)
        }
    }

    // ==================
    // Auto move
    // ==================

    override suspend fun unsetAutoMoveConfig(fileType: FileType, sourceType: SourceType) {
        update {
            it.updateAutoMoveConfig(fileType, sourceType) {
                AutoMoveConfig.Empty
            }
        }
    }

    // ==================
    // Quick move
    // ==================

    override suspend fun saveQuickMoveDestination(fileType: FileType, sourceType: SourceType, destination: LocalDestinationApi) {
        update { config ->
            config.updateSourceConfig(
                fileType,
                sourceType
            ) { sourceConfig ->
                sourceConfig.copy(
                    quickMoveDestinations = updatedQuickMoveDestinations(
                        currentDestinations = sourceConfig.quickMoveDestinations,
                        destination = destination
                    )
                )
            }
        }
    }

    override suspend fun unsetQuickMoveDestination(fileType: FileType, sourceType: SourceType) {
        update { config ->
            config.updateSourceConfig(
                fileType,
                sourceType
            ) {
                it.copy(quickMoveDestinations = listOf())
            }
        }
    }
}

@VisibleForTesting
internal fun updatedQuickMoveDestinations(
    currentDestinations: List<LocalDestinationApi>,
    destination: LocalDestinationApi
): List<LocalDestinationApi> =
    when (destination.documentUri) {
        currentDestinations.firstOrNull()?.documentUri -> currentDestinations
        currentDestinations.getOrNull(1)?.documentUri -> currentDestinations.reversed()
        else -> buildList {
            add(destination)
            currentDestinations.firstOrNull()
                ?.let { firstCurrentElement ->
                    add(firstCurrentElement)
                }
        }
    }
