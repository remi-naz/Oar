package dev.ridill.oar.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.oar.account.domain.service.AccessTokenKeystoreService
import dev.ridill.oar.application.OarViewModel
import dev.ridill.oar.core.data.db.MIGRATION_5_6
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.data.preferences.PreferencesManagerImpl
import dev.ridill.oar.core.data.preferences.animPreferences.AnimPreferencesManager
import dev.ridill.oar.core.data.preferences.animPreferences.AnimPreferencesManagerImpl
import dev.ridill.oar.core.data.preferences.security.SecurityPreferencesManager
import dev.ridill.oar.core.data.preferences.security.SecurityPreferencesManagerImpl
import dev.ridill.oar.core.data.util.AndroidConnectivityObserver
import dev.ridill.oar.core.data.util.ConnectivityObserver
import dev.ridill.oar.core.domain.crashlytics.CrashlyticsManager
import dev.ridill.oar.core.domain.crashlytics.FirebaseCrashlyticsManager
import dev.ridill.oar.core.domain.crypto.Argon2CryptoManager
import dev.ridill.oar.core.domain.crypto.DefaultCryptoManager
import dev.ridill.oar.core.domain.crypto.DefaultKeystoreCryptoManager
import dev.ridill.oar.core.domain.crypto.KeystoreCryptoManager
import dev.ridill.oar.core.domain.crypto.PasswordBasedCryptoManager
import dev.ridill.oar.core.domain.remoteConfig.FirebaseRemoteConfigService
import dev.ridill.oar.core.domain.service.ExpEvalService
import dev.ridill.oar.core.domain.service.ReceiverService
import dev.ridill.oar.core.domain.util.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): OarDatabase = Room
        .databaseBuilder(
            context = context,
            klass = OarDatabase::class.java,
            name = OarDatabase.NAME
        )
        .addMigrations(MIGRATION_5_6)
        .fallbackToDestructiveMigration(dropAllTables = false)
        .build()

    @AppPreferences
    @Singleton
    @Provides
    fun provideAppPrefDataStoreInstance(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { context.preferencesDataStoreFile(PreferencesManager.NAME) },
        migrations = listOf()
    )

    @Provides
    fun providePreferencesManager(
        @AppPreferences dataStore: DataStore<Preferences>
    ): PreferencesManager = PreferencesManagerImpl(dataStore)

    @AnimPreferences
    @Singleton
    @Provides
    fun provideAnimPrefDataStoreInstance(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { context.preferencesDataStoreFile(AnimPreferencesManager.NAME) }
    )

    @Provides
    fun provideAnimPreferencesManager(
        @AnimPreferences dataStore: DataStore<Preferences>
    ): AnimPreferencesManager = AnimPreferencesManagerImpl(dataStore)

    @SecurityPreferences
    @Singleton
    @Provides
    fun provideSecurityPrefDataStoreInstance(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { context.preferencesDataStoreFile(SecurityPreferencesManager.NAME) }
    )

    @Provides
    fun provideSecurityPreferencesManager(
        @SecurityPreferences dataStore: DataStore<Preferences>,
        cryptoManager: KeystoreCryptoManager
    ): SecurityPreferencesManager = SecurityPreferencesManagerImpl(
        dataStore = dataStore,
        cryptoManager = cryptoManager
    )

    @Provides
    fun provideExpressionEvaluationService(): ExpEvalService = ExpEvalService()

    @ApplicationScope
    @Provides
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    fun provideReceiverService(
        @ApplicationContext context: Context
    ): ReceiverService = ReceiverService(context)

    @Provides
    fun provideOarEventBus(): EventBus<OarViewModel.OarEvent> = EventBus()

    @Provides
    fun provideDefaultPasswordBasedCryptoManager(): PasswordBasedCryptoManager =
        DefaultCryptoManager()

    @Argon2PasswordBasedCryptoManager
    @Provides
    fun provideArgon2PasswordBasedCryptoManager(): PasswordBasedCryptoManager =
        Argon2CryptoManager()

    @Provides
    fun provideKeystoreCryptoManager(): KeystoreCryptoManager = DefaultKeystoreCryptoManager()

    @AccessTokenPreferences
    @Singleton
    @Provides
    fun provideAccessTokenPrefDataStoreInstance(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        produceFile = { context.preferencesDataStoreFile(AccessTokenKeystoreService.NAME) }
    )

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    fun provideRemoteConfigService(json: Json): FirebaseRemoteConfigService =
        FirebaseRemoteConfigService(json = json)

    @Provides
    fun provideConnectivityObserver(
        @ApplicationContext context: Context
    ): ConnectivityObserver = AndroidConnectivityObserver(context)

    @Provides
    fun provideCrashlyticsManager(): CrashlyticsManager = FirebaseCrashlyticsManager()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AccessTokenPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AnimPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SecurityPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Argon2PasswordBasedCryptoManager