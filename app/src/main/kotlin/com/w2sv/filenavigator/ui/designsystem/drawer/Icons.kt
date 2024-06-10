package com.w2sv.filenavigator.ui.designsystem.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.core.navigator.R
import com.w2sv.domain.model.FileType
import com.w2sv.filenavigator.ui.model.color

object IconSize {
    val Default = 24.dp
    val Big = 28.dp
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

//@Composable
//fun AutoMoveIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
//        CompositionLocalProvider(LocalContentColor provides tint) {
//            Icon(
//                painter = painterResource(id = R.drawable.ic_app_logo_24),
//                contentDescription = null
//            )
//            Text(text = "Auto", fontSize = 12.sp, lineHeight = 6.sp, fontWeight = FontWeight.Medium)
//        }
//    }
//}