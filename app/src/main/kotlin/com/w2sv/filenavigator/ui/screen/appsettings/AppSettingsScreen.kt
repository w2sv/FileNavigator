package com.w2sv.filenavigator.ui.screen.appsettings

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import com.w2sv.androidutils.content.openUrl
import com.w2sv.androidutils.content.packagePlayStoreUrl
import com.w2sv.androidutils.content.startActivity
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.androidutils.widget.showToast
import com.w2sv.common.AppUrl
import com.w2sv.composed.core.rememberStyledTextResource
import com.w2sv.designsystem.theme.onSurfaceVariantDecreasedAlpha
import com.w2sv.domain.model.Theme
import com.w2sv.filenavigator.BuildConfig
import com.w2sv.filenavigator.ui.designsystem.AppCard
import com.w2sv.filenavigator.ui.designsystem.IconSize
import com.w2sv.filenavigator.ui.designsystem.ItemRow
import com.w2sv.filenavigator.ui.designsystem.ItemRowIcon
import com.w2sv.filenavigator.ui.designsystem.ItemRowTokens
import com.w2sv.filenavigator.ui.designsystem.PaddingDefaults
import com.w2sv.filenavigator.ui.designsystem.SwitchItemRow
import com.w2sv.filenavigator.ui.util.PreviewOf
import com.w2sv.filenavigator.ui.util.ScreenPreviews
import com.w2sv.filenavigator.ui.util.useDarkTheme
import com.w2sv.modules.common.R
import java.time.LocalDate

@Composable
fun AppSettingsScreen(appPreferences: AppPreferences = rememberAppPreferences()) {
    SettingsCardColumn(
        appPreferences = appPreferences,
        modifier = Modifier.fillMaxSize()
    )
}

private object AppSettingsScreenDimens {
    val verticalArrangement = Arrangement.spacedBy(16.dp)

    val contentPadding
        @Composable
        @ReadOnlyComposable
        get() = PaddingValues(
            horizontal = PaddingDefaults.horizontal,
            vertical = 18.dp
        )

    val cardActionsVerticalArrangement = Arrangement.spacedBy(20.dp)
}

@ScreenPreviews
@Composable
private fun Prev() {
    PreviewOf {
        AppSettingsScreen(
            appPreferences = AppPreferences(
                showStorageVolumeNames = { true },
                setShowStorageVolumeNames = {},
                theme = { Theme.Default },
                setTheme = {},
                useAmoledBlackTheme = { false },
                setUseAmoledBlackTheme = {},
                useDynamicColors = { true },
                setUseDynamicColors = {}
            )
        )
    }
}

@Composable
private fun SettingsCardColumn(appPreferences: AppPreferences, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val actionGroups = rememberSettingsActionGroups()

    LazyColumn(
        modifier = modifier,
        verticalArrangement = AppSettingsScreenDimens.verticalArrangement,
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = AppSettingsScreenDimens.contentPadding
    ) {
        item { GeneralSettingsCard(appPreferences = appPreferences) }
        item { AppearanceSettingsCard(appPreferences = appPreferences) }
        items(actionGroups, key = { it.titleRes }) {
            SettingsActionCard(
                group = it,
                context = context
            )
        }
        item {
            Text(
                text = stringResource(id = R.string.version, BuildConfig.VERSION_NAME),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { context.openUrl(AppUrl.RELEASES) }
            )
            Text(text = remember { "© 2023 - ${LocalDate.now().year} | w2sv" })
        }
    }
}

@Composable
private fun GeneralSettingsCard(appPreferences: AppPreferences, modifier: Modifier = Modifier) {
    AppCard(
        title = stringResource(R.string.general),
        modifier = modifier
    ) {
        SwitchItemRow(
            icon = { AppSettingsItemRowIcon(res = R.drawable.ic_storage_24) },
            labelRes = R.string.show_storage_volume_names,
            checked = appPreferences.showStorageVolumeNames(),
            onCheckedChange = appPreferences.setShowStorageVolumeNames,
            explanation = rememberStyledTextResource(R.string.show_storage_volume_names_explanation)
        )
    }
}

@Composable
private fun AppearanceSettingsCard(appPreferences: AppPreferences, modifier: Modifier = Modifier) {
    AppCard(
        title = stringResource(id = R.string.appearance),
        modifier = modifier
    ) {
        Column(verticalArrangement = AppSettingsScreenDimens.cardActionsVerticalArrangement, modifier = Modifier.animateContentSize()) {
            ThemeRow(appPreferences = appPreferences)
            DynamicColorsRow(appPreferences = appPreferences)
            // Leave at the end for Modifier.animateContentSize() to animate it in and out.
            AmoledBlackRow(appPreferences = appPreferences)
        }
    }
}

@Composable
private fun ThemeRow(appPreferences: AppPreferences) {
    ItemRow(
        icon = { AppSettingsItemRowIcon(res = R.drawable.ic_nightlight_24) },
        labelRes = R.string.theme
    ) {
        ThemeSelectionRow(
            selected = appPreferences.theme(),
            onSelected = appPreferences.setTheme,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        )
    }
}

@Composable
private fun DynamicColorsRow(appPreferences: AppPreferences) {
    if (dynamicColorsSupported) {
        SwitchItemRow(
            icon = { AppSettingsItemRowIcon(res = R.drawable.ic_palette_24) },
            labelRes = R.string.dynamic_colors,
            checked = appPreferences.useDynamicColors(),
            onCheckedChange = appPreferences.setUseDynamicColors,
            explanation = stringResource(id = R.string.use_colors_derived_from_your_wallpaper)
        )
    }
}

@Composable
private fun AmoledBlackRow(appPreferences: AppPreferences) {
    if (useDarkTheme(appPreferences.theme())) {
        SwitchItemRow(
            icon = { AppSettingsItemRowIcon(res = R.drawable.ic_contrast_24) },
            labelRes = R.string.amoled_black,
            checked = appPreferences.useAmoledBlackTheme(),
            onCheckedChange = appPreferences.setUseAmoledBlackTheme,
            explanation = stringResource(R.string.amoled_black_explanation)
        )
    }
}

@Composable
private fun SettingsActionCard(group: SettingsActionGroup, context: Context, modifier: Modifier = Modifier) {
    AppCard(
        title = stringResource(group.titleRes),
        modifier = modifier
    ) {
        Column(verticalArrangement = AppSettingsScreenDimens.cardActionsVerticalArrangement) {
            group.actions.forEach {
                SettingsActionRow(
                    action = it,
                    context = context,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AppSettingsItemRowIcon(@DrawableRes res: Int) {
    ItemRowIcon(
        res = res,
        tint = colorScheme.primary,
        modifier = Modifier.size(IconSize.Big)
    )
}

@Immutable
private data class SettingsActionGroup(@StringRes val titleRes: Int, val actions: List<SettingsAction>)

@Immutable
private data class SettingsAction(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    @StringRes val explanationRes: Int? = null,
    val onClick: (Context) -> Unit
)

@Composable
private fun rememberSettingsActionGroups(): List<SettingsActionGroup> =
    remember {
        listOf(
            SettingsActionGroup(
                titleRes = R.string.legal,
                actions = listOf(
                    SettingsAction(
                        iconRes = R.drawable.ic_policy_24,
                        labelRes = R.string.privacy_policy,
                        onClick = { it.openUrl(AppUrl.PRIVACY_POLICY) }
                    ),
                    SettingsAction(
                        iconRes = R.drawable.ic_copyright_24,
                        labelRes = R.string.license,
                        onClick = { it.openUrl(AppUrl.LICENSE) }
                    )
                )
            ),
            SettingsActionGroup(
                titleRes = R.string.support_the_app,
                actions = listOf(
                    SettingsAction(
                        iconRes = R.drawable.ic_star_rate_24,
                        labelRes = R.string.rate,
                        explanationRes = R.string.rate_the_app_in_the_playstore,
                        onClick = {
                            it.startActivity(
                                intent = Intent(
                                    Intent.ACTION_VIEW,
                                    it.packagePlayStoreUrl.toUri()
                                )
                                    .setPackage("com.android.vending"),
                                onActivityNotFoundException = {
                                    it.showToast(R.string.you_re_not_signed_into_the_play_store)
                                }
                            )
                        }
                    ),
                    SettingsAction(
                        iconRes = R.drawable.ic_share_24,
                        labelRes = R.string.share,
                        explanationRes = R.string.share_action_explanation,
                        onClick = {
                            ShareCompat.IntentBuilder(it)
                                .setType("text/plain")
                                .setText(it.getString(R.string.share_action_text, AppUrl.PLAYSTORE_LISTING))
                                .startChooser()
                        }
                    ),
                    SettingsAction(
                        iconRes = R.drawable.ic_bug_report_24,
                        explanationRes = R.string.report_a_bug_request_a_feature_explanation,
                        labelRes = R.string.report_a_bug_request_a_feature,
                        onClick = { it.openUrl(AppUrl.CREATE_ISSUE) }
                    ),
                    SettingsAction(
                        iconRes = R.drawable.ic_donate_24,
                        labelRes = R.string.support_the_development,
                        explanationRes = R.string.buy_me_a_coffee_as_a_sign_of_gratitude,
                        onClick = { it.openUrl(AppUrl.DONATE) }
                    )
                )
            ),
            SettingsActionGroup(
                titleRes = R.string.more,
                actions = listOf(
                    SettingsAction(
                        iconRes = R.drawable.ic_developer_24,
                        labelRes = R.string.about_the_developer,
                        explanationRes = R.string.check_out_my_other_apps,
                        onClick = { it.openUrl(AppUrl.GOOGLE_PLAY_DEVELOPER_PAGE) }
                    ),
                    SettingsAction(
                        iconRes = R.drawable.ic_github_24,
                        labelRes = R.string.source,
                        explanationRes = R.string.examine_the_app_s_source_code_on_github,
                        onClick = { it.openUrl(AppUrl.GITHUB_REPOSITORY) }
                    )
                )
            )
        )
    }

@Composable
private fun SettingsActionRow(action: SettingsAction, context: Context, modifier: Modifier = Modifier) {
    ConstraintLayout(modifier = modifier.clickable { action.onClick(context) }) {
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
                    linkTo(label.start, parent.end)
                    width = Dimension.fillToConstraints
                    top.linkTo(label.bottom)
                }
            )
        }
    }
}
