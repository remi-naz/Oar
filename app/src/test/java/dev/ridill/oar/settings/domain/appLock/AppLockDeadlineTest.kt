package dev.ridill.oar.settings.domain.appLock

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppLockDeadlineTest {

    @Test
    fun noDeadline_isNotPassed() {
        assertThat(isDeadlinePassed(null, storedBootCount = 5, currentBootCount = 5, now = 1_000))
            .isFalse()
    }

    @Test
    fun beforeDeadline_isNotPassed() {
        assertThat(isDeadlinePassed(10_000, storedBootCount = 5, currentBootCount = 5, now = 9_999))
            .isFalse()
    }

    @Test
    fun atOrAfterDeadline_isPassed() {
        assertThat(isDeadlinePassed(10_000, storedBootCount = 5, currentBootCount = 5, now = 10_000))
            .isTrue()
        assertThat(isDeadlinePassed(10_000, storedBootCount = 5, currentBootCount = 5, now = 60_000))
            .isTrue()
    }

    @Test
    fun deadlineFromPreviousBoot_isPassedEvenIfClockIsBehind() {
        // elapsedRealtime restarted from zero after reboot, so now < deadline proves nothing
        assertThat(isDeadlinePassed(10_000, storedBootCount = 5, currentBootCount = 6, now = 500))
            .isTrue()
    }
}
