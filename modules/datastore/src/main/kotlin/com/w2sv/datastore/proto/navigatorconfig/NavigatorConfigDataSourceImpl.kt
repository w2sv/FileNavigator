package com.w2sv.datastore.proto.navigatorconfig

import androidx.datastore.core.DataStore
import com.w2sv.core.di.ApplicationIoScope
import com.w2sv.datastore.NavigatorConfigProto
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
}
