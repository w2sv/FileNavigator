package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.w2sv.filenavigator.R

@Stable
interface DropdownMenuScope : ColumnScope {
    fun collapseMenu()
}

@Composable
fun MoreIconButtonWithDropdownMenu(modifier: Modifier = Modifier, dropdownMenuContent: @Composable (DropdownMenuScope.() -> Unit)) {
    var menuIsExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    Box(modifier = modifier) {
        IconButton(onClick = { menuIsExpanded = !menuIsExpanded }, modifier = Modifier.size(IconSize.IconButton.Smaller)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_vert_24),
                contentDescription = stringResource(R.string.open_the_dropdown_menu)
            )
        }
        DropdownMenu(
            expanded = menuIsExpanded,
            onDismissRequest = { menuIsExpanded = false },
            modifier = Modifier.border(
                width = Dp.Hairline,
                color = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.extraSmall
            ),
            content = {
                val scope = remember {
                    object : DropdownMenuScope, ColumnScope by this {
                        override fun collapseMenu() {
                            menuIsExpanded = false
                        }
                    }
                }
                with(scope) { dropdownMenuContent() }
            }
        )
    }
}
