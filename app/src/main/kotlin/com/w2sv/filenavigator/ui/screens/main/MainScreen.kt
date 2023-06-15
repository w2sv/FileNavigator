package com.w2sv.filenavigator.ui.screens.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.AppSnackbar
import com.w2sv.filenavigator.ui.RailwayText

@Composable
fun MainScreen(mainScreenViewModel: MainScreenViewModel = viewModel()) {
    val context = LocalContext.current

    var showSettingsDialog by rememberSaveable {
        mutableStateOf(false)
    }
        .apply {
            if (value) {
                SettingsDialog(closeDialog = { value = false })
            }
        }

    Scaffold(snackbarHost = {
        SnackbarHost(mainScreenViewModel.snackbarHostState) { snackbarData ->
            AppSnackbar(snackbarData = snackbarData)
        }
    }) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Spacer(modifier = Modifier.weight(0.05f))
                Box(modifier = Modifier.weight(0.15f), contentAlignment = Alignment.CenterStart) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RailwayText(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        SettingsDialogButton(onClick = { showSettingsDialog = true })
                    }
                }

                Box(modifier = Modifier.weight(0.7f), contentAlignment = Alignment.Center) {
                    FileTypeSelectionColumn(Modifier.fillMaxHeight())
                }

                Box(modifier = Modifier.weight(0.25f), contentAlignment = Alignment.Center) {
                    val unconfirmedConfigurationChangesPresent by mainScreenViewModel.unconfirmedNavigatorConfiguration.statesDissimilar.collectAsState()

                    this@Column.AnimatedVisibility(
                        visible = !unconfirmedConfigurationChangesPresent,
                        enter = fadeIn() + slideInHorizontally(),
                        exit = fadeOut() + slideOutHorizontally()
                    ) {
                        StartNavigatorButton(
                            modifier = Modifier
                                .width(220.dp)
                                .height(70.dp)
                        )
                    }

                    this@Column.AnimatedVisibility(
                        visible = unconfirmedConfigurationChangesPresent,
                        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 }),
                        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 })
                    ) {
                        NavigatorConfigurationButtons()
                    }
                }
            }
        }
    }

    EventualManageExternalStorageRational()

    BackHandler {
        mainScreenViewModel.onBackPress(context)
    }
}