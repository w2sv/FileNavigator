package com.w2sv.filenavigator.ui.designsystem.drawer

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.core.net.toUri
import com.w2sv.androidutils.openUrl
import com.w2sv.androidutils.packagePlayStoreUrl
import com.w2sv.androidutils.startActivity
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.AppUrl
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.designsystem.IconSize
import com.w2sv.filenavigator.ui.designsystem.ItemRowDefaults
import com.w2sv.filenavigator.ui.navigation.LocalNavigator
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.theme.onSurfaceVariantDecreasedAlpha
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun NavigationDrawerSheetItemColumn(
    closeDrawer: suspend () -> Unit,
    modifier: Modifier = Modifier,
    scope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    navigator: Navigator = LocalNavigator.current
) {
    Column(modifier = modifier) {
        remember {
            listOf(
                NavigationDrawerSheetElement.Header(
                    titleRes = R.string.settings,
                    modifier = Modifier
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_smartphone_24,
                    labelRes = R.string.app_settings,
                    onClick = {
                        scope.launch {
                            closeDrawer()
                            navigator.toAppSettings()
                        }
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_app_logo_24,
                    labelRes = R.string.navigator_settings,
                    onClick = {
                        scope.launch {
                            closeDrawer()
                            navigator.toNavigatorSettings()
                        }
                    }
                ),
                NavigationDrawerSheetElement.Header(
                    titleRes = R.string.legal
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_policy_24,
                    labelRes = R.string.privacy_policy,
                    onClick = { context.openUrl(AppUrl.PRIVACY_POLICY) }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_copyright_24,
                    labelRes = R.string.license,
                    onClick = { context.openUrl(AppUrl.LICENSE) }
                ),
                NavigationDrawerSheetElement.Header(
                    titleRes = R.string.support_the_app
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_star_rate_24,
                    labelRes = R.string.rate,
                    explanationRes = R.string.rate_the_app_in_the_playstore,
                    onClick = {
                        context.startActivity(
                            intent = Intent(
                                Intent.ACTION_VIEW,
                                context.packagePlayStoreUrl.toUri()
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
                    explanationRes = R.string.share_action_explanation,
                    onClick = {
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(context.getString(R.string.share_action_text, AppUrl.PLAYSTORE_LISTING))
                            .startChooser()
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_bug_report_24,
                    explanationRes = R.string.report_a_bug_request_a_feature_explanation,
                    labelRes = R.string.report_a_bug_request_a_feature,
                    onClick = { context.openUrl(AppUrl.CREATE_ISSUE) }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_donate_24,
                    labelRes = R.string.support_the_development,
                    explanationRes = R.string.buy_me_a_coffee_as_a_sign_of_gratitude,
                    onClick = { context.openUrl(AppUrl.DONATE) }
                ),
                NavigationDrawerSheetElement.Header(
                    titleRes = R.string.more
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_developer_24,
                    labelRes = R.string.about_the_developer,
                    explanationRes = R.string.check_out_my_other_apps,
                    onClick = { context.openUrl(AppUrl.GOOGLE_PLAY_DEVELOPER_PAGE) }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_github_24,
                    labelRes = R.string.source,
                    explanationRes = R.string.examine_the_app_s_source_code_on_github,
                    onClick = { context.openUrl(AppUrl.GITHUB_REPOSITORY) }
                )
            )
        }
            .forEach { element ->
                when (element) {
                    is NavigationDrawerSheetElement.Item -> {
                        DrawerSheetItem(
                            item = element,
                            modifier = element.modifier
                        )
                    }

                    is NavigationDrawerSheetElement.Header -> {
                        DrawerSheetSubHeader(
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
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int,
        @StringRes val explanationRes: Int? = null,
        override val modifier: Modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        val onClick: () -> Unit
    ) : NavigationDrawerSheetElement
}

@Composable
private fun DrawerSheetSubHeader(@StringRes titleRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = titleRes),
        modifier = modifier,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun DrawerSheetItem(item: NavigationDrawerSheetElement.Item, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clickable(onClick = item.onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(size = IconSize.Big),
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(id = item.labelRes),
                modifier = Modifier.padding(start = ItemRowDefaults.IconTextSpacing),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
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
