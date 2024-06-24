package com.w2sv.filenavigator.ui.designsystem.drawer

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.navigation.NavController
import com.ramcosta.composedestinations.generated.destinations.AppSettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.NavigatorSettingsScreenDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.w2sv.androidutils.openUrl
import com.w2sv.androidutils.packagePlayStoreUrl
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.AppUrl
import com.w2sv.common.utils.startActivityWithActivityNotFoundExceptionHandling
import com.w2sv.composed.extensions.thenIfNotNull
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.ItemRowDefaults
import com.w2sv.filenavigator.ui.designsystem.RightAligned
import com.w2sv.filenavigator.ui.theme.onSurfaceVariantDecreasedAlpha
import com.w2sv.filenavigator.ui.utils.LocalNavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun NavigationDrawerSheetItemColumn(
    closeDrawer: suspend () -> Unit,
    modifier: Modifier = Modifier,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    navHostController: NavController = LocalNavHostController.current
) {
    Column(modifier = modifier) {
        remember {
            listOf(
                NavigationDrawerSheetElement.Header(
                    titleRes = R.string.settings,
                    modifier = Modifier
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = com.w2sv.core.navigator.R.drawable.ic_settings_24,
                    labelRes = R.string.app_settings,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        scope.launch {
                            closeDrawer()
                            navHostController.navigate(AppSettingsScreenDestination)
                        }
                    },
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = com.w2sv.core.navigator.R.drawable.ic_app_logo_24,
                    labelRes = R.string.navigator_settings,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        scope.launch {
                            closeDrawer()
                            navHostController.navigate(NavigatorSettingsScreenDestination)
                        }
                    },
                ),
                NavigationDrawerSheetElement.Header(
                    titleRes = R.string.legal
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_policy_24,
                    labelRes = R.string.privacy_policy,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        context.openUrl(AppUrl.PRIVACY_POLICY)
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_copyright_24,
                    labelRes = R.string.license,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        context.openUrl(AppUrl.LICENSE)
                    }
                ),
                NavigationDrawerSheetElement.Header(
                    titleRes = R.string.support_the_app
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_star_rate_24,
                    labelRes = R.string.rate,
                    explanationRes = R.string.rate_the_app_in_the_playstore,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        context.startActivityWithActivityNotFoundExceptionHandling(
                            intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(context.packagePlayStoreUrl)
                            )
                                .setPackage("com.android.vending"),
                            onActivityNotFoundException = {
                                it.showToast(context.getString(R.string.you_re_not_signed_into_the_play_store))
                            }
                        )
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_share_24,
                    labelRes = R.string.share,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(context.getString(R.string.share_action_text))
                            .startChooser()
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_bug_report_24,
                    labelRes = R.string.report_a_bug_request_a_feature,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        context.openUrl(AppUrl.CREATE_ISSUE)
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_donate_24,
                    labelRes = R.string.support_development,
                    explanationRes = R.string.buy_me_a_coffee_as_a_sign_of_gratitude,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        context.openUrl(AppUrl.DONATE)
                    }
                ),
                NavigationDrawerSheetElement.Header(
                    titleRes = R.string.more
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_developer_24,
                    labelRes = R.string.developer,
                    explanationRes = R.string.check_out_my_other_apps,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        context.openUrl(AppUrl.GOOGLE_PLAY_DEVELOPER_PAGE)
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_github_24,
                    labelRes = R.string.source,
                    explanationRes = R.string.examine_the_app_s_source_code_on_github,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        context.openUrl(AppUrl.GITHUB_REPOSITORY)
                    }
                )
            )
        }
            .forEach { element ->
                when (element) {
                    is NavigationDrawerSheetElement.Item -> {
                        Item(
                            item = element,
                            modifier = element.modifier,
                        )
                    }

                    is NavigationDrawerSheetElement.Header -> {
                        SubHeader(
                            titleRes = element.titleRes,
                            modifier = element.modifier
                        )
                    }
                }
            }
    }
}

@Immutable
private sealed interface NavigationDrawerSheetElement {
    val modifier: Modifier

    @Immutable
    data class Header(
        @StringRes val titleRes: Int,
        override val modifier: Modifier = Modifier
            .padding(top = 20.dp, bottom = 4.dp)
    ) : NavigationDrawerSheetElement

    @Immutable
    data class Item(
        val iconRes: Int,
        val labelRes: Int,
        val explanationRes: Int? = null,
        override val modifier: Modifier = DefaultModifier,
        val type: Type
    ) : NavigationDrawerSheetElement {

        companion object {
            val DefaultModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        }

        @Immutable
        sealed interface Type {
            @Immutable
            data class Clickable(val onClick: () -> Unit) : Type

            @Immutable
            data class Switch(
                val checked: () -> Boolean,
                val onCheckedChange: (Boolean) -> Unit
            ) : Type

            @Immutable
            data class Custom(val content: @Composable RowScope.() -> Unit) : Type
        }
    }
}

@Composable
private fun SubHeader(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(id = titleRes),
        modifier = modifier,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun Item(
    item: NavigationDrawerSheetElement.Item,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .thenIfNotNull(item.type as? NavigationDrawerSheetElement.Item.Type.Clickable) {
                clickable {
                    it.onClick()
                }
            }
    ) {
        MainItemRow(item = item, modifier = Modifier.fillMaxWidth())
        item.explanationRes?.let {
            Text(
                text = stringResource(id = it),
                color = MaterialTheme.colorScheme.onSurfaceVariantDecreasedAlpha,
                modifier = Modifier.padding(start = IconSize.Big + ItemRowDefaults.IconTextSpacing),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun MainItemRow(
    item: NavigationDrawerSheetElement.Item,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(size = IconSize.Big),
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = stringResource(id = item.labelRes),
            modifier = Modifier.padding(start = ItemRowDefaults.IconTextSpacing),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )

        when (val type = item.type) {
            is NavigationDrawerSheetElement.Item.Type.Custom -> {
                type.content(this)
            }

            is NavigationDrawerSheetElement.Item.Type.Switch -> {
                RightAligned {
                    Switch(checked = type.checked(), onCheckedChange = type.onCheckedChange)
                }
            }

            else -> Unit
        }
    }
}
