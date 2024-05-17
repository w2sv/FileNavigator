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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.w2sv.composed.OnChange
import com.w2sv.composed.extensions.thenIfNotNull
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.RightAligned
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel
import com.w2sv.filenavigator.ui.theme.onSurfaceVariantDecreasedAlpha
import com.w2sv.filenavigator.ui.theme.rememberUseDarkTheme
import com.w2sv.filenavigator.ui.utils.OptionalAnimatedVisibility
import com.w2sv.filenavigator.ui.utils.activityViewModel
import slimber.log.i

private object AppUrl {
    const val LICENSE = "https://github.com/w2sv/FileNavigator/blob/main/LICENSE.md"
    const val PRIVACY_POLICY = "https://github.com/w2sv/FileNavigator/blob/main/PRIVACY-POLICY.md"
    const val GITHUB_REPOSITORY = "https://github.com/w2sv/FileNavigator"
    const val CREATE_ISSUE = "https://github.com/w2sv/FileNavigator/issues/new"
    const val GOOGLE_PLAY_DEVELOPER_PAGE =
        "https://play.google.com/store/apps/dev?id=6884111703871536890"
    const val DONATE = "https://buymeacoffee.com/w2sv"
}

@Composable
internal fun NavigationDrawerSheetItemColumn(
    modifier: Modifier = Modifier,
    appVM: AppViewModel = activityViewModel()
) {
    Column(modifier = modifier) {
        val context: Context = LocalContext.current

        val theme by appVM.theme.collectAsStateWithLifecycle()
        // I don't know why, but it doesn't work otherwise
        val useDarkThemeExternal by rememberUseDarkTheme(theme = theme)
        var useDarkTheme by remember {
            mutableStateOf(useDarkThemeExternal)
        }
        OnChange(useDarkThemeExternal) {
            useDarkTheme = it
        }
        val useDynamicColors by appVM.useDynamicColors.collectAsStateWithLifecycle()
        val useAmoledBlackTheme by appVM.useAmoledBlackTheme.collectAsStateWithLifecycle()

        remember {
            buildList {
                add(NavigationDrawerSheetElement.Header(R.string.appearance))
                add(
                    NavigationDrawerSheetElement.Item.Custom(
                        R.drawable.ic_nightlight_24,
                        R.string.theme
                    ) {
                        ThemeSelectionRow(
                            selected = theme,
                            onSelected = appVM::saveTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 22.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        )
                    })
                add(
                    NavigationDrawerSheetElement.Item.Switch(
                        iconRes = R.drawable.ic_contrast_24,
                        labelRes = R.string.amoled_black,
                        visible = {
                            useDarkTheme
                        },
                        checked = { useAmoledBlackTheme },
                        onCheckedChange = appVM::saveUseAmoledBlackTheme
                    )
                )
                if (dynamicColorsSupported) {
                    add(
                        NavigationDrawerSheetElement.Item.Switch(
                            iconRes = R.drawable.ic_palette_24,
                            labelRes = R.string.dynamic_colors,
                            explanationRes = R.string.use_colors_derived_from_your_wallpaper,
                            checked = { useDynamicColors },
                            onCheckedChange = appVM::saveUseDynamicColors
                        )
                    )
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
                add(
                    NavigationDrawerSheetElement.Item.Clickable(
                        iconRes = R.drawable.ic_donate_24,
                        labelRes = R.string.support_development,
                        explanationRes = R.string.buy_me_a_coffee_as_a_sign_of_gratitude
                    ) {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.DONATE)
                    })
                add(NavigationDrawerSheetElement.Header(R.string.more))
                add(
                    NavigationDrawerSheetElement.Item.Clickable(
                        iconRes = R.drawable.ic_developer_24,
                        labelRes = R.string.developer,
                        explanationRes = R.string.check_out_my_other_apps
                    ) {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.GOOGLE_PLAY_DEVELOPER_PAGE)
                    })
                add(
                    NavigationDrawerSheetElement.Item.Clickable(
                        iconRes = R.drawable.ic_github_24,
                        labelRes = R.string.source,
                        explanationRes = R.string.examine_the_app_s_source_code_on_github
                    ) {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.GITHUB_REPOSITORY)
                    })
            }
        }
            .forEach { element ->
                when (element) {
                    is NavigationDrawerSheetElement.Item -> {
                        OptionalAnimatedVisibility(visible = element.visible) {
                            Item(
                                item = element,
                                modifier = itemModifier,
                            )
                        }
                    }

                    is NavigationDrawerSheetElement.Header -> {
                        SubHeader(
                            titleRes = element.titleRes,
                            modifier = headerModifier
                        )
                    }
                }
            }
    }
}

private val itemModifier = Modifier
    .fillMaxWidth()
    .padding(vertical = 12.dp)
private val headerModifier = Modifier
    .padding(top = 20.dp, bottom = 4.dp)

@Immutable
private sealed interface NavigationDrawerSheetElement {

    @Immutable
    data class Header(
        @StringRes val titleRes: Int,
    ) : NavigationDrawerSheetElement

    @Immutable
    sealed interface Item : NavigationDrawerSheetElement {
        val iconRes: Int
        val labelRes: Int
        val explanationRes: Int?
        val visible: (() -> Boolean)?

        @Immutable
        data class Clickable(
            @DrawableRes override val iconRes: Int,
            @StringRes override val labelRes: Int,
            @StringRes override val explanationRes: Int? = null,
            override val visible: (() -> Boolean)? = null,
            val onClick: () -> Unit
        ) : Item

        @Immutable
        data class Switch(
            @DrawableRes override val iconRes: Int,
            @StringRes override val labelRes: Int,
            @StringRes override val explanationRes: Int? = null,
            override val visible: (() -> Boolean)? = null,
            val checked: () -> Boolean,
            val onCheckedChange: (Boolean) -> Unit
        ) : Item

        @Immutable
        data class Custom(
            @DrawableRes override val iconRes: Int,
            @StringRes override val labelRes: Int,
            @StringRes override val explanationRes: Int? = null,
            override val visible: (() -> Boolean)? = null,
            val content: @Composable RowScope.() -> Unit
        ) : Item
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
            .thenIfNotNull(item as? NavigationDrawerSheetElement.Item.Clickable) {
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
                modifier = Modifier.padding(start = iconSize + labelStartPadding),
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
        modifier = modifier,
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

        when (item) {
            is NavigationDrawerSheetElement.Item.Custom -> {
                item.content(this)
            }

            is NavigationDrawerSheetElement.Item.Switch -> {
                RightAligned {
                    Switch(checked = item.checked(), onCheckedChange = item.onCheckedChange)
                }
            }

            else -> Unit
        }
    }
}
