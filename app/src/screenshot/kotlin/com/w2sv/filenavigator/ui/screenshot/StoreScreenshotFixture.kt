package com.w2sv.filenavigator.ui.screenshot

import android.content.Context
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.w2sv.common.uri.DocumentUri
import com.w2sv.common.uri.MediaUri
import com.w2sv.domain.model.MovedFile
import com.w2sv.domain.model.Theme
import com.w2sv.domain.model.filetype.FileAndSourceType
import com.w2sv.domain.model.filetype.PresetFileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.movedestination.MoveDestinationApi
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.usecase.MoveDestinationLabelProvider
import com.w2sv.filenavigator.BuildConfig
import com.w2sv.filenavigator.ui.screen.appsettings.model.AppPreferences
import com.w2sv.navigator.domain.moving.MediaStoreEntry
import com.w2sv.navigator.domain.moving.NavigatableFile
import java.io.File
import java.time.LocalDateTime

internal class StoreScreenshotFixture(context: Context) {

    val destinationLabelProvider = object : MoveDestinationLabelProvider {
        override fun invoke(moveDestination: MoveDestinationApi, context: Context): String =
            (moveDestination as? LocalDestinationApi)?.pathRepresentation(context, includeVolumeName = false)
                ?: moveDestination.uiRepresentation(context)
    }

    val navigatorConfig: NavigatorConfig = NavigatorConfig.default
        .updateAutoMoveConfig(
            fileType = PresetFileType.Image.toFileType(),
            sourceType = SourceType.Screenshot
        ) {
            AutoMoveConfig(enabled = true, destination = StoreDestination("/Pictures/Screenshots"))
        }
        .updateSourceTypeEnablement(
            fileType = PresetFileType.Image.toFileType(),
            sourceType = SourceType.OtherApp,
            enabled = false
        )
        .updateAutoMoveConfig(
            fileType = PresetFileType.Image.toFileType(),
            sourceType = SourceType.Download
        ) {
            AutoMoveConfig(enabled = true, destination = StoreDestination("/Pictures/Downloads"))
        }
        .updateAutoMoveConfig(
            fileType = PresetFileType.Image.toFileType(),
            sourceType = SourceType.Camera
        ) {
            AutoMoveConfig(enabled = true, destination = StoreDestination("/Pictures/Photos"))
        }

    val appPreferences = AppPreferences(
        showStorageVolumeNames = true,
        setShowStorageVolumeNames = {},
        theme = Theme.Light,
        setTheme = {},
        useAmoledBlackTheme = false,
        setUseAmoledBlackTheme = {},
        useDynamicColors = false,
        setUseDynamicColors = {}
    )

    val moveHistory = listOf(
        movedFile(
            context = context,
            fileName = "Screenshot_Trip_Plans.png",
            fileType = PresetFileType.Image,
            sourceType = SourceType.Screenshot,
            destination = StoreDestination("/Pictures/Screenshots"),
            minutesAgo = 4,
            autoMoved = true
        ),
        movedFile(
            context = context,
            fileName = "Travel_Tickets.pdf",
            fileType = PresetFileType.PDF,
            sourceType = SourceType.Download,
            destination = StoreDestination("/Documents/Tickets"),
            minutesAgo = 18,
            autoMoved = false
        ),
        movedFile(
            context = context,
            fileName = "FileNavigator-0.3.5.apk",
            fileType = PresetFileType.APK,
            sourceType = SourceType.Download,
            destination = StoreDestination("/APKs"),
            minutesAgo = 42,
            autoMoved = true
        )
    )

    val notifications = listOf(
        navigatableFile(
            fileName = "Boarding_Passes.pdf",
            fileType = PresetFileType.PDF,
            size = 842_000
        ),
        navigatableFile(
            fileName = "FileNavigator-0.3.5.apk",
            fileType = PresetFileType.APK,
            size = 8_640_000
        )
    )

    private fun movedFile(
        context: Context,
        fileName: String,
        fileType: PresetFileType,
        sourceType: SourceType,
        destination: StoreDestination,
        minutesAgo: Long,
        autoMoved: Boolean
    ): MovedFile.Local {
        // Move-history UI checks DocumentFile.exists(), so use real cache files while keeping fixture data disposable.
        val file = File(context.cacheDir, "store-screenshots/$fileName").apply {
            parentFile?.mkdirs()
            if (!exists()) {
                writeText("Store screenshot fixture")
            }
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.store-screenshots",
            file
        )

        return MovedFile.Local(
            documentUri = DocumentUri(uri),
            mediaUri = null,
            name = fileName,
            originalName = null,
            fileType = fileType.toFileType(),
            sourceType = sourceType,
            moveDestination = destination,
            moveDateTime = LocalDateTime.now().minusMinutes(minutesAgo),
            autoMoved = autoMoved
        )
    }

    // Notification rendering only needs MediaStore metadata; the synthetic URI is never opened for these non-preview file types.
    private fun navigatableFile(fileName: String, fileType: PresetFileType, size: Long): NavigatableFile =
        NavigatableFile(
            mediaUri = MediaUri.parse("content://media/external/file/${fileName.hashCode().toUInt()}"),
            mediaStoreEntry = MediaStoreEntry(
                rowId = fileName.hashCode().toUInt().toString(),
                absPath = "/storage/emulated/0/Download/$fileName",
                relativePath = "Download/",
                size = size,
                isPending = false,
                isTrashed = false
            ),
            fileAndSourceType = FileAndSourceType(
                fileType = fileType.toFileType(),
                sourceType = SourceType.Download
            )
        )
}

private data class StoreDestination(private val path: String) : LocalDestinationApi {
    override val documentUri = DocumentUri("content://store-screenshots$path".toUri())
    override val isVolumeRoot = false

    override fun fileName(context: Context): String =
        path.substringAfterLast("/")

    override fun uiRepresentation(context: Context): String =
        path

    override fun pathRepresentation(context: Context, includeVolumeName: Boolean): String =
        path
}
