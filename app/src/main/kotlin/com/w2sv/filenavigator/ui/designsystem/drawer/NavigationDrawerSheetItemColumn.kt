package com.w2sv.filenavigator.ui.designsystem.drawer

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.w2sv.androidutils.generic.appPlayStoreUrl
import com.w2sv.androidutils.generic.dynamicColorsSupported
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.AppUrl
import com.w2sv.common.utils.startActivityWithActivityNotFoundExceptionHandling
import com.w2sv.composed.OnChange
import com.w2sv.composed.extensions.thenIfNotNull
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.RightAligned
import com.w2sv.filenavigator.ui.designsystem.drawer.model.NavigationDrawerItemState
import com.w2sv.filenavigator.ui.theme.onSurfaceVariantDecreasedAlpha
import com.w2sv.filenavigator.ui.utils.LocalUseDarkTheme
import com.w2sv.filenavigator.ui.utils.OptionalAnimatedVisibility

@Composable
internal fun NavigationDrawerSheetItemColumn(
    itemState: NavigationDrawerItemState,
    modifier: Modifier = Modifier,
    useDarkTheme: Boolean = LocalUseDarkTheme.current,
    context: Context = LocalContext.current
) {
    var useDarkThemeLocal by remember {
        mutableStateOf(useDarkTheme)
    }
    OnChange(value = useDarkTheme) {
        useDarkThemeLocal = useDarkTheme
    }

    Column(modifier = modifier) {
        remember {
            listOf(
                NavigationDrawerSheetElement.Header(
                    titleRes = R.string.appearance,
                    modifier = Modifier
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_nightlight_24,
                    labelRes = R.string.theme,
                    type = NavigationDrawerSheetElement.Item.Type.Custom {
                        ThemeSelectionRow(
                            selected = itemState.theme(),
                            onSelected = itemState.setTheme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 22.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        )
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_contrast_24,
                    labelRes = R.string.amoled_black,
                    visible = { useDarkThemeLocal },
                    type = NavigationDrawerSheetElement.Item.Type.Switch(
                        checked = itemState.useAmoledBlackTheme,
                        onCheckedChange = itemState.setUseAmoledBlackTheme
                    )
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_palette_24,
                    labelRes = R.string.dynamic_colors,
                    explanationRes = R.string.use_colors_derived_from_your_wallpaper,
                    visible = {
                        dynamicColorsSupported
                    },
                    type = NavigationDrawerSheetElement.Item.Type.Switch(
                        checked = itemState.useDynamicColors,
                        onCheckedChange = itemState.setUseDynamicColors
                    )
                ),
                NavigationDrawerSheetElement.Header(
                    titleRes = R.string.legal
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_policy_24,
                    labelRes = R.string.privacy_policy,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.PRIVACY_POLICY)
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_copyright_24,
                    labelRes = R.string.license,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.LICENSE)
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
                                Uri.parse(appPlayStoreUrl(context))
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
                        context.openUrlWithActivityNotFoundHandling(AppUrl.CREATE_ISSUE)
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_donate_24,
                    labelRes = R.string.support_development,
                    explanationRes = R.string.buy_me_a_coffee_as_a_sign_of_gratitude,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.DONATE)
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
                        context.openUrlWithActivityNotFoundHandling(AppUrl.GOOGLE_PLAY_DEVELOPER_PAGE)
                    }
                ),
                NavigationDrawerSheetElement.Item(
                    iconRes = R.drawable.ic_github_24,
                    labelRes = R.string.source,
                    explanationRes = R.string.examine_the_app_s_source_code_on_github,
                    type = NavigationDrawerSheetElement.Item.Type.Clickable {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.GITHUB_REPOSITORY)
                    }
                )
            )
        }
            .forEach { element ->
                when (element) {
                    is NavigationDrawerSheetElement.Item -> {
                        OptionalAnimatedVisibility(visible = element.visible) {
                            Item(
                                item = element,
                                modifier = element.modifier,
                            )
                        }
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
        val visible: (() -> Boolean)? = null,
        override val modifier: Modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        val type: Type
    ) : NavigationDrawerSheetElement {

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
