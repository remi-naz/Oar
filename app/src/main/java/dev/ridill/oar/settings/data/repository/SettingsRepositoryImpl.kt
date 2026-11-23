package dev.ridill.oar.settings.data.repository

import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.domain.util.DateUtil
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update

class SettingsRepositoryImpl(
    private val preferencesManager: PreferencesManager,
) : SettingsRepository {
    private val currentDate = MutableStateFlow(DateUtil.dateNow())
    private val preferences = preferencesManager.preferences

    override fun refreshCurrentDate() {
        currentDate.update { DateUtil.dateNow() }
    }

    override fun getCurrentAppTheme(): Flow<AppTheme> = preferences
        .mapLatest { it.appTheme }
        .distinctUntilChanged()

    override suspend fun updateAppTheme(theme: AppTheme) = preferencesManager
        .updateAppThem(theme)

    override fun getDynamicColorsEnabled(): Flow<Boolean> = preferences
        .mapLatest { it.dynamicColorsEnabled }
        .distinctUntilChanged()

    override suspend fun toggleDynamicColors(enabled: Boolean) = preferencesManager
        .updateDynamicColorsEnabled(enabled)
}