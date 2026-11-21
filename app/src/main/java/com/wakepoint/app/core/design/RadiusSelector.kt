package com.wakepoint.app.core.design

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * 반경 선택 — `ExposedDropdownMenuBox`.
 * 필드는 `WakepointTextField` 와 같은 모양 + 아래 화살표 (DESIGN.md 5절).
 *
 * @param filled 시트·카드 안이면 true(채움 필드), 화면 배경 위면 false(테두리 필드).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadiusSelector(
    options: List<String>,
    selectedOption: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "반경",
    filled: Boolean = true
) {
    val colors = LocalWakepointColors.current
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        WakepointTextField(
            value = selectedOption,
            onValueChange = {},
            placeholder = label,
            readOnly = true,
            filled = filled,
            trailingIcon = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
            modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = colors.surfaceElevated,
            shape = WakepointShape.message,
            shadowElevation = androidx.compose.ui.unit.Dp.Hairline,
            tonalElevation = androidx.compose.ui.unit.Dp.Hairline
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (option == selectedOption) colors.brand else colors.text
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = colors.text,
                        leadingIconColor = colors.textSecondary,
                        trailingIconColor = colors.textSecondary
                    ),
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
