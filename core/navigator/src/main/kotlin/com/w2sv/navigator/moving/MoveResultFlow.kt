package com.w2sv.navigator.moving

import com.w2sv.navigator.moving.model.MoveResult
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MoveResultFlow @Inject constructor() :
    MutableSharedFlow<MoveResult> by MutableSharedFlow()