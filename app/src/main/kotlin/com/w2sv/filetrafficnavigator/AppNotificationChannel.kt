package com.w2sv.filetrafficnavigator

enum class AppNotificationChannel(val title: String) {
    STARTED_FOREGROUND_SERVICE("Listening to newly created files"),
    NEW_FILE_DETECTED("Detected a new file")
}