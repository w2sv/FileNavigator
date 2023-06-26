package com.w2sv.filenavigator.datastore

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.w2sv.androidutils.datastorage.datastore.preferences.AbstractPreferencesDataStoreRepository
import com.w2sv.filenavigator.ui.model.FileType
import com.w2sv.filenavigator.ui.components.Theme
import com.w2sv.filenavigator.utils.StorageAccessStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PreferencesDataStoreRepository @Inject constructor(dataStore: DataStore<Preferences>) :
    AbstractPreferencesDataStoreRepository(dataStore) {

    // =======================
    // FileType-related
    // =======================

    val fileTypeStatus: Map<FileType.Status.StoreEntry, Flow<FileType.Status>> =
        getEnumValuedFlowMap(FileType.all.map { it.status })

    val mediaFileSourceEnabled: Map<FileType.Source.IsEnabled, Flow<Boolean>> = getFlowMap(
        FileType.Media.all
            .map { it.sources }
            .flatten()
            .map { it.isEnabled }
    )

    fun getFileSourceDefaultDestinationFlow(source: FileType.Source): Flow<Uri?> =
        getUriFlow(source.defaultDestination)

    fun getFileSourceDefaultDestinationIsLockedFlow(source: FileType.Source): Flow<Boolean> =
        getFlow(source.defaultDestinationIsLocked)

    // =======================
    // Internally set flags
    // =======================

    val showedManageExternalStorageRational: Flow<Boolean> =
        getFlow(PreferencesKey.SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL, false)

    val showedPostNotificationsPermissionsRational: Flow<Boolean> =
        getFlow(PreferencesKey.SHOWED_POST_NOTIFICATIONS_PERMISSION_RATIONAL, false)

    // =======================
    // User Preferences
    // =======================

    val disableListenerOnLowBattery: Flow<Boolean> =
        getFlow(PreferencesKey.DISABLE_LISTENER_ON_LOW_BATTERY, false)

    val inAppTheme: Flow<Theme> = getEnumFlow(
        PreferencesKey.IN_APP_THEME,
        Theme.DeviceDefault
    )

    // =======================
    // Other
    // =======================

    val previousStorageAccessStatus: Flow<StorageAccessStatus> = getEnumFlow(
        PreferencesKey.PREVIOUS_STORAGE_ACCESS_STATUS,
        StorageAccessStatus.NoAccess
    )
}