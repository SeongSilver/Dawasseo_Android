package com.wakepoint.app.feature.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.core.design.LocalWakepointColors
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointConfirmDialog
import com.wakepoint.app.core.design.WakepointLoadingPlaceholder
import com.wakepoint.app.core.design.WakepointMessageDialog
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointSpacing

/**
 * v2 STEP3 (3-6) — 보낸 알람 목록(친구 탭 서브 화면).
 * 상태는 발신자에게 [전송 대기]/[전송됨]까지만 노출 — 이후(끔/도착)는 상호 비가시(명세).
 * 항목별 [취소]는 원격 삭제(상대 기기 미등록분에만 의미).
 */
@Composable
fun SentAlarmsScreen(
    onBack: () -> Unit,
    viewModel: SentAlarmsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    SentAlarmsContent(
        uiState = uiState,
        onBack = onBack,
        onCancel = viewModel::cancel,
        onDismissError = viewModel::clearError,
    )
}

/** 상태 주입형 콘텐츠(계측 테스트 대상). */
@Composable
fun SentAlarmsContent(
    uiState: SentAlarmsUiState,
    onBack: () -> Unit,
    onCancel: (alarmId: String) -> Unit,
    onDismissError: () -> Unit,
) {
    var cancelTarget by remember { mutableStateOf<SentAlarmItem?>(null) }

    uiState.errorMessage?.let { message ->
        WakepointMessageDialog(message = message, onDismiss = onDismissError)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalWakepointColors.current.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(LocalWakepointColors.current.surfaceElevated)
                .padding(horizontal = WakepointSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "뒤로",
                    tint = LocalWakepointColors.current.text,
                )
            }
            Text("보낸 알람", style = MaterialTheme.typography.titleSmall, color = LocalWakepointColors.current.text)
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = WakepointSpacing.screenHorizontal)
                .padding(bottom = WakepointSpacing.xl),
        ) {
            WakepointPageHeader(
                title = "보낸 알람",
                subtitle = "전송까지만 보여요. 이후는 친구의 알람이에요.",
                modifier = Modifier.padding(horizontal = 0.dp),
            )
            Spacer(Modifier.height(WakepointSpacing.sectionTitleGap))
            Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap)) {
                when {
                    uiState.isLoading -> WakepointLoadingPlaceholder()
                    uiState.items.isEmpty() -> Text(
                        "아직 보낸 알람이 없어요.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocalWakepointColors.current.textTertiary,
                        modifier = Modifier.padding(vertical = WakepointSpacing.xl),
                    )
                    else -> uiState.items.forEach { item ->
                        SentAlarmCard(
                            item = item,
                            canceling = item.alarm.id in uiState.cancelingIds,
                            onCancel = { cancelTarget = item },
                        )
                    }
                }
            }
        }
    }

    cancelTarget?.let { target ->
        WakepointConfirmDialog(
            title = "보낸 알람을 취소할까요?",
            message = "'${target.alarm.label}' 알람이 ${target.recipientName}님에게서 사라져요.",
            cancelText = "닫기",
            confirmText = "알람 취소",
            onDismiss = { cancelTarget = null },
            onConfirm = {
                onCancel(target.alarm.id)
                cancelTarget = null
            },
        )
    }
}

@Composable
private fun SentAlarmCard(item: SentAlarmItem, canceling: Boolean, onCancel: () -> Unit) {
    val radiusLabel = if (item.alarm.radiusKm < 1.0) {
        "${(item.alarm.radiusKm * 1000).toInt()}m"
    } else {
        "${item.alarm.radiusKm.toInt()}km"
    }
    WakepointCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${item.recipientName}님 · ${item.alarm.label}",
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalWakepointColors.current.text,
                )
                Spacer(Modifier.height(WakepointSpacing.xs))
                Text(
                    "위치 · $radiusLabel · ${item.alarm.targetAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalWakepointColors.current.textSecondary,
                )
            }
            if (item.pending) {
                StatusPill("전송 대기", color = LocalWakepointColors.current.cautionary, background = LocalWakepointColors.current.cautionarySurface)
            } else {
                StatusPill("전송됨", color = LocalWakepointColors.current.textSecondary)
            }
        }
        Spacer(Modifier.height(WakepointSpacing.md))
        Text(
            text = if (canceling) "취소 중…" else "취소",
            style = MaterialTheme.typography.labelLarge,
            color = if (canceling) LocalWakepointColors.current.textTertiary else LocalWakepointColors.current.negative,
            modifier = Modifier.clickable(enabled = !canceling, onClick = onCancel),
        )
    }
}
