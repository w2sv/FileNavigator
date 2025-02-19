package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.util.ClearFocusOnFlowEmissionOrKeyboardHidden
import com.w2sv.kotlinutils.coroutines.flow.emit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private enum class FileExtensionInvalidityReason(val errorMessage: String) {
    ContainsSpecialCharacter("Extension must not contain special characters"),
    AlreadyAmongstFileExtensions("Already amongst added extensions")
}

@Stable
private class CustomFileType(private val scope: CoroutineScope) {
    var name by mutableStateOf("")
        private set

    fun updateName(value: String) {
        name = value.trim().replaceFirstChar(Char::titlecase)
    }

    val fileExtensions = mutableStateListOf<String>()

    var newFileExtension by mutableStateOf("")
        private set

    fun updateNewFileExtension(value: String) {
        newFileExtension = value.trim().lowercase()
    }

    val newFileExtensionInvalidityReason by derivedStateOf {
        when {
            newFileExtension.any { !it.isLetterOrDigit() } -> FileExtensionInvalidityReason.ContainsSpecialCharacter
            fileExtensions.contains(newFileExtension) -> FileExtensionInvalidityReason.AlreadyAmongstFileExtensions
            else -> null
        }
    }
    val newFileExtensionCanBeAdded by derivedStateOf { newFileExtensionInvalidityReason == null && newFileExtension.isNotBlank() }

    fun addNewFileExtension() {
        fileExtensions.add(newFileExtension)
        newFileExtension = ""
    }

    val canBeCreated by derivedStateOf { name.isNotEmpty() && fileExtensions.isNotEmpty() }

    val clearFocus get() = _clearFocus.asSharedFlow()
    private val _clearFocus = MutableSharedFlow<Unit>()

    fun clearFocus() {
        _clearFocus.emit(Unit, scope)
    }
}

@Composable
fun FileTypeCreationDialog(onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val customFileType = remember { CustomFileType(scope) }  // TODO: rememberSavable

    StatelessFileTypeCreationDialog(customFileType, onDismissRequest, modifier)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatelessFileTypeCreationDialog(customFileType: CustomFileType, onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        modifier = modifier.pointerInput(Unit) { detectTapGestures { customFileType.clearFocus() } },
        title = { Text("Create a file type") },
        onDismissRequest = onDismissRequest,
        text = {
            ClearFocusOnFlowEmissionOrKeyboardHidden(customFileType.clearFocus)

            Column {
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = customFileType.name,
                    onValueChange = customFileType::updateName,
                    placeholder = { Text("Enter name") },
                    singleLine = true
                )
//                Text(
//                    text = "File Extensions",
//                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
//                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
//                )
//                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 32.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//                    items(customFileType.fileExtensions) {
//                        if (it.isNotEmpty()) {
//                            Badge {
//                                Text(it)
//                            }
//                        }
//                    }
//                }
                FileExtensionTextField(
                    customFileType = customFileType,
                    modifier = Modifier
                        .width(192.dp)
                        .padding(vertical = 16.dp)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    customFileType.fileExtensions.forEachIndexed { i, el ->
                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                text = el,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { DialogButton("Create", onClick = {}, enabled = customFileType.canBeCreated) },
    )
}

@Composable
private fun FileExtensionTextField(customFileType: CustomFileType, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = customFileType.newFileExtension,
        onValueChange = customFileType::updateNewFileExtension,
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = { Text("Enter file extension", maxLines = 1) },
        singleLine = true,
        modifier = modifier,
        trailingIcon = when {
            customFileType.newFileExtensionCanBeAdded -> {
                {
                    IconButton(onClick = customFileType::addNewFileExtension) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AppColor.success)
                    }
                }
            }

            customFileType.newFileExtensionInvalidityReason != null -> {
                {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            }

            else -> {
                null
            }
        },
        isError = customFileType.newFileExtensionInvalidityReason != null,
        supportingText = customFileType.newFileExtensionInvalidityReason?.let { invalidityReason ->
            {
                Text(
                    text = invalidityReason.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    )
}

@Preview
@Composable
private fun StatelessFileTypeCreationDialogPrev() {
    AppTheme {
        StatelessFileTypeCreationDialog(
            CustomFileType(rememberCoroutineScope()).apply { fileExtensions.addAll(listOf("jpg", "png", "jpg", "jpg", "jpgasdf")) },
            {}
        )
    }
}
