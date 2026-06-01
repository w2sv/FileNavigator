package com.w2sv.domain.usecase

import com.w2sv.domain.model.MovedFile
import kotlinx.coroutines.flow.Flow

/**
 * Provides the move history shown to users.
 *
 * Entries reflect the current navigator configuration where applicable, such as updated
 * file type metadata for previously recorded moves.
 */
interface GetMoveHistoryUseCase {
    operator fun invoke(): Flow<List<MovedFile>>
}
