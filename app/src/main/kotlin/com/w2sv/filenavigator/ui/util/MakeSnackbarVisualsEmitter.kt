package com.w2sv.filenavigator.ui.util

import android.content.Context
import androidx.compose.material3.SnackbarVisuals
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

typealias MakeSnackbarVisuals = (Context) -> SnackbarVisuals

interface MakeSnackbarVisualsEmitter {
    val makeSnackbarVisuals: SharedFlow<MakeSnackbarVisuals>
    suspend fun emitMakeSnackbarVisuals(makeSnackbarVisuals: MakeSnackbarVisuals)
}

class MakeSnackbarVisualsEmitterImpl : MakeSnackbarVisualsEmitter {
    final override val makeSnackbarVisuals: SharedFlow<MakeSnackbarVisuals>
        field = MutableSharedFlow<MakeSnackbarVisuals>()

    override suspend fun emitMakeSnackbarVisuals(makeSnackbarVisuals: MakeSnackbarVisuals) {
        this.makeSnackbarVisuals.emit(makeSnackbarVisuals)
    }
}
