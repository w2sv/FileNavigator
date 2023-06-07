package com.w2sv.filenavigator.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.w2sv.filenavigator.mediastore.MediaType
import javax.inject.Inject

class DataStoreRepository @Inject constructor(dataStore: DataStore<Preferences>): AbstractDataStoreRepository(dataStore){

    val accountForMediaType = mapFromDataStoreProperties(MediaType.values())

    val accountForMediaTypeOrigin = mapFromDataStoreProperties(
        MediaType.values()
            .map { it.origins }
            .flatten()
            .toTypedArray()
    )
}