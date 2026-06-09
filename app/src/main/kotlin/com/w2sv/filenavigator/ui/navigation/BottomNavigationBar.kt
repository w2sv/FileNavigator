package com.w2sv.filenavigator.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.w2sv.modules.resources.R

@Composable
fun BottomNavigationBar(navigator: Navigator) {
    Column {
        HorizontalDivider(
            thickness = Dp.Hairline,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        ShortNavigationBar(arrangement = ShortNavigationBarArrangement.EqualWeight) {
            BottomNavigationTab.entries.forEach { tab ->
                val label = stringResource(tab.labelRes)
                ShortNavigationBarItem(
                    selected = navigator.currentScreen == tab.screen,
                    onClick = { tab.navigate(navigator) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = label
                        )
                    },
                    label = { Text(label) },
                    iconPosition = NavigationItemIconPosition.Top
                )
            }
        }
    }
}

private enum class BottomNavigationTab(
    val screen: Screen,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val navigate: Navigator.() -> Unit
) {
    Home(
        screen = Screen.Home,
        labelRes = R.string.home,
        icon = Icons.Outlined.Dashboard,
        navigate = Navigator::toHome
    ),
    NavigatorSettings(
        screen = Screen.NavigatorSettings,
        labelRes = R.string.navigator_settings,
        icon = Icons.Outlined.Tune,
        navigate = Navigator::toNavigatorSettings
    ),
    AppSettings(
        screen = Screen.AppSettings,
        labelRes = R.string.app_settings,
        icon = Icons.Outlined.Settings,
        navigate = Navigator::toAppSettings
    )
}
