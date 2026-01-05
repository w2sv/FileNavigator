package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.filenavigator.ui.modelext.color

object IconSize {
    val Default = 24.dp
    val Big = 28.dp

    object IconButton {
        val Smaller = 36.dp
    }
}

@Composable
fun FileTypeIcon(fileType: FileType, modifier: Modifier = Modifier, tint: Color = fileType.color) {
    Icon(
        painter = painterResource(id = fileType.iconRes),
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}

@Composable
fun SubDirectoryIcon(modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Icon(
        painter = painterResource(id = R.drawable.ic_subdirectory_arrow_right_24),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}
