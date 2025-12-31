package com.w2sv.filenavigator.ui.designsystem.drawer

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import com.w2sv.androidutils.openUrl
import com.w2sv.androidutils.packagePlayStoreUrl
import com.w2sv.androidutils.startActivity
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.AppUrl
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.LocalNavigator
import com.w2sv.filenavigator.ui.designsystem.IconSize
import com.w2sv.filenavigator.ui.designsystem.ItemRowTokens
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.theme.onSurfaceVariantDecreasedAlpha
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
private class DrawerActionScope(val context: Context, private val closeDrawer: suspend () -> Unit, private val navigator: Navigator) {
    suspend fun closeDrawerAndNavigate(block: Navigator.() -> Unit) {
        closeDrawer()
        block(navigator)
    }
}

@Composable
private fun rememberDrawerActionScope(
    closeDrawer: suspend () -> Unit,
    context: Context = LocalContext.current,
    navigator: Navigator = LocalNavigator.current
): DrawerActionScope =
    remember(context, navigator, closeDrawer) {
        DrawerActionScope(
            context = context,
            closeDrawer = closeDrawer,
            navigator = navigator
        )
    }

@Immutable
private sealed interface DrawerItem {

    @Immutable
    @JvmInline
    value class Header(@StringRes val titleRes: Int) : DrawerItem

    @Immutable
    data class Action(
        @DrawableRes val iconRes: Int,
        @StringRes val labelRes: Int,
        @StringRes val explanationRes: Int? = null,
        val onClick: suspend DrawerActionScope.() -> Unit
    ) : DrawerItem
}

private fun navigationDrawerElements(): List<DrawerItem> =
    listOf(
        DrawerItem.Header(titleRes = R.string.settings),
        DrawerItem.Action(
            iconRes = R.drawable.ic_smartphone_24,
            labelRes = R.string.app_settings,
            onClick = { closeDrawerAndNavigate { toAppSettings() } }
        ),
        DrawerItem.Action(
            iconRes = R.drawable.ic_app_logo_24,
            labelRes = R.string.navigator_settings,
            onClick = { closeDrawerAndNavigate { toNavigatorSettings() } }
        ),
        DrawerItem.Header(titleRes = R.string.legal),
        DrawerItem.Action(
            iconRes = R.drawable.ic_policy_24,
            labelRes = R.string.privacy_policy,
            onClick = { context.openUrl(AppUrl.PRIVACY_POLICY) }
        ),
        DrawerItem.Action(
            iconRes = R.drawable.ic_copyright_24,
            labelRes = R.string.license,
            onClick = { context.openUrl(AppUrl.LICENSE) }
        ),
        DrawerItem.Header(titleRes = R.string.support_the_app),
        DrawerItem.Action(
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
                        context.showToast(R.string.you_re_not_signed_into_the_play_store)
                    }
                )
            }
        ),
        DrawerItem.Action(
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
        DrawerItem.Action(
            iconRes = R.drawable.ic_bug_report_24,
            explanationRes = R.string.report_a_bug_request_a_feature_explanation,
            labelRes = R.string.report_a_bug_request_a_feature,
            onClick = { context.openUrl(AppUrl.CREATE_ISSUE) }
        ),
        DrawerItem.Action(
            iconRes = R.drawable.ic_donate_24,
            labelRes = R.string.support_the_development,
            explanationRes = R.string.buy_me_a_coffee_as_a_sign_of_gratitude,
            onClick = { context.openUrl(AppUrl.DONATE) }
        ),
        DrawerItem.Header(titleRes = R.string.more),
        DrawerItem.Action(
            iconRes = R.drawable.ic_developer_24,
            labelRes = R.string.about_the_developer,
            explanationRes = R.string.check_out_my_other_apps,
            onClick = { context.openUrl(AppUrl.GOOGLE_PLAY_DEVELOPER_PAGE) }
        ),
        DrawerItem.Action(
            iconRes = R.drawable.ic_github_24,
            labelRes = R.string.source,
            explanationRes = R.string.examine_the_app_s_source_code_on_github,
            onClick = { context.openUrl(AppUrl.GITHUB_REPOSITORY) }
        )
    )

@Composable
fun NavigationDrawerItemColumn(closeDrawer: suspend () -> Unit, modifier: Modifier = Modifier) {
    val actionScope = rememberDrawerActionScope(closeDrawer)
    val coroutineScope = rememberCoroutineScope()
    val elements = remember { navigationDrawerElements() }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HomeScreenNavigationDrawerTokens.itemSpacing)) {
        elements.forEach { element ->
            when (element) {
                is DrawerItem.Action -> {
                    Action(
                        action = element,
                        actionScope = actionScope,
                        coroutineScope = coroutineScope
                    )
                }

                is DrawerItem.Header -> {
                    GroupHeader(
                        titleRes = element.titleRes,
                        modifier = Modifier.padding(HomeScreenNavigationDrawerTokens.headerPadding)
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(@StringRes titleRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = titleRes),
        modifier = modifier,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun Action(
    action: DrawerItem.Action,
    actionScope: DrawerActionScope,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(modifier = modifier.clickable { coroutineScope.launch { action.onClick(actionScope) } }) {
        val (icon, label, explanation) = createRefs()

        Icon(
            painter = painterResource(id = action.iconRes),
            contentDescription = null,
            tint = colorScheme.primary,
            modifier = Modifier
                .size(IconSize.Big)
                .constrainAs(icon) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
        )

        Text(
            text = stringResource(id = action.labelRes),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.constrainAs(label) {
                start.linkTo(icon.end, margin = ItemRowTokens.IconTextSpacing)
                centerVerticallyTo(icon)
            }
        )

        action.explanationRes?.let {
            Text(
                text = stringResource(id = it),
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariantDecreasedAlpha,
                modifier = Modifier.constrainAs(explanation) {
                    start.linkTo(label.start)
                    top.linkTo(label.bottom)
                }
            )
        }
    }
}
