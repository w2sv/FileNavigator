package com.w2sv.filenavigator.ui.screens.main

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.coroutines.launchDelayed
import com.w2sv.androidutils.generic.appPlayStoreUrl
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.filenavigator.BuildConfig
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.AppFontText
import com.w2sv.filenavigator.ui.ThemeSelectionDialog
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    homeScreenViewModel: MainScreenViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    val theme by homeScreenViewModel.inAppTheme.collectAsState()
    val themeRequiringUpdate by homeScreenViewModel.inAppTheme.statesDissimilar.collectAsState()

    var showThemeDialog by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                ThemeSelectionDialog(
                    onDismissRequest = {
                        scope.launch {
                            homeScreenViewModel.inAppTheme.reset()
                        }
                        value = false
                    },
                    selectedTheme = { theme },
                    onThemeSelected = { homeScreenViewModel.inAppTheme.value = it },
                    applyButtonEnabled = { themeRequiringUpdate },
                    onApplyButtonClick = {
                        scope.launch {
                            homeScreenViewModel.inAppTheme.sync()
                        }
                        value = false
                    }
                )
            }
        }
    var showSettingsDialog by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                SettingsDialog(closeDialog = { value = false })
            }
        }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            NavigationDrawerSheet(
                closeDrawer = closeDrawer,
                onItemSettingsPressed = {
                    scope.launchDelayed(250L) {
                        showSettingsDialog = true
                    }
                },
                onItemThemePressed = {
                    // show dialog after delay for display of navigationDrawer close animation
                    scope.launchDelayed(250L) {
                        showThemeDialog = true
                    }
                }
            )
        },
        drawerState = drawerState
    ) {
        content()
    }
}

fun DrawerState.offsetFraction(maxWidthPx: Int): State<Float> =
    derivedStateOf { 1 + offset.value / maxWidthPx }

@Composable
private fun NavigationDrawerSheet(
    closeDrawer: () -> Unit,
    onItemSettingsPressed: () -> Unit,
    onItemThemePressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    ModalDrawerSheet(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.padding(vertical = 32.dp)) {
                Image(
                    painterResource(id = R.drawable.ic_launcher_foreground),
                    null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                VersionText(Modifier.padding(top = 26.dp))
            }
            Divider(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp)
            )
            remember {
                listOf(
                    NavigationDrawerItem(
                        R.drawable.ic_settings_24,
                        R.string.navigator_settings
                    ) {
                        onItemSettingsPressed()
                    },
                    NavigationDrawerItem(
                        R.drawable.ic_nightlight_24,
                        R.string.theme
                    ) {
                        onItemThemePressed()
                    },
                    NavigationDrawerItem(
                        R.drawable.ic_share_24,
                        R.string.share
                    ) {
                        ShareCompat.IntentBuilder(it)
                            .setType("text/plain")
                            .setText(context.getString(R.string.share_action_text))
                            .startChooser()
                    },
                    NavigationDrawerItem(
                        R.drawable.ic_star_rate_24,
                        R.string.rate
                    ) {
                        try {
                            it.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(appPlayStoreUrl(it))
                                )
                                    .setPackage("com.android.vending")
                            )
                        } catch (e: ActivityNotFoundException) {
                            it.showToast(context.getString(R.string.you_re_not_signed_into_the_play_store))
                        }
                    },
                    NavigationDrawerItem(
                        R.drawable.ic_policy_24,
                        R.string.privacy_policy
                    ) {
                        it.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/WiFi-Widget/blob/main/PRIVACY-POLICY.md")
                    }
                )
            }
                .forEach {
                    NavigationDrawerItem(properties = it, closeDrawer = closeDrawer)
                }
        }
    }
}

@Composable
fun VersionText(modifier: Modifier = Modifier) {
    AppFontText(
        text = stringResource(id = R.string.version).format(BuildConfig.VERSION_NAME),
        modifier = modifier
    )
}

@Stable
private data class NavigationDrawerItem(
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
    val callback: (Context) -> Unit
)

@Composable
private fun NavigationDrawerItem(
    properties: NavigationDrawerItem,
    closeDrawer: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                properties.callback(context)
                closeDrawer()
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(size = 28.dp),
            painter = painterResource(id = properties.icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        AppFontText(
            text = stringResource(id = properties.label),
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}