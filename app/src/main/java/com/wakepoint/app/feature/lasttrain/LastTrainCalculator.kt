package com.wakepoint.app.feature.lasttrain

import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** 막차 3단계(v2 STEP4 4-1). */
enum class LastTrainLevel {
    RELAXED, // 여유 (Primary)
    IMMINENT, // 임박 (Warning 링)
    CRITICAL, // 아슬 (Danger 반전)
}

/** 막차 조회 결과(4-1 여유/임박/아슬 · 4-2 끊김 · 4-4 미지원). */
sealed interface LastTrainResult {
    data class Available(
        val remainingMinutes: Int,
        /** 승차 데드라인. 시간 알람 연동에 쓴다. */
        val deadlineHour: Int,
        val deadlineMinute: Int,
        val stationName: String,
        val bottleneck: String,
        /** null 이면 "탭해서 조회"(여유 단계 지연 로딩). */
        val taxiFare: String?,
    ) : LastTrainResult {
        val level: LastTrainLevel get() = LastTrainPolicy.levelFor(remainingMinutes)
    }

    data class Ended(
        val firstTrainHour: Int,
        val firstTrainMinute: Int,
        val taxiFare: String,
        val taxiDetail: String,
    ) : LastTrainResult

    data class Unsupported(val reason: String) : LastTrainResult
}

/**
 * 막차 단계 판정 순수 정책.
 * 임계값(임박 15분)은 제품 미확정이라 여기 한 곳에만 두고, 확정 시 이 상수만 바꾼다.
 */
object LastTrainPolicy {
    const val IMMINENT_THRESHOLD_MINUTES = 15

    fun levelFor(remainingMinutes: Int): LastTrainLevel = when {
        remainingMinutes <= 0 -> LastTrainLevel.CRITICAL
        remainingMinutes <= IMMINENT_THRESHOLD_MINUTES -> LastTrainLevel.IMMINENT
        else -> LastTrainLevel.RELAXED
    }

    /** 남은 시간 표기: "23분" / "-4분"(아슬). */
    fun remainingText(remainingMinutes: Int): String = "${remainingMinutes}분"
}

/** 막차 계산기. 실기능은 Kakao Mobility 확보 후 — 지금은 [MockLastTrainCalculator] 뿐. */
interface LastTrainCalculator {
    /** [now] 기준 집 방향 막차 상태. */
    fun lookup(now: ZonedDateTime): LastTrainResult
}

/**
 * 모의 계산기(계획서 Phase 5 제약: 외부 모빌리티 API 미확보 → UI는 명세대로, 계산은 모의).
 *
 * 시계만으로 결정적으로 동작해 데모·테스트가 가능하다:
 * - 막차 23:45 고정, 심야(00:00~05:00)는 끊김(첫차 05:34), 낮(05:00~21:00)은 미지원 안내
 *   대신 여유(다음 막차까지 계산)로 표시.
 */
@Singleton
class MockLastTrainCalculator @Inject constructor() : LastTrainCalculator {

    override fun lookup(now: ZonedDateTime): LastTrainResult {
        val time = now.toLocalTime()
        // 심야(자정~첫차): 오늘 대중교통 끊김.
        if (time.isBefore(FIRST_TRAIN)) {
            return LastTrainResult.Ended(
                firstTrainHour = FIRST_TRAIN.hour,
                firstTrainMinute = FIRST_TRAIN.minute,
                taxiFare = "약 28,000원",
                taxiDetail = "심야할증 포함 · 약 25분",
            )
        }
        // 막차 이후~자정: 끊김.
        if (time.isAfter(LAST_TRAIN_BOARDING)) {
            return LastTrainResult.Ended(
                firstTrainHour = FIRST_TRAIN.hour,
                firstTrainMinute = FIRST_TRAIN.minute,
                taxiFare = "약 28,000원",
                taxiDetail = "심야할증 포함 · 약 25분",
            )
        }
        val remaining = Duration.between(time, LAST_TRAIN_BOARDING).toMinutes().toInt()
        val level = LastTrainPolicy.levelFor(remaining)
        return LastTrainResult.Available(
            remainingMinutes = remaining,
            deadlineHour = LAST_TRAIN_BOARDING.hour,
            deadlineMinute = LAST_TRAIN_BOARDING.minute,
            stationName = "선릉역",
            bottleneck = when (level) {
                LastTrainLevel.RELAXED -> "도보 8분 + 환승 대기 (모의 데이터)"
                LastTrainLevel.IMMINENT -> "지금 나가야 해요 (모의 데이터)"
                LastTrainLevel.CRITICAL -> "막차가 곧 끊겨요 (모의 데이터)"
            },
            // 여유 단계는 지연 조회(placeholder), 임박부터는 즉시 노출(명세 4-1).
            taxiFare = if (level == LastTrainLevel.RELAXED) null else "약 12,000원",
        )
    }

    private companion object {
        /** 모의 막차 승차 데드라인 / 첫차 시각. */
        val LAST_TRAIN_BOARDING: LocalTime = LocalTime.of(23, 45)
        val FIRST_TRAIN: LocalTime = LocalTime.of(5, 34)
    }
}
