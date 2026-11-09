package dev.ridill.oar.settings.domain.appLock

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppAutoLockIntervalTest {

    @Test
    fun fromName_resolvesCurrentOptions() {
        AppAutoLockInterval.entries.forEach { interval ->
            assertThat(AppAutoLockInterval.fromName(interval.name)).isEqualTo(interval)
        }
    }

    @Test
    fun fromName_fallsBackToDefaultForRemovedOptions() {
        assertThat(AppAutoLockInterval.fromName("FIVE_MINUTE")).isEqualTo(AppAutoLockInterval.Default)
        assertThat(AppAutoLockInterval.fromName("TEN_MINUTE")).isEqualTo(AppAutoLockInterval.Default)
    }

    @Test
    fun fromName_fallsBackToDefaultWhenNothingStored() {
        assertThat(AppAutoLockInterval.fromName(null)).isEqualTo(AppAutoLockInterval.Default)
    }
}
