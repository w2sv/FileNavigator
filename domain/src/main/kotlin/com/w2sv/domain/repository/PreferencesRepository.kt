package com.w2sv.domain.repository

import com.w2sv.androidutils.datastorage.datastore.DataStoreFlow
import com.w2sv.domain.model.Theme

interface PreferencesRepository {
    val theme: DataStoreFlow<Theme>
    val useDynamicColors: DataStoreFlow<Boolean>
    val postNotificationsPermissionRequested: DataStoreFlow<Boolean>
}