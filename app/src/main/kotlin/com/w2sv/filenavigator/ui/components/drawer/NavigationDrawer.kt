package com.w2sv.filenavigator.ui.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.BuildConfig
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun NavigationDrawer(
    state: DrawerState,
    modifier: Modifier = Modifier,
    scope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            Sheet {
                SheetContent(closeDrawer = { scope.launch { state.close() } })
            }
        },
        drawerState = state
    ) {
        content()
    }
}

@Composable
private fun Sheet(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalDrawerSheet(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(vertical = 32.dp, horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header()
            Divider(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp))
            content()
        }
    }
}

@Composable
private fun Header() {
    AppLogoWCircularBackground()
    Spacer(modifier = Modifier.height(18.dp))
    AppFontText(text = stringResource(id = R.string.version).format(BuildConfig.VERSION_NAME))
    Spacer(modifier = Modifier.height(12.dp))
    AppFontText(text = "© 2023 | w2sv")
}

@Composable
fun AppLogoWCircularBackground(modifier: Modifier = Modifier) {
    Icon(
        painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.inversePrimary,
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}