package com.w2sv.filenavigator.ui.sharedviewmodels

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anggrayudi.storage.file.child
import com.w2sv.domain.model.MoveEntry
import com.w2sv.domain.repository.MoveEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.annotation.concurrent.Immutable
import javax.inject.Inject

@HiltViewModel
class MoveHistoryViewModel @Inject constructor(private val moveEntryRepository: MoveEntryRepository) :
    ViewModel() {

    val moveHistory = moveEntryRepository
        .getAllInDescendingOrder()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun launchHistoryDeletion(): Job =
        viewModelScope.launch(Dispatchers.IO) {
            moveEntryRepository.deleteAll()
        }

    fun launchEntryDeletion(entry: MoveEntry): Job =
        viewModelScope.launch(Dispatchers.IO) {
            moveEntryRepository.delete(entry)
        }

    fun launchFileRetrieval(
        moveEntry: MoveEntry,
        context: Context,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _fileRetrievalResult.emit(
                    when (val mediaUri = moveEntry.getMediaUri(context)) {
                        null -> FileRetrievalResult.CouldntFindFile(moveEntry)
                        else -> FileRetrievalResult.Success(moveEntry, mediaUri)
                    }
                )
            }
        }
    }

    val fileRetrievalResult: SharedFlow<FileRetrievalResult> get() = _fileRetrievalResult.asSharedFlow()
    private val _fileRetrievalResult = MutableSharedFlow<FileRetrievalResult>()
}

@Immutable
sealed interface FileRetrievalResult {
    val moveEntry: MoveEntry

    @Immutable
    data class Success(override val moveEntry: MoveEntry, val mediaUri: Uri) : FileRetrievalResult

    @Immutable
    data class CouldntFindFile(override val moveEntry: MoveEntry) : FileRetrievalResult
}

//TODO: test
private fun MoveEntry.getMediaUri(context: Context): Uri? {
    val documentFile = DocumentFile
        .fromSingleUri(context, destinationDocumentUri)
        ?.child(context, fileName, false)
        ?: return null

    return MediaStore.getMediaUri(
        context,
        documentFile.uri
    )
}