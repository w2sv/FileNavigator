package com.w2sv.common.notifications

data class NotificationChannelProperties(
    val id: String,
    val name: String,
    val idGroupSeed: Int = -1
)