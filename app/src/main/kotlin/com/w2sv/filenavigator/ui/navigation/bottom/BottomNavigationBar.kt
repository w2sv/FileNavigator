package com.w2sv.filenavigator.ui.navigation.bottom

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.modules.resources.R

@Composable
fun BottomNavigationBar(navigator: Navigator) {
    Column {
        HorizontalDivider(
            thickness = Hairline,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        ShortNavigationBar(arrangement = EqualWeight) {
            BottomNavigationTab.entries.forEach { tab ->
                val label = stringResource(tab.labelRes)
                ShortNavigationBarItem(
                    selected = navigator.currentBottomNavRoute == tab.route,
                    onClick = { tab.navigate(navigator) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = label
                        )
                    },
                    label = { Text(label) },
                    iconPosition = Top
                )
            }
        }
    }
}

private enum class BottomNavigationTab(
    val route: BottomNavRoute,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val navigate: Navigator.() -> Unit
) {
    Home(
        route = BottomNavRoute.Home,
        labelRes = R.string.home,
        icon = Icons.Outlined.Dashboard,
        navigate = Navigator::toHome
    ),
    NavigatorSettings(
        route = BottomNavRoute.NavigatorSettings,
        labelRes = R.string.navigator_settings,
        icon = Icons.Outlined.Tune,
        navigate = Navigator::toNavigatorSettings
    ),
    AppSettings(
        route = BottomNavRoute.AppSettings,
        labelRes = R.string.app_settings,
        icon = Icons.Outlined.Settings,
        navigate = Navigator::toAppSettings
    )
}
