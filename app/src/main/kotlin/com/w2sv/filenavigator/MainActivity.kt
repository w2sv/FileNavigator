package com.w2sv.filenavigator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.w2sv.androidutils.lifecycle.SelfManagingLocalBroadcastReceiver
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.filenavigator.mediastore.MediaType
import com.w2sv.filenavigator.service.FileListenerService
import com.w2sv.filenavigator.ui.animateGridItemSpawn
import com.w2sv.filenavigator.ui.theme.FileNavigatorTheme
import com.w2sv.filenavigator.utils.getMutableStateMap
import com.w2sv.filenavigator.utils.toggle
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<HomeScreenViewModel>()

    class FileListenerServiceBroadcastReceiver(
        context: Context,
        callback: (Context?, Intent?) -> Unit
    ) : SelfManagingLocalBroadcastReceiver.Impl(
        context,
        IntentFilter()
            .apply {
                addAction(FileListenerService.ACTION_FILE_LISTENER_SERVICE_STARTED)
                addAction(FileListenerService.ACTION_FILE_LISTENER_SERVICE_STOPPED)
            },
        callback
    )

    private val fileListenerServiceBroadcastReceiver by lazy {
        FileListenerServiceBroadcastReceiver(this) { _, intent ->
            intent ?: return@FileListenerServiceBroadcastReceiver

            when (intent.action) {
                FileListenerService.ACTION_FILE_LISTENER_SERVICE_STARTED -> viewModel.isListenerRunning.value =
                    true

                FileListenerService.ACTION_FILE_LISTENER_SERVICE_STOPPED -> viewModel.isListenerRunning.value =
                    false

                else -> throw Error()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        lifecycle.addObserver(fileListenerServiceBroadcastReceiver)

        setContent {
            FileNavigatorTheme {
                HomeScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (!isServiceRunning<FileListenerService>()) {
            FileListenerService.start(this)
        }  // TODO: remove
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun HomeScreen() {
    val context = LocalContext.current

    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) {
            if (it) {
                FileListenerService.start(context)
            }
        }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(vertical = 42.dp, horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = stringResource(R.string.select_media_types),
                style = MaterialTheme.typography.headlineMedium
            )
            MediaTypeSelectionGrid()
            Divider(modifier = Modifier.padding(16.dp))
            ListenerButton(
                startListener = {
                    when (permissionState.status.isGranted) {
                        true -> FileListenerService.start(context)
                        false -> permissionState.launchPermissionRequest()
                    }
                },
                stopListener = { FileListenerService.stop(context) }
            )
        }
    }
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    val listenToMediaType = MediaType.values().associateWith { true }.getMutableStateMap()

    val isListenerRunning: MutableStateFlow<Boolean> =
        MutableStateFlow(context.isServiceRunning<FileListenerService>())
}

@Composable
fun ListenerButton(
    startListener: () -> Unit,
    stopListener: () -> Unit,
    modifier: Modifier = Modifier,
    homeScreenViewModel: HomeScreenViewModel = viewModel()
) {
    val isListenerRunning by homeScreenViewModel.isListenerRunning.collectAsState()

    ElevatedButton(
        onClick = if (isListenerRunning) stopListener else startListener,
        modifier = modifier
            .width(220.dp)
            .height(80.dp)
    ) {
        Text(
            text = stringResource(if (isListenerRunning) R.string.stop_listener else R.string.start_listener),
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )
    }
}

@Preview
@Composable
private fun MediaTypeSelectionGridPrev() {
    FileNavigatorTheme {
        MediaTypeSelectionGrid()
    }
}

@Composable
internal fun MediaTypeSelectionGrid(modifier: Modifier = Modifier) {
    val state = rememberLazyListState()
    val nColumns = 2

    LazyVerticalGrid(
        columns = GridCells.Fixed(nColumns),
        modifier = modifier.height(240.dp)
    ) {
        items(MediaType.values().size) {
            MediaTypeCard(
                mediaType = MediaType.values()[it],
                modifier = Modifier
                    .padding(8.dp)
                    .animateGridItemSpawn(it, nColumns, state)
            )
        }
    }
}

@Composable
internal fun MediaTypeCard(
    mediaType: MediaType,
    modifier: Modifier = Modifier,
    homeScreenViewModel: HomeScreenViewModel = viewModel()
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(id = mediaType.labelRes), fontSize = 18.sp)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = mediaType.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp)
                )
                Checkbox(
                    checked = homeScreenViewModel.listenToMediaType.getValue(mediaType),
                    onCheckedChange = { homeScreenViewModel.listenToMediaType.toggle(mediaType) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun MediaTypeCardPreview() {
    FileNavigatorTheme {
        MediaTypeCard(
            mediaType = MediaType.Image,
            modifier = Modifier.size(160.dp)
        )
    }
}