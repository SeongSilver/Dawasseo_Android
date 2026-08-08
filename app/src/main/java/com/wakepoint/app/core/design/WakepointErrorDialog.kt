package com.wakepoint.app.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * 오류/알림 팝업. 입력창 아래 인라인 대신 별도 다이얼로그로 표시하며,
 * 기존 [WakepointConfirmDialog] 와 동일한 디자인 언어(라운드 20 카드·아이콘·풀폭 버튼)를 따른다.
 *
 * @param isError true면 위험(빨강) 톤, false면 안내(파랑) 톤
 */
@Composable
fun WakepointMessageDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    confirmText: String = "확인",
    isError: Boolean = true
) {
    val accent = if (isError) WakepointDanger else WakepointPrimary
    val resolvedTitle = title ?: if (isError) "알림" else "안내"
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
                        .background(accent.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isError) "!" else "✓",
                        color = accent,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Text(
                    text = resolvedTitle,
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
                WakepointButton(
                    text = confirmText,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
