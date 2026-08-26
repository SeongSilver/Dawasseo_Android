package com.wakepoint.app.core.network

import com.wakepoint.app.data.auth.AuthHttpException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * 멱등한 네트워크 호출을 일시적 오류에 한해 지수 백오프로 재시도한다.
 *
 * 재시도 대상: 네트워크/타임아웃(IOException), 서버 일시 오류(HTTP 408/429/5xx).
 * 재시도 안 함: 그 외 4xx(요청 자체 오류)와 코루틴 취소(CancellationException).
 *
 * 반드시 멱등(GET, upsert, id 기준 PATCH/DELETE)한 호출에만 사용할 것.
 */
suspend fun <T> retryingNetwork(
    maxAttempts: Int = 3,
    initialDelayMs: Long = 400L,
    maxDelayMs: Long = 5_000L,
    block: suspend () -> T
): T {
    var attempt = 0
    var delayMs = initialDelayMs
    while (true) {
        try {
            return block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            attempt++
            if (attempt >= maxAttempts || !error.isTransient()) throw error
            delay(delayMs)
            // 지수 증가에 상한을 둬 시도 횟수를 늘려 써도 대기가 무한정 길어지지 않게 한다.
            delayMs = (delayMs * 2).coerceAtMost(maxDelayMs)
        }
    }
}

private fun Throwable.isTransient(): Boolean = when (this) {
    is IOException -> true
    is AuthHttpException -> statusCode == 408 || statusCode == 429 || statusCode in 500..599
    else -> false
}
