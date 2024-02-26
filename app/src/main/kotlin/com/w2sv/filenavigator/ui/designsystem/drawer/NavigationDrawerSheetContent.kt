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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.androidutils.generic.appPlayStoreUrl
import com.w2sv.androidutils.generic.openUrlWithActivityNotFoundHandling
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.common.utils.dynamicColorsSupported
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.RightAligned
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel

private object AppUrl {
    const val LICENSE = "https://github.com/w2sv/FileNavigator/blob/main/LICENSE"
    const val PRIVACY_POLICY = "https://github.com/w2sv/FileNavigator/blob/main/PRIVACY-POLICY.md"
    const val GITHUB_REPOSITORY = "https://github.com/w2sv/FileNavigator"
    const val CREATE_ISSUE = "https://github.com/w2sv/FileNavigator/issues/new"
    const val GOOGLE_PLAY_DEVELOPER_PAGE =
        "https://play.google.com/store/apps/dev?id=6884111703871536890"
}

@Composable
internal fun NavigationDrawerSheetContent(
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    appVM: AppViewModel = viewModel()
) {
    Column(modifier = modifier) {
        val context: Context = LocalContext.current

        remember {
            buildList {
                add(Element.Header(R.string.appearance))
                add(Element.LabelledItem.Custom(R.drawable.ic_nightlight_24, R.string.theme) {
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
                    add(Element.LabelledItem.Custom(
                        iconRes = R.drawable.ic_palette_24,
                        labelRes = R.string.use_dynamic_colors,
                    ) {
                        RightAligned {
                            Switch(
                                checked = appVM.useDynamicColors.collectAsStateWithLifecycle().value,
                                onCheckedChange = appVM::saveUseDynamicColors
                            )
                        }
                    })
                }
                add(Element.Header(R.string.legal))
                add(
                    Element.LabelledItem.Clickable(
                        R.drawable.ic_policy_24,
                        R.string.privacy_policy
                    ) {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.PRIVACY_POLICY)
                    })
                add(Element.LabelledItem.Clickable(R.drawable.ic_copyright_24, R.string.license) {
                    context.openUrlWithActivityNotFoundHandling(AppUrl.LICENSE)
                })
                add(Element.Header(R.string.support_the_app))
                add(Element.LabelledItem.Clickable(R.drawable.ic_star_rate_24, R.string.rate) {
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
                add(Element.LabelledItem.Clickable(R.drawable.ic_share_24, R.string.share) {
                    ShareCompat.IntentBuilder(context)
                        .setType("text/plain")
                        .setText(context.getString(R.string.share_action_text))
                        .startChooser()
                })
                add(
                    Element.LabelledItem.Clickable(
                        R.drawable.ic_bug_report_24,
                        R.string.report_a_bug
                    ) {
                        context.openUrlWithActivityNotFoundHandling(AppUrl.CREATE_ISSUE)
                    })
                add(Element.Header(R.string.more))
                add(Element.LabelledItem.Clickable(R.drawable.ic_developer_24, R.string.developer) {
                    context.openUrlWithActivityNotFoundHandling(AppUrl.GOOGLE_PLAY_DEVELOPER_PAGE)
                })
                add(Element.LabelledItem.Clickable(R.drawable.ic_github_24, R.string.source) {
                    context.openUrlWithActivityNotFoundHandling(AppUrl.GITHUB_REPOSITORY)
                })
            }
        }
            .forEach {
                when (it) {
                    is Element.LabelledItem -> {
                        LabelledItem(
                            item = it,
                            closeDrawer = closeDrawer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                        )
                    }

                    is Element.Header -> {
                        SubHeader(
                            titleRes = it.titleRes,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    is Element.Custom -> {
                        it.content()
                    }
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
private fun LabelledItem(
    item: Element.LabelledItem,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .then(
                if (item is Element.LabelledItem.Clickable)
                    Modifier.clickable {
                        item.onClick()
                        closeDrawer()
                    }
                else
                    Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(size = 28.dp),
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
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
