package com.wakepoint.app.core.error

import java.io.IOException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserFacingErrorTest {

    @Test
    fun nullOrBlank_returnsGeneric() {
        assertEquals(UserFacingError.GENERIC, UserFacingError.of(null as Throwable?))
        assertEquals(UserFacingError.GENERIC, UserFacingError.of("" as String?))
        assertEquals(UserFacingError.GENERIC, UserFacingError.of("   "))
    }

    @Test
    fun ioException_mapsToNetworkMessage() {
        assertEquals("네트워크 연결을 확인해 주세요.", UserFacingError.of(IOException("boom")))
        assertEquals("네트워크 연결을 확인해 주세요.", UserFacingError.of(UnknownHostException("no host")))
    }

    @Test
    fun invalidCredentials_mapsToFriendlyMessage() {
        assertEquals(
            "이메일 또는 비밀번호가 올바르지 않아요.",
            UserFacingError.of("Invalid login credentials")
        )
    }

    @Test
    fun duplicateKey_mapsToFriendlyMessage() {
        assertEquals("이미 등록되어 있어요.", UserFacingError.of("duplicate key value violates unique constraint"))
        assertEquals("이미 등록되어 있어요.", UserFacingError.of("error code 23505"))
    }

    @Test
    fun cleanKoreanMessage_passesThrough() {
        val message = "이메일과 비밀번호를 입력해주세요."
        assertEquals(message, UserFacingError.of(message))
    }

    @Test
    fun rawPostgrestOrSqlError_neverLeaks() {
        // 원문 DB/PostgREST 오류는 사용자에게 그대로 노출되지 않아야 한다.
        val raw = "null value in column \"owner_id\" violates not-null constraint"
        val mapped = UserFacingError.of(raw)
        assertEquals(UserFacingError.GENERIC, mapped)
        assertFalse(mapped.contains("constraint"))

        val pgrst = "PGRST125: Invalid path specified in request URL"
        assertEquals(UserFacingError.GENERIC, UserFacingError.of(pgrst))
    }

    @Test
    fun tooManyRequests_mapsToRateLimitMessage() {
        assertTrue(UserFacingError.of("rate limit exceeded").contains("잠시 후"))
    }
}
