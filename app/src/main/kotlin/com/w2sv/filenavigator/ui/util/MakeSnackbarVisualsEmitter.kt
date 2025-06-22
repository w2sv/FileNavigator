package com.w2sv.filenavigator.ui.util

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

typealias MakeSnackbarVisuals = (Context) -> SnackbarVisuals

interface MakeSnackbarVisualsEmitter {
    val makeSnackbarVisuals: SharedFlow<MakeSnackbarVisuals>
    suspend fun emitMakeSnackbarVisuals(makeSnackbarVisuals: MakeSnackbarVisuals)
}

class MakeSnackbarVisualsEmitterImpl : MakeSnackbarVisualsEmitter {
    override val makeSnackbarVisuals: SharedFlow<MakeSnackbarVisuals> get() = _makeSnackbarVisuals.asSharedFlow()
    private val _makeSnackbarVisuals = MutableSharedFlow<MakeSnackbarVisuals>()

    override suspend fun emitMakeSnackbarVisuals(makeSnackbarVisuals: MakeSnackbarVisuals) {
        _makeSnackbarVisuals.emit(makeSnackbarVisuals)
    }
}
