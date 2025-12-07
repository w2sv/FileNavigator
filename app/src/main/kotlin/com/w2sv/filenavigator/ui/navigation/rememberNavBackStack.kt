package com.w2sv.filenavigator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Composable
@Suppress("UNCHECKED_CAST")
fun <T : NavKey> rememberNavBackStack(vararg initialKeys: T): NavBackStack<T> =
    rememberNavBackStack(*initialKeys) as NavBackStack<T>
