package com.w2sv.filenavigator.ui.designsystem

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

object AppCardProperties {
    val headerIconModifier = Modifier
        .padding(end = 12.dp)
        .size(28.dp)

    val elevation
        @Composable
        get() = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
}

@Composable
fun AppCard(
    title: String,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    headerIcon: (@Composable () -> Unit)? = null,
    trailingHeaderContent: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        elevation = AppCardProperties.elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = verticalArrangement
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                headerIcon?.invoke()
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium
                )
                trailingHeaderContent()
            }
            content()
        }
    }
}

@Composable
fun AppCardHeaderIcon(
    @DrawableRes painterRes: Int,
    modifier: Modifier = AppCardProperties.headerIconModifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Icon(
        painter = painterResource(painterRes),
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}
