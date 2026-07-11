package dev.ridill.oar.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.data.preferences.security.SecurityPreferencesManager
import dev.ridill.oar.core.domain.crypto.PasswordBasedCryptoManager
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.settings.data.local.ConfigDao
import dev.ridill.oar.settings.data.repository.BackupSettingsRepositoryImpl
import dev.ridill.oar.settings.data.repository.SettingsRepositoryImpl
import dev.ridill.oar.settings.domain.backup.BackupWorkManager
import dev.ridill.oar.settings.domain.repositoty.BackupSettingsRepository
import dev.ridill.oar.settings.domain.repositoty.SettingsRepository
import dev.ridill.oar.settings.presentation.backupEncryption.BackupEncryptionViewModel
import dev.ridill.oar.settings.presentation.backupSettings.BackupSettingsViewModel
import dev.ridill.oar.settings.presentation.securitySettings.SecuritySettingsViewModel
import dev.ridill.oar.settings.presentation.settings.SettingsViewModel

@Module
@InstallIn(ViewModelComponent::class)
object SettingsViewModelModule {

    @Provides
    fun provideSettingsRepository(
        preferencesManager: PreferencesManager,
    ): SettingsRepository = SettingsRepositoryImpl(
        preferencesManager = preferencesManager,
    )

    @Provides
    fun provideSettingsEventBus(): EventBus<SettingsViewModel.SettingsEvent> = EventBus()

    @Provides
    fun provideBackupSettingsEventBus(): EventBus<BackupSettingsViewModel.BackupSettingsEvent> =
        EventBus()

    @Provides
    fun provideBackupSettingsRepository(
        dao: ConfigDao,
        preferencesManager: PreferencesManager,
        securityPreferencesManager: SecurityPreferencesManager,
        backupWorkManager: BackupWorkManager,
        @Argon2PasswordBasedCryptoManager cryptoManager: PasswordBasedCryptoManager,
        legacyCryptoManager: PasswordBasedCryptoManager
    ): BackupSettingsRepository = BackupSettingsRepositoryImpl(
        dao = dao,
        preferencesManager = preferencesManager,
        backupWorkManager = backupWorkManager,
        securityPreferencesManager = securityPreferencesManager,
        argon2CryptoManager = cryptoManager,
        legacyCryptoManager = legacyCryptoManager
    )

    @Provides
    fun provideSecuritySettingsEventBus(): EventBus<SecuritySettingsViewModel.SecuritySettingsEvent> =
        EventBus()

    @Provides
    fun provideBackupEncryptionEventBus(): EventBus<BackupEncryptionViewModel.BackupEncryptionEvent> =
        EventBus()
}