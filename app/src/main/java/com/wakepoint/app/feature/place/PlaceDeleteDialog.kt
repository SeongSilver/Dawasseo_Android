package com.wakepoint.app.feature.place

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointDanger
import com.wakepoint.app.core.design.WakepointDangerButton
import com.wakepoint.app.core.design.WakepointDangerLight
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointShape
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary

/**
 * v2 STEP2 (2-4) — 장소 삭제 확인 다이얼로그.
 * 공개 중이면 친구 쪽 공유 항목('장소 N')도 함께 사라짐을 명시. 삭제 버튼만 Danger.
 */
@Composable
fun PlaceDeleteDialog(
    placeName: String,
    sharedName: String?,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(shape = WakepointShape.card, color = WakepointCanvas, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(WakepointSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(WakepointDangerLight, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = WakepointDanger,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    "'$placeName'을(를) 삭제할까요?",
                    style = MaterialTheme.typography.titleLarge,
                    color = WakepointInk,
                    textAlign = TextAlign.Center,
                )
                Text(
                    buildString {
                        append("이 장소로 받은 알람도 함께 사라져요.")
                        if (sharedName != null) append("\n친구 목록의 '$sharedName'도 사라져요.")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = WakepointTextSecondary,
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm),
                ) {
                    WakepointSecondaryButton("취소", Modifier.weight(1f), onClick = onCancel)
                    WakepointDangerButton("삭제", Modifier.weight(1f), onClick = onDelete)
                }
            }
        }
    }
}
