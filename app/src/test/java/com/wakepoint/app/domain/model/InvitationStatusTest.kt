package com.wakepoint.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class InvitationStatusTest {
    @Test
    fun fromValue_mapsEveryServerValue() {
        assertEquals(InvitationStatus.Pending, InvitationStatus.fromValue("pending"))
        assertEquals(InvitationStatus.Accepted, InvitationStatus.fromValue("accepted"))
        assertEquals(InvitationStatus.Rejected, InvitationStatus.fromValue("rejected"))
        assertEquals(InvitationStatus.Expired, InvitationStatus.fromValue("expired"))
        assertEquals(InvitationStatus.Cancelled, InvitationStatus.fromValue("cancelled"))
    }

    @Test
    fun fromValue_isCaseAndWhitespaceInsensitive() {
        assertEquals(InvitationStatus.Accepted, InvitationStatus.fromValue(" ACCEPTED "))
    }

    @Test
    fun fromValue_fallsBackToPendingForUnknown() {
        assertEquals(InvitationStatus.Pending, InvitationStatus.fromValue(null))
        assertEquals(InvitationStatus.Pending, InvitationStatus.fromValue(""))
        assertEquals(InvitationStatus.Pending, InvitationStatus.fromValue("arrived"))
    }

    @Test
    fun toValue_roundTripsThroughFromValue() {
        InvitationStatus.entries.forEach { status ->
            assertEquals(status, InvitationStatus.fromValue(status.toValue()))
        }
    }
}
