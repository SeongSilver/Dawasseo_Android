package com.wakepoint.app.core.alarm

import com.wakepoint.app.domain.model.TimeAlarm

/**
 * 시간 알람을 OS 스케줄(AlarmManager)에 등록/해제한다.
 * 발화 시각 계산은 [TimeAlarmSchedule](순수·테스트됨)에 위임하고, 여기서는 OS 연동만 담당한다.
 */
interface TimeAlarmScheduler {
    /** [alarm] 의 다음 발화를 예약한다. 비활성이거나 발화 시각이 없으면 기존 예약을 해제한다. */
    fun schedule(alarm: TimeAlarm)

    /** 예약을 해제한다. */
    fun cancel(alarmId: String)

    /** 정확 알람 예약이 가능한지(API 31/32 에서 사용자 허용 여부). */
    fun canScheduleExact(): Boolean
}
