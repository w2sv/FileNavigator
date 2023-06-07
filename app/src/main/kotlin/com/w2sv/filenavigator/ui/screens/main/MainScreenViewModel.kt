package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import androidx.lifecycle.ViewModel
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.filenavigator.mediastore.MediaType
import com.w2sv.filenavigator.service.FileListenerService
import com.w2sv.filenavigator.utils.getMutableStateMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(@ApplicationContext context: Context) : ViewModel() {
    val listenToMediaType = MediaType.values()
        .associateWith { true }
        .getMutableStateMap()

    val isListenerRunning: MutableStateFlow<Boolean> =
        MutableStateFlow(context.isServiceRunning<FileListenerService>())

    val includeMediaTypeOrigin = MediaType.values()
        .map { it.originIdentifiers }
        .flatten()
        .associateWith { true }
        .getMutableStateMap()
}