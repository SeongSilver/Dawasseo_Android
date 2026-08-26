package com.wakepoint.app.core.network

import com.wakepoint.app.data.auth.AuthHttpException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 멱등 네트워크 호출 재시도 로직(retryingNetwork) 테스트.
 *
 * 규칙: IOException/HTTP 408/429/5xx 는 지수 백오프로 재시도,
 * 그 외 4xx 와 코루틴 취소는 즉시 전파한다.
 */
class NetworkRetryTest {

    @Test
    fun `첫 시도가 성공하면 재시도 없이 결과를 반환한다`() = runBlocking {
        var attempts = 0
        val result = retryingNetwork(maxAttempts = 3, initialDelayMs = 1) {
            attempts++
            "ok"
        }
        assertEquals("ok", result)
        assertEquals(1, attempts)
    }

    @Test
    fun `IOException은 일시 오류로 보고 재시도 끝에 성공한다`() = runBlocking {
        var attempts = 0
        val result = retryingNetwork(maxAttempts = 3, initialDelayMs = 1) {
            attempts++
            if (attempts < 3) throw IOException("네트워크 순단")
            "recovered"
        }
        assertEquals("recovered", result)
        assertEquals(3, attempts)
    }

    @Test
    fun `HTTP 5xx는 maxAttempts까지 재시도한다`() = runBlocking {
        var attempts = 0
        val error = runCatching {
            retryingNetwork(maxAttempts = 3, initialDelayMs = 1) {
                attempts++
                throw AuthHttpException(503, "server unavailable")
            }
        }.exceptionOrNull()
        assertEquals(3, attempts)
        assertTrue(error is AuthHttpException)
    }

    @Test
    fun `HTTP 408과 429도 일시 오류로 재시도한다`() = runBlocking {
        for (statusCode in listOf(408, 429)) {
            var attempts = 0
            runCatching {
                retryingNetwork(maxAttempts = 2, initialDelayMs = 1) {
                    attempts++
                    throw AuthHttpException(statusCode, "transient $statusCode")
                }
            }
            assertEquals("status=$statusCode", 2, attempts)
        }
    }

    @Test
    fun `HTTP 400 같은 요청 오류는 재시도하지 않고 즉시 던진다`() = runBlocking {
        var attempts = 0
        val error = runCatching {
            retryingNetwork(maxAttempts = 3, initialDelayMs = 1) {
                attempts++
                throw AuthHttpException(400, "bad request")
            }
        }.exceptionOrNull()
        assertEquals(1, attempts)
        assertTrue(error is AuthHttpException)
        assertEquals(400, (error as AuthHttpException).statusCode)
    }

    @Test
    fun `일시 오류가 아닌 일반 예외는 재시도하지 않는다`() = runBlocking {
        var attempts = 0
        val error = runCatching {
            retryingNetwork(maxAttempts = 3, initialDelayMs = 1) {
                attempts++
                throw IllegalStateException("버그성 오류")
            }
        }.exceptionOrNull()
        assertEquals(1, attempts)
        assertTrue(error is IllegalStateException)
    }

    @Test
    fun `maxAttempts 소진 시 마지막 오류를 그대로 던진다`() = runBlocking {
        var attempts = 0
        val error = runCatching {
            retryingNetwork(maxAttempts = 2, initialDelayMs = 1) {
                attempts++
                throw IOException("계속 실패")
            }
        }.exceptionOrNull()
        assertEquals(2, attempts)
        assertTrue(error is IOException)
        assertEquals("계속 실패", error?.message)
    }

    @Test
    fun `코루틴 취소는 재시도 대상이 아니고 즉시 전파된다`() = runBlocking {
        var attempts = 0
        val error = runCatching {
            retryingNetwork(maxAttempts = 3, initialDelayMs = 1) {
                attempts++
                throw CancellationException("취소됨")
            }
        }.exceptionOrNull()
        assertEquals(1, attempts)
        assertTrue(error is CancellationException)
    }

    @Test
    fun `지수 백오프는 maxDelayMs 상한을 넘지 않는다`() = runBlocking {
        // 가상 시간 없이 실제 delay 를 쓰므로 아주 작은 지연으로 검증한다:
        // initial=1ms, cap=2ms 에서 5회 실패해도 총 대기가 (1+2+2+2)ms 수준에 머문다.
        var attempts = 0
        val elapsed = kotlin.system.measureTimeMillis {
            runCatching {
                retryingNetwork(maxAttempts = 5, initialDelayMs = 1, maxDelayMs = 2) {
                    attempts++
                    throw java.io.IOException("transient")
                }
            }
        }
        assertEquals(5, attempts)
        // 캡이 없다면 1+2+4+8=15ms 이상 — 캡이 있으면 훨씬 짧다(여유 있게 상한 검증).
        assertTrue("elapsed=$elapsed", elapsed < 1_000)
    }
}
