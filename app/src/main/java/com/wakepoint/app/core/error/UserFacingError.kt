package com.wakepoint.app.core.error

import java.io.IOException

/**
 * 예외/서버 오류를 사용자가 이해할 수 있는 한국어 메시지로 변환한다.
 *
 * - null·빈 메시지 → 무음 실패 방지용 일반 안내
 * - PostgREST/SQL 코드(23505 등)·원문 오류는 사용자에게 그대로 노출하지 않고 매핑
 * - 우리가 직접 던진 깔끔한 한국어 메시지는 그대로 노출
 */
object UserFacingError {
    const val GENERIC = "문제가 발생했어요. 잠시 후 다시 시도해 주세요."
    private const val NETWORK = "네트워크 연결을 확인해 주세요."

    fun of(throwable: Throwable?): String {
        if (throwable == null) return GENERIC
        if (throwable is IOException) return NETWORK
        return of(throwable.message)
    }

    fun of(message: String?): String {
        val raw = message?.trim().orEmpty()
        if (raw.isBlank()) return GENERIC
        val lower = raw.lowercase()
        return when {
            lower.contains("unable to resolve host") ||
                lower.contains("failed to connect") ||
                lower.contains("timeout") ||
                lower.contains("timed out") ||
                lower.contains("network") -> NETWORK

            lower.contains("invalid login credentials") ->
                "이메일 또는 비밀번호가 올바르지 않아요."
            lower.contains("email not confirmed") ->
                "이메일 인증이 완료되지 않았어요. 받은 메일을 확인해 주세요."
            lower.contains("already registered") ||
                lower.contains("already been registered") ||
                lower.contains("user already") ->
                "이미 가입된 이메일이에요."
            lower.contains("23505") ||
                lower.contains("duplicate key") ||
                lower.contains("unique constraint") ->
                "이미 등록되어 있어요."
            lower.contains("23503") || lower.contains("foreign key") ->
                "요청을 처리할 수 없어요. 대상을 다시 확인해 주세요."
            lower.contains("weak password") || lower.contains("password should be") ->
                "비밀번호가 너무 약해요. 6자 이상으로 설정해 주세요."
            lower.contains("rate limit") ||
                lower.contains("over_email_send_rate") ||
                lower.contains("too many") ||
                lower.contains("429") ->
                "요청이 너무 잦아요. 잠시 후 다시 시도해 주세요."
            lower.contains("provider is not enabled") ->
                "지금은 사용할 수 없는 로그인 방식이에요."

            isCleanKoreanMessage(raw) -> raw
            else -> GENERIC
        }
    }

    /** 우리가 직접 만든 안내 문구(한글 + 원문 오류 마커 없음)만 그대로 노출하기 위한 판별. */
    private fun isCleanKoreanMessage(message: String): Boolean {
        val hasHangul = message.any { it in '가'..'힣' }
        val lower = message.lowercase()
        val hasRawMarker = RAW_MARKERS.any { lower.contains(it) }
        return hasHangul && !hasRawMarker && message.length <= 80
    }

    private val RAW_MARKERS = listOf(
        "constraint", "violates", "column", "relation", "pgrst",
        "http", "exception", "{", "}", "sql", "supabase", "null value"
    )
}
