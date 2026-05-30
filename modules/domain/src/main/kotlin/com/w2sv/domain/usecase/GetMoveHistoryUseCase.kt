package com.w2sv.domain.usecase

import androidx.annotation.ColorInt
import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.repository.MovedFileRepository
import com.w2sv.domain.repository.NavigatorConfigFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetMoveHistoryUseCase @Inject constructor(
    private val movedFileRepository: MovedFileRepository,
    private val navigatorConfigFlow: NavigatorConfigFlow
) {
    operator fun invoke(): Flow<List<MovedFile>> =
        combine(
            movedFileRepository.getAllInDescendingOrder(),
            navigatorConfigFlow
        ) { history, navigatorConfig ->
            history.map { movedFile ->
                val updatedColor = navigatorConfig.fileTypeConfigMap.values.firstOrNull { fileTypeConfig ->
                    fileTypeConfig.fileType.id == movedFile.fileType.id
                }
                    ?.fileType
                    ?.colorInt

                if (updatedColor == null) {
                    movedFile
                } else {
                    movedFile.fileTypeColorUpdated(updatedColor)
                }
            }
        }
}

private fun MovedFile.fileTypeColorUpdated(@ColorInt updatedColor: Int): MovedFile =
    when (this) {
        is MovedFile.Local -> copy(fileType = fileType.withColor(updatedColor))
        is MovedFile.External -> copy(fileType = fileType.withColor(updatedColor))
    }
