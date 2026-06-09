package com.w2sv.filenavigator.ui.screen.appsettings.model

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import com.w2sv.androidutils.content.openUrl
import com.w2sv.androidutils.content.packagePlayStoreUrl
import com.w2sv.androidutils.content.startActivity
import com.w2sv.androidutils.widget.showToast
import com.w2sv.filenavigator.AppUrl
import com.w2sv.modules.resources.R

@Immutable
data class SettingsActionGroup(@StringRes val titleRes: Int, val actions: List<SettingsAction>)

@Immutable
data class SettingsAction(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    @StringRes val explanationRes: Int? = null,
    val onClick: (Context) -> Unit
)

fun settingsActionGroups(): List<SettingsActionGroup> =
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
