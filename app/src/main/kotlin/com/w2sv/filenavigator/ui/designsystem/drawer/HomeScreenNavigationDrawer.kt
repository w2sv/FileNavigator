package com.w2sv.filenavigator.ui.designsystem.drawer

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.core.common.R
import com.w2sv.filenavigator.BuildConfig
import com.w2sv.filenavigator.ui.util.PreviewOf
import java.time.LocalDate

internal object HomeScreenNavigationDrawerTokens {
    val verticalPadding = PaddingValues(vertical = 16.dp)
    val horizontalPadding = 24.dp
    val itemSpacing = 12.dp
    val headerPadding = PaddingValues(top = 8.dp)
}

@Composable
fun HomeScreenNavigationDrawer(state: DrawerState, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = { Sheet(drawerState = state) },
        drawerState = state,
        content = content
    )
}

@Composable
private fun Sheet(drawerState: DrawerState, modifier: Modifier = Modifier) {
    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = modifier,
        windowInsets = WindowInsets(),
        drawerTonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.systemBarsIgnoringVisibility)
                .padding(horizontal = HomeScreenNavigationDrawerTokens.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppLogoWithCircularBackground(modifier = Modifier.padding(HomeScreenNavigationDrawerTokens.verticalPadding))
            Text(
                text = stringResource(id = R.string.version, BuildConfig.VERSION_NAME),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(text = remember { "© 2023 - ${LocalDate.now().year} | w2sv" })
            HorizontalDivider(modifier = Modifier.padding(HomeScreenNavigationDrawerTokens.verticalPadding))
            NavigationDrawerItemColumn(closeDrawer = drawerState::close, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun AppLogoWithCircularBackground(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.ic_app_foreground_108),
        contentDescription = null,
        tint = colorScheme.inversePrimary,
        modifier = modifier
            .clip(CircleShape)
            .background(colorScheme.primary)
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showSystemUi = true)
@Composable
private fun Prev() {
    PreviewOf {
        HomeScreenNavigationDrawer(rememberDrawerState(DrawerValue.Open)) { Scaffold { } }
    }
}
