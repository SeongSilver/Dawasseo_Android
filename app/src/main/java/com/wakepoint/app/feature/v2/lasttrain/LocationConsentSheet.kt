package com.wakepoint.app.feature.v2.lasttrain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.BottomSheetTitle
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointPrimary
import com.wakepoint.app.core.design.WakepointSecondaryButton
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointTheme

/**
 * v2 STEP4 (4e) — 위치 전송 동의(최초 1회).
 * 막차·택시비 조회 시에만 현재 위치가 카카오맵으로 전달. 전달 항목/시점/저장여부 3줄 명세.
 * 도착 알람 판정은 로컬·서버 미저장 재확인. [동의하고 조회][다음에 할게요].
 */
@Composable
fun LocationConsentSheet(
    modifier: Modifier = Modifier,
    onAgree: () -> Unit = {},
    onLater: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WakepointCanvas)
            .padding(horizontal = WakepointSpacing.screenHorizontal)
            .padding(bottom = WakepointSpacing.xl),
    ) {
        Spacer(Modifier.height(WakepointSpacing.md))
        BottomSheetHandle()
        Spacer(Modifier.height(WakepointSpacing.lg))
        BottomSheetTitle(
            title = "현재 위치를 카카오맵에 전달할까요?",
            subtitle = "막차·택시비를 조회할 때만 사용해요.",
        )
        Spacer(Modifier.height(WakepointSpacing.xl))

        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.lg)) {
            ConsentRow(Icons.Rounded.Shield, "전달 항목", "현재 위치와 집 주소(막차·경로 계산용)")
            ConsentRow(Icons.Rounded.Schedule, "전달 시점", "'조회' 버튼을 누를 때만, 그 순간에")
            ConsentRow(Icons.Rounded.CheckCircle, "저장 여부", "다왔어 서버에 저장하지 않아요. 도착 알람 판정은 휴대폰 안에서만.")
        }

        Spacer(Modifier.height(WakepointSpacing.xl))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.sm)) {
            WakepointSecondaryButton("다음에 할게요", Modifier.weight(1f), onClick = onLater)
            WakepointButton("동의하고 조회", Modifier.weight(1f), onClick = onAgree)
        }
    }
}

@Composable
private fun ConsentRow(icon: ImageVector, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
        Icon(icon, contentDescription = null, tint = WakepointPrimary, modifier = Modifier.height(24.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = WakepointInk)
            Text(body, style = MaterialTheme.typography.bodySmall, color = WakepointTextSecondary)
        }
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun LocationConsentSheetPreview() {
    WakepointTheme { LocationConsentSheet() }
}
