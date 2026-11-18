package com.wakepoint.app.core.design

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wakepoint.app.R

/**
 * 공통 컴포넌트 골격.
 * 디자인 토큰은 모두 제거된 상태이며 Material 3 기본값으로 렌더링된다.
 * 새 디자인은 이 API 위에 스타일을 입힌다. 요구사항은 docs/DESIGN_SPEC.md 참조.
 */

@Composable
fun WakepointButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Button(onClick = onClick, enabled = enabled, modifier = modifier) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
        }
        Text(text = text)
    }
}

@Composable
fun WakepointSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
        }
        Text(text = text)
    }
}

@Composable
fun WakepointConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(text = confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = cancelText) }
        }
    )
}

@Composable
fun WakepointCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(text = text) },
        modifier = modifier
    )
}

@Composable
fun WakepointLogo(
    modifier: Modifier = Modifier,
    horizontal: Boolean = true
) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
}

@Composable
fun WakepointHeader(
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WakepointLogo()
        Spacer(modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

@Composable
fun WakepointTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    trailingIconContentDescription: String? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder) },
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null) }
        },
        trailingIcon = trailingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = trailingIconContentDescription,
                    modifier = if (onTrailingIconClick != null) {
                        Modifier.clickable(onClick = onTrailingIconClick)
                    } else {
                        Modifier
                    }
                )
            }
        }
    )
}

@Composable
fun SoundOptionRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = { Text(text = title) },
        supportingContent = subtitle?.let { { Text(text = it) } },
        leadingContent = { Icon(imageVector = icon, contentDescription = null) },
        trailingContent = trailing
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetHandle(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.BottomSheetDefaults.DragHandle()
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
    )
}

@Composable
fun MessageText(text: String, modifier: Modifier = Modifier, isError: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) MaterialTheme.colorScheme.error else Color.Unspecified,
        modifier = modifier
    )
}

@Composable
fun ButtonRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}
