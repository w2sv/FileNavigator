package com.w2sv.filenavigator.ui.navigation.bottom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BottomNavScaffoldState(currentBottomNavRoute: BottomNavRoute) {
    var currentBottomNavRoute by mutableStateOf(currentBottomNavRoute)
    var fab: (@Composable () -> Unit)? by mutableStateOf<(@Composable () -> Unit)?>(null)
}
