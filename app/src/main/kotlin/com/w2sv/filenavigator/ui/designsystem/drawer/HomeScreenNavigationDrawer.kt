package com.w2sv.filenavigator.ui.designsystem.drawer

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.core.common.R
import com.w2sv.filenavigator.BuildConfig
import com.w2sv.filenavigator.ui.designsystem.SystemBarsIgnoringVisibilityPaddedColumn
import com.w2sv.filenavigator.ui.designsystem.emptyWindowInsets
import com.w2sv.filenavigator.ui.util.PreviewOf
import java.time.LocalDate

@Composable
fun HomeScreenNavigationDrawer(
    state: DrawerState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        modifier = modifier,
        drawerContent = { Sheet(drawerState = state) },
        drawerState = state,
        content = content
    )
}

@Composable
private fun Sheet(drawerState: DrawerState, modifier: Modifier = Modifier) {
    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = modifier,
        windowInsets = emptyWindowInsets,
        drawerTonalElevation = 1.dp
    ) {
        SystemBarsIgnoringVisibilityPaddedColumn(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .padding(top = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            NavigationDrawerSheetItemColumn(
                closeDrawer = drawerState::close,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }
    }
}

private val horizontalPadding = 24.dp

@Composable
private fun Header(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        AppLogoWithCircularBackground()
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(id = R.string.version).format(BuildConfig.VERSION_NAME),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = remember { "© 2023 - ${LocalDate.now().year} | w2sv" })
    }
}

@Composable
private fun AppLogoWithCircularBackground(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(id = R.drawable.ic_app_foreground_108),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.inversePrimary,
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showSystemUi = true)
@Composable
private fun Prev() {
    PreviewOf {
        HomeScreenNavigationDrawer(rememberDrawerState(DrawerValue.Open)) { Scaffold { } }
    }
}
