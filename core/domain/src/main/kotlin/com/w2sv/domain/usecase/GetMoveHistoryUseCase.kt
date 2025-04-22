package com.w2sv.domain.usecase

import androidx.annotation.ColorInt
import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.model.filetype.AnyPresetWrappingFileType
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.PresetWrappingFileType
import com.w2sv.domain.repository.MovedFileRepository
import com.w2sv.domain.repository.NavigatorConfigDataSource
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetMoveHistoryUseCase @Inject constructor(
    private val movedFileRepository: MovedFileRepository,
    private val navigatorConfigDataSource: NavigatorConfigDataSource
) {
    operator fun invoke(): Flow<List<MovedFile>> =
        combine(
            movedFileRepository.getAllInDescendingOrder(),
            navigatorConfigDataSource.navigatorConfig
        ) { history, navigatorConfig ->
            history.map { movedFile ->
                val updatedColor = navigatorConfig.fileTypeConfigMap.keys.firstOrNull { configFileType ->
                    when (val movedFileType = movedFile.fileType) {
                        is CustomFileType -> configFileType.ordinal == movedFileType.ordinal
                        is AnyPresetWrappingFileType -> configFileType.wrappedPresetTypeOrNull == movedFileType.presetFileType
                    }
                }
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
        is MovedFile.Local -> copy(fileType = fileType.colorUpdated(updatedColor))
        is MovedFile.External -> copy(fileType = fileType.colorUpdated(updatedColor))
    }

private fun FileType.colorUpdated(@ColorInt updatedColor: Int): FileType =
    when (this) {
        is CustomFileType -> copy(colorInt = updatedColor)
        is PresetWrappingFileType.ExtensionConfigurable -> copy(colorInt = updatedColor)
        is PresetWrappingFileType.ExtensionSet -> copy(colorInt = updatedColor)
    }
