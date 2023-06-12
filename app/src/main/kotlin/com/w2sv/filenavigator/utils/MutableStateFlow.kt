package com.w2sv.filenavigator.utils

import kotlinx.coroutines.flow.MutableStateFlow

fun MutableStateFlow<Boolean>.toggle() {
    value = !value
}