package dev.ridill.oar.settings.domain.repositoty

interface DataResetRepository {
    /**
     * Removes all user data from this device and cancels everything scheduled around it.
     * Theme, dynamic colors, app lock and similar app-level preferences are kept.
     */
    suspend fun clearAllLocalData()
}
