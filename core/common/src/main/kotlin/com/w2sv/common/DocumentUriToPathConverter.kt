package com.w2sv.common

import android.content.Context
import android.net.Uri
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.getDocumentUriPath
import com.w2sv.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentUriToPathConverter @Inject constructor(
    preferencesRepository: PreferencesRepository,
    @GlobalScope(AppDispatcher.Default) scope: CoroutineScope
) {
    private val showStorageVolumeNames =
        preferencesRepository.showStorageVolumeNames.stateIn(scope, SharingStarted.Eagerly)

    operator fun invoke(documentUri: Uri, context: Context): String =
        getDocumentUriPath(documentUri, context)
            .run {
                if (showStorageVolumeNames.value) {
                    this
                } else {
                    substringAfter(":")
                }
            }
}