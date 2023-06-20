package com.w2sv.filenavigator.datastore

import android.net.Uri
import androidx.datastore.preferences.core.Preferences

/**
 * Interface for Classes, which are to be managed as map by a [AbstractPreferencesDataStoreRepository].
 */
interface DataStoreEntry<T, KeyType> {
    val defaultValue: T
    val preferencesKey: Preferences.Key<KeyType>

    interface UniType<T> : DataStoreEntry<T, T>

    interface EnumValued<E : Enum<E>> : DataStoreEntry<E, Int>

    interface UriValued: DataStoreEntry<Uri?, String>
}