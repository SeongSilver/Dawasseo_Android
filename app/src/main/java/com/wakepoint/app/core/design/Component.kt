package com.wakepoint.app.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wakepoint.app.R

/** Full-width primary CTA — blue, 높이 52 · 라운드 12 · 텍스트 15/SemiBold. */
@Composable
fun WakepointButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(WakepointSize.buttonHeight),
        shape = WakepointShape.control,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = WakepointSpacing.cardPadding),
        colors = ButtonDefaults.buttonColors(
            containerColor = WakepointPrimary,
            contentColor = Color.White,
            disabledContainerColor = WakepointPrimary.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.8f)
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(WakepointSpacing.iconTextGap))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Neutral / secondary button — Field 회색 채움, ink 텍스트, 높이 52 · 라운드 12. */
@Composable
fun WakepointSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(WakepointSize.buttonHeight),
        shape = WakepointShape.control,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = WakepointSpacing.cardPadding),
        colors = ButtonDefaults.buttonColors(
            containerColor = WakepointField,
            contentColor = WakepointInk,
            disabledContainerColor = WakepointField.copy(alpha = 0.5f),
            disabledContentColor = WakepointDisabledText
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(WakepointSpacing.iconTextGap))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Destructive tonal action — Danger Light 채움 + Danger 텍스트, 높이 52 · 라운드 12. */
@Composable
fun WakepointDangerButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(WakepointSize.buttonHeight),
        shape = WakepointShape.control,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = WakepointSpacing.cardPadding),
        colors = ButtonDefaults.buttonColors(
            containerColor = WakepointDangerLight,
            contentColor = WakepointDanger,
            disabledContainerColor = WakepointField.copy(alpha = 0.5f),
            disabledContentColor = WakepointDisabledText
        )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(WakepointSpacing.iconTextGap))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Tinted-blue secondary action (e.g. map "알람 수정하기"). 높이 52 · 라운드 12. */
@Composable
fun WakepointTintButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(WakepointSize.buttonHeight),
        shape = WakepointShape.control,
        colors = ButtonDefaults.buttonColors(
            containerColor = WakepointPrimaryLight,
            contentColor = WakepointPrimary
        )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(WakepointSpacing.iconTextGap))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
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
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = WakepointShape.card,
            color = WakepointCanvas
        ) {
            Column(
                modifier = Modifier.padding(WakepointSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(WakepointPrimaryTint10, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PriorityHigh,
                        contentDescription = null,
                        tint = WakepointPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = title,
                    color = WakepointInk,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    color = WakepointTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
                ) {
                    WakepointSecondaryButton(
                        text = cancelText,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    WakepointButton(
                        text = confirmText,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** White card — 라운드 20, hairline border, 기본 내부 패딩 16(통일). */
@Composable
fun WakepointCard(
    modifier: Modifier = Modifier,
    contentPadding: Int = 16,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = WakepointShape.card,
        border = BorderStroke(1.dp, WakepointBorder),
        colors = CardDefaults.cardColors(containerColor = WakepointCanvas),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(contentPadding.dp)) {
            content()
        }
    }
}

/** Status badge — 라운드 8 · T6(11/Medium). 배경/글자색을 역할 토큰으로 지정. */
@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    background: Color = color.copy(alpha = 0.12f)
) {
    Surface(
        modifier = modifier,
        color = background,
        shape = WakepointShape.badge
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = WakepointSpacing.sm, vertical = WakepointSpacing.xs)
        )
    }
}

/** Small status dot — green when active, muted grey when off. */
@Composable
fun StatusDot(active: Boolean, modifier: Modifier = Modifier, size: Int = 8) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                color = if (active) WakepointSuccess else WakepointFillHover,
                shape = CircleShape
            )
    )
}

/** Themed switch matching the prototype toggle. */
@Composable
fun WakepointSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = WakepointPrimary,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = WakepointInactiveTrack,
            uncheckedBorderColor = Color.Transparent
        )
    )
}

/** Circular avatar with a coloured background + initial. */
@Composable
fun WakepointAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 48,
    background: Color = WakepointPrimaryTint08,
    foreground: Color = WakepointPrimary
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).ifBlank { "?" },
            color = foreground,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun WakepointLogo(
    modifier: Modifier = Modifier,
    horizontal: Boolean = true
) {
    val symbolSize = if (horizontal) 30.dp else 56.dp
    val wordmark: @Composable () -> Unit = {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = WakepointInk
        )
    }
    val symbol: @Composable () -> Unit = {
        Image(
            painter = painterResource(R.drawable.logo_point),
            contentDescription = stringResource(R.string.app_name),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(symbolSize)
        )
    }

    if (horizontal) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            symbol()
            wordmark()
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            symbol()
            wordmark()
        }
    }
}

/** Logo header (kept for map / misc screens). */
@Composable
fun WakepointHeader(
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = WakepointSpacing.screenHorizontal, vertical = WakepointSpacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WakepointLogo()
        Spacer(modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

/** Bold page-title header used on tabbed list screens. */
@Composable
fun WakepointPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = WakepointSpacing.screenHorizontal)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = WakepointSpacing.md, bottom = if (subtitle == null) WakepointSpacing.sm else WakepointSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = WakepointInk,
                modifier = Modifier.weight(1f)
            )
            action?.invoke()
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointTextSecondary,
                modifier = Modifier.padding(bottom = WakepointSpacing.sm)
            )
        }
    }
}

/** Circular icon button used in headers (add / user-plus etc.). */
@Composable
fun WakepointCircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    tint: Color = WakepointInk
) {
    Box(
        modifier = modifier
            .size(WakepointSize.circleButton)
            .clip(CircleShape)
            .background(if (filled) WakepointField else WakepointCanvas)
            .then(
                if (filled) Modifier else Modifier.border(1.dp, WakepointBorderStrong, CircleShape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
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
        textStyle = MaterialTheme.typography.bodyLarge,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        modifier = modifier
            .fillMaxWidth()
            .height(WakepointSize.fieldHeight),
        placeholder = {
            Text(
                text = placeholder,
                color = WakepointTextQuaternary,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = WakepointIconInactive
                )
            }
        },
        trailingIcon = trailingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = trailingIconContentDescription,
                    tint = WakepointIconMuted,
                    modifier = if (onTrailingIconClick != null) {
                        Modifier.clickable(onClick = onTrailingIconClick)
                    } else {
                        Modifier
                    }
                )
            }
        },
        shape = WakepointShape.control,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WakepointPrimary,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedContainerColor = WakepointParchment,
            unfocusedContainerColor = WakepointParchment,
            disabledContainerColor = WakepointParchment,
            focusedTextColor = WakepointInk,
            unfocusedTextColor = WakepointInk,
            disabledTextColor = WakepointInk
        )
    )
}

/** Selectable option row (sounds) — white, bordered, blue when selected. */
@Composable
fun SoundOptionRow(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    selected: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(WakepointShape.control)
            .background(if (selected) WakepointPrimaryLight else WakepointCanvas)
            .border(
                width = 1.5.dp,
                color = if (selected) WakepointPrimary else WakepointChipBorder,
                shape = WakepointShape.control
            )
            .clickable(onClick = onClick)
            .padding(horizontal = WakepointSpacing.cardPadding, vertical = WakepointSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) WakepointPrimary else WakepointIconMuted,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) WakepointPrimary else WakepointInk
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = WakepointTextSecondary
                )
            }
        }
        trailing?.invoke()
    }
}

/** Grouped settings row inside a card. */
@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    val color = if (danger) WakepointDanger else WakepointInk
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(WakepointSize.rowHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = WakepointSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.iconTextGap)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (danger) WakepointDanger else WakepointIconMuted,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (showChevron) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = WakepointIconLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun BottomSheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .clip(CircleShape)
                .background(WakepointChipBorder)
        )
    }
}

/**
 * Standard header for bottom sheets. Uses the title (not headline) type scale and adds
 * breathing room below the drag handle, matching 2026 sheet patterns (e.g. Toss):
 * compact left-aligned title, tight optional subtitle.
 */
@Composable
fun BottomSheetTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = WakepointSpacing.xs, bottom = WakepointSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
    ) {
        Text(
            text = title,
            color = WakepointInk,
            style = MaterialTheme.typography.headlineLarge
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                color = WakepointTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun MapMarkerPreview(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(WakepointPrimaryTint13)
            .border(1.5.dp, WakepointPrimaryTint35, CircleShape)
            .padding(18.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.logo_point),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 34.dp, height = 50.dp)
        )
    }
}
