package com.w2sv.filenavigator.ui.screens.main

import android.content.Context
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.filenavigator.datastore.AbstractDataStoreRepository
import com.w2sv.filenavigator.datastore.DataStoreRepository
import com.w2sv.filenavigator.service.FileListenerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    dataStoreRepository: DataStoreRepository
) : AbstractDataStoreRepository.InterfacingViewModel(dataStoreRepository) {

    val isListenerRunning: MutableStateFlow<Boolean> =
        MutableStateFlow(context.isServiceRunning<FileListenerService>())

    val accountForMediaType by lazy {
        makeNonAppliedSnapshotStateMap(dataStoreRepository.accountForMediaType)
    }

    val accountForMediaTypeOrigin by lazy {
        makeNonAppliedSnapshotStateMap(dataStoreRepository.accountForMediaTypeOrigin)
    }

    val nonAppliedListenerConfiguration by lazy {
        makeNonAppliedStatesComposition(accountForMediaTypeOrigin, accountForMediaTypeOrigin)
    }
}