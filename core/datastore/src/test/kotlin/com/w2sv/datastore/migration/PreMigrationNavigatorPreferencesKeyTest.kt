package com.w2sv.datastore.migration

import junit.framework.TestCase.assertEquals
import org.junit.Test

class PreMigrationNavigatorPreferencesKeyTest {

    @Test
    fun keys() {
        assertEquals(
            "[disableNavigatorOnLowBattery, Image, Image.Camera.IS_ENABLED, Image.Camera.LAST_MOVE_DESTINATION, " +
                "Image.Screenshot.IS_ENABLED, Image.Screenshot.LAST_MOVE_DESTINATION, Image.OtherApp.IS_ENABLED, " +
                "Image.OtherApp.LAST_MOVE_DESTINATION, Image.Download.IS_ENABLED, Image.Download.LAST_MOVE_DESTINATION, " +
                "Video, Video.Camera.IS_ENABLED, Video.Camera.LAST_MOVE_DESTINATION, Video.OtherApp.IS_ENABLED, " +
                "Video.OtherApp.LAST_MOVE_DESTINATION, Video.Download.IS_ENABLED, Video.Download.LAST_MOVE_DESTINATION, " +
                "Audio, Audio.Recording.IS_ENABLED, Audio.Recording.LAST_MOVE_DESTINATION, Audio.OtherApp.IS_ENABLED, " +
                "Audio.OtherApp.LAST_MOVE_DESTINATION, Audio.Download.IS_ENABLED, Audio.Download.LAST_MOVE_DESTINATION, " +
                "PDF, PDF.Download.IS_ENABLED, PDF.Download.LAST_MOVE_DESTINATION, Text, Text.Download.IS_ENABLED," +
                " Text.Download.LAST_MOVE_DESTINATION, Archive, Archive.Download.IS_ENABLED, Archive.Download.LAST_MOVE_DESTINATION, " +
                "APK, APK.Download.IS_ENABLED, APK.Download.LAST_MOVE_DESTINATION, EBook, EBook.Download.IS_ENABLED," +
                " EBook.Download.LAST_MOVE_DESTINATION]",
            PreMigrationNavigatorPreferencesKey.keys().toList().toString()
        )
    }
}
