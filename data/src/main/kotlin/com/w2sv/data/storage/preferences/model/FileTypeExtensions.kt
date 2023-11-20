package com.w2sv.data.storage.preferences.model

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.DataStoreEntry
import com.w2sv.domain.model.FileType

internal val FileType.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(
        preferencesKey = booleanPreferencesKey(name = name),
        defaultValue = true
    )

internal val FileType.Source.isEnabledDSE
    get() = DataStoreEntry.UniType.Impl(  // TODO: Remove for NonMedia
        booleanPreferencesKey(getPreferencesKeyContent("IS_ENABLED")),
        true
    )

internal val FileType.Source.lastMoveDestinationDSE
    get() = DataStoreEntry.UriValued.Impl(
        stringPreferencesKey(getPreferencesKeyContent("LAST_MOVE_DESTINATION")),
        null
    )

private fun FileType.Source.getPreferencesKeyContent(keySuffix: String): String =
    "${fileType.name}.$kind.$keySuffix"