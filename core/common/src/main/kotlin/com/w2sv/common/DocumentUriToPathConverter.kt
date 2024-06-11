package com.w2sv.common

import android.content.Context
import com.w2sv.common.di.AppDispatcher
import com.w2sv.common.di.GlobalScope
import com.w2sv.common.utils.DocumentUri
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

    operator fun invoke(documentUri: DocumentUri, context: Context): String =
        documentUri.documentFilePath(context)
            .run {
                if (showStorageVolumeNames.value) {
                    this
                } else {
                    substringAfter(":")
                }
            }
}