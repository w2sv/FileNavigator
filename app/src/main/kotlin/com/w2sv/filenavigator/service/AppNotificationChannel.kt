package com.w2sv.filenavigator.service

enum class AppNotificationChannel(val title: String) {
    STARTED_FOREGROUND_SERVICE("File Navigator is running"),
    NEW_FILE_DETECTED("Detected a new %1s file")
}