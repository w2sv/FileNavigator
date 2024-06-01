package com.w2sv.datastore.proto.navigatorconfig

import android.net.Uri
import androidx.datastore.core.DataStore
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
        destination: Uri
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
//            it.copy {
//                fileTypeToConfig[FileType.values.indexOf(fileType)] =
//                    fileTypeToConfig.getValue(FileType.values.indexOf(fileType)).copy {
//                        sourceTypeToConfig[sourceType.ordinal] =
//                            sourceTypeToConfig.getValue(sourceType.ordinal).copy {
//                                lastMoveDestination[0] = destination.toString()
//                            }
//                    }
//            }
        }
    }

    override fun lastMoveDestination(
        fileType: FileType,
        sourceType: SourceType
    ): Flow<List<Uri>> =
        navigatorConfig.map { it.sourceConfig(fileType, sourceType).lastMoveDestinations }
}



