package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.w2sv.composed.extensions.thenIfNotNull
import com.w2sv.core.common.R

@Composable
fun NavigationDrawerScreenTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigationIconClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                title
            )
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.open_navigation_drawer),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        windowInsets = WindowInsets.statusBarsIgnoringVisibility // Apply status bar insets also if status bar hidden during immersive mode
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    )
}

@Composable
fun TopAppBarAboveHorizontalDivider(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    modifier = Modifier.thenIfNotNull(navigationIcon) { padding(start = 14.dp) },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = navigationIcon ?: {},
            windowInsets = windowInsets,
            colors = colors
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun BackArrowTopAppBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBarAboveHorizontalDivider(
        title = title,
        modifier = modifier,
        navigationIcon = {
            FilledTonalIconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.return_to_main_screen)
                )
            }
        }
    )
}
