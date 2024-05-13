package com.w2sv.filenavigator.ui.designsystem.drawer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.w2sv.androidutils.generic.appPlayStoreUrl
import com.w2sv.androidutils.generic.dynamicColorsSupported
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.composed.extensions.thenIfNotNull
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.RightAligned
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel
import com.w2sv.filenavigator.ui.utils.activityViewModel

private object AppUrl {
    const val LICENSE = "https://github.com/w2sv/FileNavigator/blob/main/LICENSE"
    const val PRIVACY_POLICY = "https://github.com/w2sv/FileNavigator/blob/main/PRIVACY-POLICY.md"
    const val GITHUB_REPOSITORY = "https://github.com/w2sv/FileNavigator"
    const val CREATE_ISSUE = "https://github.com/w2sv/FileNavigator/issues/new"
    const val GOOGLE_PLAY_DEVELOPER_PAGE =
        "https://play.google.com/store/apps/dev?id=6884111703871536890"
}

@Composable
internal fun NavigationDrawerSheetItemColumn(
    modifier: Modifier = Modifier,
    appVM: AppViewModel = activityViewModel()
) {
    Column(modifier = modifier) {
        val context: Context = LocalContext.current

        remember {
            buildList {
                add(NavigationDrawerSheetElement.Header(R.string.appearance))
                add(
                    NavigationDrawerSheetElement.Item.Custom(
                        R.drawable.ic_nightlight_24,
                        R.string.theme
                    ) {
                        ThemeSelectionRow(
                            selected = appVM.theme.collectAsStateWithLifecycle().value,
                            onSelected = appVM::saveTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 22.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        )
                    })
                if (dynamicColorsSupported) {
                    add(NavigationDrawerSheetElement.Item.Custom(
                        iconRes = R.drawable.ic_palette_24,
                        labelRes = R.string.use_dynamic_colors,
                        explanationRes = R.string.use_colors_derived_from_your_wallpaper
                    ) {
                        RightAligned {
                            Switch(
                                checked = appVM.useDynamicColors.collectAsStateWithLifecycle().value,
                                onCheckedChange = appVM::saveUseDynamicColors
                            )
                        }
                    })
                }
                add(NavigationDrawerSheetElement.Header(R.string.legal))
                add(
                    NavigationDrawerSheetElement.Item.Clickable(
                        R.drawable.ic_policy_24,
                        R.string.privacy_policy
                    ) {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.PRIVACY_POLICY)
                    })
                add(
                    NavigationDrawerSheetElement.Item.Clickable(
                        R.drawable.ic_copyright_24,
                        R.string.license
                    ) {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.LICENSE)
                    })
                add(NavigationDrawerSheetElement.Header(R.string.support_the_app))
                add(
                    NavigationDrawerSheetElement.Item.Clickable(
                        R.drawable.ic_star_rate_24,
                        R.string.rate
                    ) {
                        try {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(appPlayStoreUrl(context))
                                )
                                    .setPackage("com.android.vending")
                            )
                        } catch (e: ActivityNotFoundException) {
                            context.showToast(context.getString(R.string.you_re_not_signed_into_the_play_store))
                        }
                    })
                add(
                    NavigationDrawerSheetElement.Item.Clickable(
                        R.drawable.ic_share_24,
                        R.string.share
                    ) {
                        ShareCompat.IntentBuilder(context)
                            .setType("text/plain")
                            .setText(context.getString(R.string.share_action_text))
                            .startChooser()
                    })
                add(
                    NavigationDrawerSheetElement.Item.Clickable(
                        R.drawable.ic_bug_report_24,
                        R.string.report_a_bug
                    ) {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.CREATE_ISSUE)
                    })
                add(NavigationDrawerSheetElement.Header(R.string.more))
                add(
                    NavigationDrawerSheetElement.Item.Clickable(
                        R.drawable.ic_developer_24,
                        R.string.developer
                    ) {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.GOOGLE_PLAY_DEVELOPER_PAGE)
                    })
                add(
                    NavigationDrawerSheetElement.Item.Clickable(
                        R.drawable.ic_github_24,
                        R.string.source
                    ) {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.GITHUB_REPOSITORY)
                    })
            }
        }
            .forEach {
                when (it) {
                    is NavigationDrawerSheetElement.Item -> {
                        Item(
                            item = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                        )
                    }

                    is NavigationDrawerSheetElement.Header -> {
                        SubHeader(
                            titleRes = it.titleRes,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
    }
}

@Immutable
private sealed interface NavigationDrawerSheetElement {

    @Immutable
    interface Item : NavigationDrawerSheetElement {
        val iconRes: Int
        val labelRes: Int
        val explanationRes: Int?

        @Immutable
        data class Clickable(
            @DrawableRes override val iconRes: Int,
            @StringRes override val labelRes: Int,
            @StringRes override val explanationRes: Int? = null,
            val onClick: () -> Unit
        ) : Item

        @Immutable
        data class Custom(
            @DrawableRes override val iconRes: Int,
            @StringRes override val labelRes: Int,
            @StringRes override val explanationRes: Int? = null,
            val content: @Composable RowScope.() -> Unit
        ) : Item
    }

    @Immutable
    data class Header(
        @StringRes val titleRes: Int,
    ) : NavigationDrawerSheetElement
}

@Composable
private fun SubHeader(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(id = titleRes),
        modifier = modifier,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.tertiary,
    )
}

@Composable
private fun Item(
    item: NavigationDrawerSheetElement.Item,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        MainItemRow(item = item, modifier = Modifier.fillMaxWidth())
        item.explanationRes?.let {
            Text(
                text = stringResource(id = it),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = iconSize + labelStartPadding, top = 2.dp),
                fontSize = 14.sp
            )
        }
    }
}

private val iconSize = 28.dp
private val labelStartPadding = 16.dp

@Composable
private fun MainItemRow(
    item: NavigationDrawerSheetElement.Item,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .thenIfNotNull(item as? NavigationDrawerSheetElement.Item.Clickable) {
                clickable {
                    it.onClick()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(size = iconSize),
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = stringResource(id = item.labelRes),
            modifier = Modifier.padding(start = labelStartPadding),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )

        if (item is NavigationDrawerSheetElement.Item.Custom) {
            item.content(this)
        }
    }
}