package com.w2sv.filenavigator.ui.components.drawer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.generic.appPlayStoreUrl
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.RightAligned
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel

@Composable
internal fun ColumnScope.SheetContent(
    closeDrawer: () -> Unit,
    context: Context = LocalContext.current,
    appVM: AppViewModel = viewModel()
) {
    remember {
        buildList {
            add(Element.Header(R.string.appearance))
            add(Element.LabelledItem.Custom(R.drawable.ic_nightlight_24, R.string.theme) {
                ThemeSelectionRow(
                    selected = appVM.theme.collectAsState().value,
                    onSelected = appVM::saveTheme,
                    modifier = Modifier.padding(start = 22.dp)
                )
            })
            if (dynamicColorsSupported) {
                add(Element.LabelledItem.Custom(
                    iconRes = R.drawable.ic_palette_24,
                    labelRes = R.string.use_dynamic_colors,
                ) {
                    RightAligned {
                        Switch(
                            checked = appVM.useDynamicColors.collectAsState().value,
                            onCheckedChange = appVM::saveUseDynamicColors
                        )
                    }
                })
            }
            add(Element.Header(R.string.legal))
            add(Element.LabelledItem.Clickable(R.drawable.ic_policy_24, R.string.privacy_policy) {
                context.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/FileNavigator/blob/main/PRIVACY-POLICY.md")
            })
            add(Element.LabelledItem.Clickable(R.drawable.ic_copyright_24, R.string.license) {
                context.openUrlWithActivityNotFoundHandling("https://github.com/w2sv/FileNavigator/blob/main/LICENSE.md")
            })
            add(Element.Header(R.string.more))
            add(Element.LabelledItem.Clickable(R.drawable.ic_star_rate_24, R.string.rate) {
                try {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(appPlayStoreUrl(context))
                        ).setPackage("com.android.vending")
                    )
                } catch (e: ActivityNotFoundException) {
                    context.showToast(context.getString(R.string.you_re_not_signed_into_the_play_store))
                }
            })
            add(Element.LabelledItem.Clickable(R.drawable.ic_share_24, R.string.share) {
                ShareCompat.IntentBuilder(context)
                    .setType("text/plain")
                    .setText(context.getString(R.string.share_action_text))
                    .startChooser()
            })
            add(Element.LabelledItem.Clickable(R.drawable.ic_developer_24, R.string.developer) {
                context.openUrlWithActivityNotFoundHandling("https://play.google.com/store/apps/dev?id=6884111703871536890")
            })
        }
    }
        .forEach {
            when (it) {
                is Element.LabelledItem -> {
                    Item(item = it, closeDrawer = closeDrawer)
                }

                is Element.Header -> {
                    SubHeader(titleRes = it.titleRes)
                }

                is Element.Custom -> {
                    it.content()
                }
            }
        }
}

private sealed interface Element {

    interface LabelledItem : Element {
        val iconRes: Int
        val labelRes: Int

        data class Clickable(
            @DrawableRes override val iconRes: Int,
            @StringRes override val labelRes: Int,
            val onClick: () -> Unit
        ) : LabelledItem

        data class Custom(
            @DrawableRes override val iconRes: Int,
            @StringRes override val labelRes: Int,
            val content: @Composable RowScope.() -> Unit
        ) : LabelledItem
    }

    data class Custom(
        val content: @Composable () -> Unit
    ) : Element

    data class Header(
        @StringRes val titleRes: Int,
    ) : Element
}

@Composable
private fun ColumnScope.SubHeader(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
) {
    AppFontText(
        text = stringResource(id = titleRes),
        modifier = modifier
            .padding(vertical = 4.dp)
            .align(Alignment.CenterHorizontally),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
private fun Item(
    item: Element.LabelledItem,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (item is Element.LabelledItem.Clickable)
                    Modifier.clickable {
                        item.onClick()
                        closeDrawer()
                    }
                else
                    Modifier
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(size = 28.dp),
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        AppFontText(
            text = stringResource(id = item.labelRes),
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )

        if (item is Element.LabelledItem.Custom) {
            item.content(this)
        }
    }
}