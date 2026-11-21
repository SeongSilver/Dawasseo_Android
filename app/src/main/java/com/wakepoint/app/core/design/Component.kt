package com.wakepoint.app.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wakepoint.app.R
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────
// 공통 컴포넌트 — 출처: design-handoff/DESIGN.md 5절, screens/*.html 의 인라인 수치.
// 그림자를 쓰지 않는다. 면은 채움과 테두리로만 구분한다.
// ─────────────────────────────────────────────────────────────────────

/** 버튼 크기. s = 카드·행 안(40), m = 기본(56), l = 알람 중지(96). */
enum class WakepointButtonSize { Small, Medium, Large }

private val WakepointButtonSize.height
    get() = when (this) {
        WakepointButtonSize.Small -> WakepointSize.buttonHeightSmall
        WakepointButtonSize.Medium -> WakepointSize.buttonHeight
        WakepointButtonSize.Large -> WakepointSize.stopButtonHeight
    }

private val WakepointButtonSize.horizontalPadding
    get() = when (this) {
        WakepointButtonSize.Small -> WakepointSpacing.lg
        WakepointButtonSize.Medium -> WakepointSpacing.xl
        WakepointButtonSize.Large -> WakepointSpacing.xxxl
    }

private val WakepointButtonSize.iconSize
    get() = when (this) {
        WakepointButtonSize.Small -> WakepointSize.iconSmall
        else -> WakepointSize.icon
    }

@Composable
private fun WakepointButtonSize.labelStyle() = when (this) {
    WakepointButtonSize.Small -> MaterialTheme.typography.labelLarge
    WakepointButtonSize.Medium -> MaterialTheme.typography.titleMedium
    WakepointButtonSize.Large -> MaterialTheme.typography.headlineSmall
}

/** 누르는 동안 모서리가 한 단계 작아진다 (DESIGN.md 4절). */
@Composable
private fun pressedAwareShape(interactionSource: MutableInteractionSource): Shape {
    val pressed by interactionSource.collectIsPressedAsState()
    return if (pressed) WakepointShape.buttonPressed else WakepointShape.full
}

@Composable
private fun BaseButton(
    text: String,
    modifier: Modifier,
    icon: ImageVector?,
    enabled: Boolean,
    size: WakepointButtonSize,
    containerColor: Color,
    contentColor: Color,
    border: BorderStroke?,
    onClick: () -> Unit
) {
    val colors = LocalWakepointColors.current
    val interactionSource = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.height(size.height),
        shape = pressedAwareShape(interactionSource),
        border = border,
        elevation = null,
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = colors.disabledContainer,
            disabledContentColor = colors.disabledContent
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size.iconSize)
            )
            Spacer(modifier = Modifier.width(WakepointSpacing.iconTextGap))
        }
        Text(text = text, style = size.labelStyle(), maxLines = 1)
    }
}

/** 채움 버튼 — 핵심 액션. 배경 `brand`, 글자 `onBrand`. */
@Composable
fun WakepointButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    size: WakepointButtonSize = WakepointButtonSize.Medium
) {
    val colors = LocalWakepointColors.current
    BaseButton(
        text = text,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        size = size,
        containerColor = colors.brand,
        contentColor = colors.onBrand,
        border = null,
        onClick = onClick
    )
}

/**
 * 테두리 버튼 — 취소 · 삭제 · 거절 · 차단 · 가져오기 · 요청 취소.
 * 파괴적 액션도 빨강을 쓰지 않고 아이콘으로 구분한다 (DESIGN.md 1·5절).
 */
@Composable
fun WakepointSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    size: WakepointButtonSize = WakepointButtonSize.Medium
) {
    val colors = LocalWakepointColors.current
    BaseButton(
        text = text,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        size = size,
        containerColor = Color.Transparent,
        contentColor = colors.text,
        border = BorderStroke(WakepointSize.border, colors.line),
        onClick = onClick
    )
}

/**
 * 파괴적 액션(삭제 · 차단 · 로그아웃)용 버튼.
 * 빨강은 입력 오류와 실패 메시지 전용이므로 여기서는 쓰지 않고 테두리 버튼으로 둔다.
 */
@Composable
fun WakepointDangerButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    size: WakepointButtonSize = WakepointButtonSize.Medium
) = WakepointSecondaryButton(text, modifier, icon, enabled, onClick, size)

/** tonal 버튼 — 친구 추가 · 권한 요청 · 정지. 배경 `fill`, 글자 `text`. */
@Composable
fun WakepointTintButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    size: WakepointButtonSize = WakepointButtonSize.Medium
) {
    val colors = LocalWakepointColors.current
    BaseButton(
        text = text,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        size = size,
        containerColor = colors.fill,
        contentColor = colors.text,
        border = null,
        onClick = onClick
    )
}

/** 글자 버튼 — 비밀번호 찾기 · 회원가입 등. 글자 `brand`, 높이 40. */
@Composable
fun WakepointTextButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val colors = LocalWakepointColors.current
    BaseButton(
        text = text,
        modifier = modifier,
        icon = null,
        enabled = enabled,
        size = WakepointButtonSize.Small,
        containerColor = Color.Transparent,
        contentColor = colors.brand,
        border = null,
        onClick = onClick
    )
}

/** 다이얼로그 — 배경 `surfaceElevated`, 모서리 28, 아이콘 24 `textSecondary`, 버튼 2개 가로. */
@Composable
fun WakepointConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val colors = LocalWakepointColors.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = WakepointShape.dialog,
            color = colors.surfaceElevated
        ) {
            Column(
                modifier = Modifier.padding(WakepointSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(WakepointSize.icon)
                    )
                }
                Text(
                    text = title,
                    color = colors.text,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = message,
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
                ) {
                    WakepointSecondaryButton(
                        text = cancelText,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        size = WakepointButtonSize.Small
                    )
                    WakepointButton(
                        text = confirmText,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        size = WakepointButtonSize.Small
                    )
                }
            }
        }
    }
}

/**
 * 카드 — 모서리 16, 안 여백 16.
 * `filled = true` 는 `card` 채움(활성·메뉴·사운드), `false` 는 `background` + 1dp `line` 테두리(비활성).
 */
@Composable
fun WakepointCard(
    modifier: Modifier = Modifier,
    contentPadding: Int = 16,
    filled: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = LocalWakepointColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = WakepointShape.card,
        border = if (filled) null else BorderStroke(WakepointSize.border, colors.line),
        colors = CardDefaults.cardColors(
            containerColor = if (filled) colors.card else colors.background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(contentPadding.dp)) {
            content()
        }
    }
}

/**
 * 상태 pill — 높이 32, 모서리 8, 아이콘 18, labelLarge.
 * 완료 `positiveSurface`+`positive` / 진행 `fill`+`text` / 부정 테두리 `line`+`text` / 중립 `fillWeak`+`textSecondary`.
 */
@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    background: Color = Color.Transparent,
    icon: ImageVector? = null,
    border: Color? = null
) {
    Surface(
        modifier = modifier.height(WakepointSize.pillHeight),
        color = background,
        contentColor = color,
        shape = WakepointShape.pill,
        border = border?.let { BorderStroke(WakepointSize.border, it) }
    ) {
        Row(
            modifier = Modifier.padding(
                start = WakepointSpacing.sm,
                end = WakepointSpacing.lg,
                top = WakepointSpacing.xs + 2.dp,
                bottom = WakepointSpacing.xs + 2.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(WakepointSize.iconPill)
                )
            }
            Text(text = text, color = color, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

/** 작은 상태 점 — 켜짐 `brand`, 꺼짐 `inactive`. */
@Composable
fun StatusDot(active: Boolean, modifier: Modifier = Modifier, size: Int = 8) {
    val colors = LocalWakepointColors.current
    Box(
        modifier = modifier
            .size(size.dp)
            .background(
                color = if (active) colors.brand else colors.inactive,
                shape = CircleShape
            )
    )
}

/** 스위치 — 켜짐 트랙 `brand`/핸들 `onBrand`, 꺼짐 트랙 `fill`/테두리·핸들 `inactive`. 핸들 아이콘 없음. */
@Composable
fun WakepointSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LocalWakepointColors.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        thumbContent = null,
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.onBrand,
            checkedTrackColor = colors.brand,
            checkedBorderColor = colors.brand,
            uncheckedThumbColor = colors.inactive,
            uncheckedTrackColor = colors.fill,
            uncheckedBorderColor = colors.inactive,
            disabledCheckedThumbColor = colors.disabledContent,
            disabledCheckedTrackColor = colors.disabledContainer,
            disabledCheckedBorderColor = Color.Transparent,
            disabledUncheckedThumbColor = colors.disabledContent,
            disabledUncheckedTrackColor = colors.disabledContainer,
            disabledUncheckedBorderColor = Color.Transparent
        )
    )
}

/** 아바타 — 원형, 배경 `fillStrong`, 글자 `text` · titleMedium. */
@Composable
fun WakepointAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 40,
    background: Color = LocalWakepointColors.current.fillStrong,
    foreground: Color = LocalWakepointColors.current.text,
    shape: Shape = CircleShape
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(background, shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).ifBlank { "?" },
            color = foreground,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/** 로고 — 심볼 + 워드마크 "다왔어"(22/28 · 500). 심볼은 `textStrong`. */
@Composable
fun WakepointLogo(
    modifier: Modifier = Modifier,
    horizontal: Boolean = true,
    symbolSize: Int = if (horizontal) 32 else 72
) {
    val colors = LocalWakepointColors.current
    val wordmark: @Composable () -> Unit = {
        Text(
            text = stringResource(R.string.app_name),
            style = if (horizontal) WakepointWordmarkStyle else MaterialTheme.typography.headlineMedium,
            color = colors.text
        )
    }
    val symbol: @Composable () -> Unit = {
        Icon(
            painter = painterResource(R.drawable.ic_brand_mark),
            contentDescription = stringResource(R.string.app_name),
            tint = colors.textStrong,
            modifier = Modifier.size(symbolSize.dp)
        )
    }

    if (horizontal) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
        ) {
            symbol()
            wordmark()
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
        ) {
            symbol()
            wordmark()
        }
    }
}

/** 상단 바 — 높이 64, 왼쪽 심볼 32 + "다왔어", 오른쪽 액션 슬롯. */
@Composable
fun WakepointHeader(
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(WakepointSize.headerHeight)
            .padding(horizontal = WakepointSpacing.screenHorizontal),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WakepointLogo()
        Spacer(modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

/** 제목 상단 바 — titleLarge(22/28 · 400). */
@Composable
fun WakepointPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null,
    navigation: @Composable (() -> Unit)? = null
) {
    val colors = LocalWakepointColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = WakepointSize.headerHeight)
                .padding(horizontal = WakepointSpacing.screenHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
        ) {
            navigation?.invoke()
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.text,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            action?.invoke()
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(
                    start = WakepointSpacing.screenHorizontal,
                    end = WakepointSpacing.screenHorizontal,
                    bottom = WakepointSpacing.sm
                )
            )
        }
    }
}

/** 원형 아이콘 버튼 — 48, `filled` 면 `fill` 채움, 아니면 1dp `line` 테두리. */
@Composable
fun WakepointCircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    tint: Color = LocalWakepointColors.current.textSecondary
) {
    val colors = LocalWakepointColors.current
    Box(
        modifier = modifier
            .size(WakepointSize.circleButton)
            .clip(CircleShape)
            .background(if (filled) colors.fill else Color.Transparent)
            .then(
                if (filled) Modifier else Modifier.border(WakepointSize.border, colors.line, CircleShape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(WakepointSize.icon)
        )
    }
}

/**
 * 텍스트 필드 — 높이 56, 모서리 4. 라벨은 안에 있다가 입력하면 위로 뜬다.
 * 화면 배경 위는 테두리형(기본), 시트와 카드 안은 채움형(`filled = true`). 한 폼에 한 종류만 쓴다.
 * 포커스 3dp `brandLineStrong`, 오류 3dp `negativeLineStrong` + 아이콘 `negative` + 라벨·보조 문구 `negativeText`.
 */
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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    filled: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = true
) {
    val colors = LocalWakepointColors.current
    val label: @Composable () -> Unit = {
        Text(text = placeholder, style = MaterialTheme.typography.bodyLarge)
    }
    val leading: (@Composable () -> Unit)? = leadingIcon?.let {
        {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(WakepointSize.icon)
            )
        }
    }
    val trailing: (@Composable () -> Unit)? = when {
        isError -> {
            {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = colors.negative,
                    modifier = Modifier.size(WakepointSize.icon)
                )
            }
        }
        trailingIcon != null -> {
            {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = trailingIconContentDescription,
                    tint = colors.textSecondary,
                    modifier = Modifier
                        .size(WakepointSize.icon)
                        .then(
                            if (onTrailingIconClick != null) {
                                Modifier.clickable(onClick = onTrailingIconClick)
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }
        else -> null
    }
    val support: (@Composable () -> Unit)? = supportingText?.let {
        { Text(text = it, style = MaterialTheme.typography.bodySmall) }
    }

    if (filled) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            isError = isError,
            label = label,
            leadingIcon = leading,
            trailingIcon = trailing,
            supportingText = support,
            textStyle = MaterialTheme.typography.bodyLarge,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            shape = WakepointShape.field,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.fill,
                unfocusedContainerColor = colors.fill,
                disabledContainerColor = colors.fill,
                errorContainerColor = colors.fill,
                focusedIndicatorColor = colors.brandLineStrong,
                unfocusedIndicatorColor = colors.line,
                disabledIndicatorColor = colors.line,
                errorIndicatorColor = colors.negativeLineStrong,
                focusedTextColor = colors.text,
                unfocusedTextColor = colors.text,
                disabledTextColor = colors.textTertiary,
                errorTextColor = colors.text,
                cursorColor = colors.brand,
                focusedLabelColor = colors.textTertiary,
                unfocusedLabelColor = colors.textTertiary,
                disabledLabelColor = colors.textTertiary,
                errorLabelColor = colors.negativeText,
                focusedSupportingTextColor = colors.textTertiary,
                unfocusedSupportingTextColor = colors.textTertiary,
                errorSupportingTextColor = colors.negativeText
            )
        )
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            isError = isError,
            label = label,
            leadingIcon = leading,
            trailingIcon = trailing,
            supportingText = support,
            textStyle = MaterialTheme.typography.bodyLarge,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            shape = WakepointShape.field,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.brandLineStrong,
                unfocusedBorderColor = colors.line,
                disabledBorderColor = colors.line,
                errorBorderColor = colors.negativeLineStrong,
                focusedContainerColor = colors.background,
                unfocusedContainerColor = colors.background,
                disabledContainerColor = colors.background,
                errorContainerColor = colors.background,
                focusedTextColor = colors.text,
                unfocusedTextColor = colors.text,
                disabledTextColor = colors.textTertiary,
                errorTextColor = colors.text,
                cursorColor = colors.brand,
                focusedLabelColor = colors.textTertiary,
                unfocusedLabelColor = colors.textTertiary,
                disabledLabelColor = colors.textTertiary,
                errorLabelColor = colors.negativeText,
                focusedSupportingTextColor = colors.textTertiary,
                unfocusedSupportingTextColor = colors.textTertiary,
                errorSupportingTextColor = colors.negativeText
            )
        )
    }
}

/** 메시지 영역의 종류 (DESIGN.md 5절). */
enum class WakepointMessageKind { Error, Info, InfoOnSheet, Caution }

/**
 * 메시지 영역 — 모서리 12, 패딩 12/16.
 * 오류: `negativeSurface` + 1dp `negativeLine` + 아이콘 `negative` + 글자 `negativeText`.
 * 안내: `fill`(시트 안은 `surfaceElevatedSecondary`) + 글자 `textSecondary`.
 * 알림 권한 안내: `cautionarySurface` + 글자 `cautionary`.
 */
@Composable
fun WakepointMessage(
    text: String,
    modifier: Modifier = Modifier,
    kind: WakepointMessageKind = WakepointMessageKind.Info,
    icon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = LocalWakepointColors.current
    val background = when (kind) {
        WakepointMessageKind.Error -> colors.negativeSurface
        WakepointMessageKind.Info -> colors.fill
        WakepointMessageKind.InfoOnSheet -> colors.surfaceElevatedSecondary
        WakepointMessageKind.Caution -> colors.cautionarySurface
    }
    val content = when (kind) {
        WakepointMessageKind.Error -> colors.negativeText
        WakepointMessageKind.Info, WakepointMessageKind.InfoOnSheet -> colors.textSecondary
        WakepointMessageKind.Caution -> colors.cautionary
    }
    val shownIcon = icon ?: if (kind == WakepointMessageKind.Error) Icons.Rounded.ErrorOutline else null
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(WakepointShape.message)
            .background(background)
            .then(
                if (kind == WakepointMessageKind.Error) {
                    Modifier.border(WakepointSize.border, colors.negativeLine, WakepointShape.message)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = WakepointSpacing.lg, vertical = WakepointSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md),
        verticalAlignment = Alignment.Top
    ) {
        if (shownIcon != null) {
            Icon(
                imageVector = shownIcon,
                contentDescription = null,
                tint = if (kind == WakepointMessageKind.Error) colors.negative else content,
                modifier = Modifier.size(WakepointSize.iconSmall)
            )
        }
        Text(
            text = text,
            color = content,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

/** 사운드 목록 행 — 높이 64, 선택됨 = 배경 `brandSubtle` + 오른쪽 체크 `brand`. */
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
    val colors = LocalWakepointColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = WakepointSize.soundRowHeight)
            .background(if (selected) colors.brandSubtle else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = WakepointSpacing.lg, vertical = WakepointSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) colors.brand else colors.textSecondary,
            modifier = Modifier.size(WakepointSize.icon)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.text
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        trailing?.invoke()
    }
}

/** 묶음 행 — 높이 56, 좌우 16. 구분선은 바깥에서 인셋 16으로 그린다. */
@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    val colors = LocalWakepointColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(WakepointSize.rowHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = WakepointSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(WakepointSize.icon)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.text,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (showChevron) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(WakepointSize.icon)
            )
        }
    }
}

/** 구분선 — 1dp `line`. 묶음 행 사이는 좌우 인셋 16. */
@Composable
fun WakepointDivider(
    modifier: Modifier = Modifier,
    inset: Boolean = false
) {
    val colors = LocalWakepointColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (inset) WakepointSpacing.lg else 0.dp)
            .height(WakepointSize.border)
            .background(colors.line)
    )
}

/** 시트 드래그 핸들 — 32×4, `inactive`. */
@Composable
fun BottomSheetHandle(modifier: Modifier = Modifier) {
    val colors = LocalWakepointColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = WakepointSpacing.md),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = WakepointSize.sheetHandleWidth, height = WakepointSize.sheetHandleHeight)
                .clip(CircleShape)
                .background(colors.inactive)
        )
    }
}

/** 시트 제목 — headlineSmall(24/32 · 400). */
@Composable
fun BottomSheetTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val colors = LocalWakepointColors.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)
    ) {
        Text(
            text = title,
            color = colors.text,
            style = MaterialTheme.typography.headlineSmall
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** 빈 상태 카드 — 모서리 28, 아이콘 원 `fill`, 제목 titleLarge. */
@Composable
fun WakepointEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    val colors = LocalWakepointColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(WakepointShape.dialog)
            .background(colors.card)
            .padding(
                start = WakepointSpacing.xl,
                end = WakepointSpacing.xl,
                top = WakepointSpacing.xxl,
                bottom = WakepointSpacing.xl
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(WakepointSpacing.xxxl + WakepointSpacing.lg)
                .clip(CircleShape)
                .background(colors.fill),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(WakepointSize.icon)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.text,
            textAlign = TextAlign.Center
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
        action?.invoke()
    }
}

/** 지도 목적지 마커 미리보기 — `brand` 핀. */
@Composable
fun MapMarkerPreview(modifier: Modifier = Modifier) {
    val colors = LocalWakepointColors.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(WakepointSpacing.xxxl)
            .clip(CircleShape)
            .background(colors.brandSubtle)
            .border(WakepointSize.border, colors.brandLineStrong, CircleShape)
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = colors.brand,
            modifier = Modifier.size(WakepointSize.icon)
        )
    }
}

/**
 * 목록 첫 로딩 플레이스홀더.
 * 로컬 DB 로딩은 수십 ms 안에 끝나므로 그 동안은 아무것도 그리지 않고,
 * 지연 임계값을 넘긴 느린 로딩에서만 표시해 깜빡임을 피한다.
 */
@Composable
fun WakepointLoadingPlaceholder(modifier: Modifier = Modifier) {
    val colors = LocalWakepointColors.current
    var showSpinner by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(LOADING_SPINNER_DELAY_MS)
        showSpinner = true
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = WakepointSpacing.xxl),
        contentAlignment = Alignment.Center
    ) {
        if (showSpinner) {
            CircularProgressIndicator(
                color = colors.brand,
                strokeWidth = 2.dp,
                modifier = Modifier.size(WakepointSize.icon)
            )
        }
    }
}

/** 이 시간 안에 로딩이 끝나면 아예 보여주지 않는다. */
private const val LOADING_SPINNER_DELAY_MS = 350L
