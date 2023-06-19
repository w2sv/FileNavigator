package com.w2sv.filenavigator.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PreferencesKey {
    val SHOWED_MANAGE_EXTERNAL_STORAGE_RATIONAL =
        booleanPreferencesKey("showedManageExternalStorageRational")

    val SHOWED_POST_NOTIFICATIONS_PERMISSION_RATIONAL = booleanPreferencesKey("showedPostNotificationsPermissionRational")

    val DISABLE_LISTENER_ON_LOW_BATTERY =
        booleanPreferencesKey("disableListenerOnLowBattery")

    val PREVIOUS_STORAGE_ACCESS_STATUS = intPreferencesKey(
        "previousStorageAccessStatus"
    )

    val IN_APP_THEME = intPreferencesKey(
        "inAppTheme"
    )
}