package com.w2sv.filenavigator.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PreferencesKey {
    val SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL =
        booleanPreferencesKey("showedManageExternalStorageRational")

    val DISABLE_LISTENER_ON_LOW_BATTERY =
        booleanPreferencesKey("disableListenerOnLowBattery")

    val MANAGE_EXTERNAL_STORAGE_PERMISSION_PREVIOUSLY_GRANTED = booleanPreferencesKey(
        "manageExternalStoragePermissionPreviouslyGranted"
    )

    val IN_APP_THEME = intPreferencesKey(
        "inAppTheme"
    )
}