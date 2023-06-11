package com.w2sv.filenavigator.datastore

import androidx.datastore.preferences.core.Preferences

/**
 * Interface for Classes, which are to be managed as map by a [PreferencesDataStoreRepository].
 */
interface DataStoreVariable<T> {
    val defaultValue: T
    val preferencesKey: Preferences.Key<T>
}