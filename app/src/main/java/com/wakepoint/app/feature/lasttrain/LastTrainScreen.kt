package com.wakepoint.app.feature.lasttrain

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakepoint.app.core.design.BottomSheetHandle
import com.wakepoint.app.core.design.StatusPill
import com.wakepoint.app.core.design.WakepointBackground
import com.wakepoint.app.core.design.WakepointButton
import com.wakepoint.app.core.design.WakepointCanvas
import com.wakepoint.app.core.design.WakepointCard
import com.wakepoint.app.core.design.WakepointInk
import com.wakepoint.app.core.design.WakepointLoadingPlaceholder
import com.wakepoint.app.core.design.WakepointMessageDialog
import com.wakepoint.app.core.design.WakepointPageHeader
import com.wakepoint.app.core.design.WakepointSpacing
import com.wakepoint.app.core.design.WakepointTextSecondary
import com.wakepoint.app.core.design.WakepointWarning
import com.wakepoint.app.core.design.WakepointWarningLight

/**
 * v2 STEP4 — 막차 알람 메인(마이 탭 진입 서브 화면).
 * 흐름: 집 미등록(4-3) → 등록 유도 / 등록됨 → [막차 조회] → 최초 1회 위치 동의(4-5) →
 * 여유·임박·아슬 카드(4-1) 또는 끊김(4-2)/미지원(4-4). "알람 걸기"는 시간 알람에 실연동.
 * 계산은 Kakao Mobility 미확보로 모의 데이터(헤더에 명시).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastTrainScreen(
    onBack: () -> Unit,
    onRegisterHome: () -> Unit,
    viewModel: LastTrainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showConsentSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.alarmSetMessage) {
        uiState.alarmSetMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeAlarmSetMessage()
        }
    }

    uiState.errorMessage?.let { message ->
        WakepointMessageDialog(message = message, onDismiss = viewModel::clearError)
    }

    fun startLookup() {
        if (context.isLastTrainConsentGiven()) {
            viewModel.lookup()
        } else {
            showConsentSheet = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WakepointBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(WakepointCanvas)
                .padding(horizontal = WakepointSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "뒤로",
                    tint = WakepointInk,
                )
            }
            Text("막차 알람", style = MaterialTheme.typography.titleSmall, color = WakepointInk)
        }
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = WakepointSpacing.screenHorizontal)
                .padding(bottom = WakepointSpacing.xl),
        ) {
            WakepointPageHeader(
                title = "막차 알람",
                subtitle = "집까지 가는 막차와 택시비를 알려드려요",
                modifier = Modifier.padding(horizontal = 0.dp),
                action = {
                    // 경로 API(Kakao Mobility) 미확보 — 모의 계산임을 화면에 정직하게 표시.
                    StatusPill("모의 데이터", color = WakepointWarning, background = WakepointWarningLight)
                },
            )
            Spacer(Modifier.height(WakepointSpacing.sectionTitleGap))
            Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.cardGap)) {
                when {
                    uiState.isLoading -> WakepointLoadingPlaceholder()
                    uiState.homePlace == null -> LastTrainNoHomeCard(onRegisterHome = onRegisterHome)
                    uiState.result == null -> LastTrainLookupCard(
                        homeName = uiState.homePlace?.myName.orEmpty(),
                        onLookup = { startLookup() },
                    )
                    else -> when (val result = uiState.result) {
                        is LastTrainResult.Available -> LastTrainCard(
                            state = result,
                            onSetDeadlineAlarm = viewModel::setDeadlineAlarm,
                            onOpenRoute = { context.openKakaoMap() },
                            onTaxiLookup = viewModel::loadTaxiFare,
                        )
                        is LastTrainResult.Ended -> LastTrainEndedCard(
                            state = result,
                            onOpenKakao = { context.openKakaoMap() },
                            onSetFirstTrainAlarm = viewModel::setFirstTrainAlarm,
                        )
                        is LastTrainResult.Unsupported -> LastTrainUnsupportedCard(
                            reason = result.reason,
                            onOpenKakao = { context.openKakaoMap() },
                        )
                        null -> Unit
                    }
                }
            }
        }
    }

    if (showConsentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showConsentSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            dragHandle = { BottomSheetHandle(modifier = Modifier.padding(top = WakepointSpacing.md)) },
            containerColor = WakepointCanvas,
        ) {
            LocationConsentSheetContent(
                onAgree = {
                    context.markLastTrainConsentGiven()
                    showConsentSheet = false
                    viewModel.lookup()
                },
                onLater = { showConsentSheet = false },
            )
        }
    }
}

/** 조회 전 초기 카드 — 집 등록 완료 상태에서 [막차 조회]로 시작. */
@Composable
private fun LastTrainLookupCard(homeName: String, onLookup: () -> Unit) {
    WakepointCard {
        Column(verticalArrangement = Arrangement.spacedBy(WakepointSpacing.md)) {
            Text(
                "지금 일어나야 하나?",
                style = MaterialTheme.typography.titleLarge,
                color = WakepointInk,
            )
            Text(
                "'$homeName'까지 가는 막차 시각과 택시비를 3초 안에 알려드려요.",
                style = MaterialTheme.typography.bodyMedium,
                color = WakepointTextSecondary,
            )
            WakepointButton(
                text = "막차 조회",
                icon = Icons.Rounded.Train,
                onClick = onLookup,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** 카카오맵 열기(설치 앱 우선, 없으면 웹). 실패는 무해하게 무시. */
private fun Context.openKakaoMap() {
    runCatching {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://map.kakao.com"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
