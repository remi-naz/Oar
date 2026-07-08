package dev.ridill.oar.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.oar.BuildConfig
import dev.ridill.oar.account.domain.repository.AuthRepository
import dev.ridill.oar.account.domain.service.AccessTokenService
import dev.ridill.oar.core.data.db.OarDatabase
import dev.ridill.oar.core.data.preferences.PreferencesManager
import dev.ridill.oar.core.data.preferences.security.SecurityPreferencesManager
import dev.ridill.oar.core.domain.crypto.CryptoManager
import dev.ridill.oar.core.domain.notification.NotificationHelper
import dev.ridill.oar.settings.data.local.ConfigDao
import dev.ridill.oar.settings.data.remote.GDriveApi
import dev.ridill.oar.settings.data.remote.interceptors.GoogleAccessTokenInterceptor
import dev.ridill.oar.settings.data.repository.BackupRepositoryImpl
import dev.ridill.oar.settings.data.repository.JSON_MIME_TYPE
import dev.ridill.oar.settings.domain.appInit.AppInitWorkManager
import dev.ridill.oar.settings.domain.appLock.AppLockServiceManager
import dev.ridill.oar.settings.domain.backup.BackupService
import dev.ridill.oar.settings.domain.backup.BackupWorkManager
import dev.ridill.oar.settings.domain.notification.AppInitNotificationHelper
import dev.ridill.oar.settings.domain.notification.BackupNotificationHelper
import dev.ridill.oar.settings.domain.repositoty.BackupRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsSingletonModule {

    @Provides
    fun provideConfigDao(database: OarDatabase): ConfigDao = database.configDao()

    @GoogleApis
    @Provides
    fun provideGoogleAccessTokenInterceptor(
        tokenService: AccessTokenService
    ): GoogleAccessTokenInterceptor = GoogleAccessTokenInterceptor(
        tokenService = tokenService
    )

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @GoogleApis
    @Provides
    fun provideGoogleApisHttpClient(
        @GoogleApis googleAccessTokenInterceptor: GoogleAccessTokenInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(googleAccessTokenInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @GoogleApis
    @Singleton
    @Provides
    fun provideGoogleApisRetrofit(
        @GoogleApis client: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(
            json.asConverterFactory(JSON_MIME_TYPE.toMediaType())
        )
        .baseUrl(BuildConfig.GOOGLE_APIS_BASE_URL)
        .client(client)
        .build()

    @Provides
    fun provideGDriveApi(@GoogleApis retrofit: Retrofit): GDriveApi =
        retrofit.create(GDriveApi::class.java)

    @Provides
    fun provideBackupService(
        @ApplicationContext context: Context,
        database: OarDatabase,
        cryptoManager: CryptoManager
    ): BackupService = BackupService(
        context = context,
        database = database,
        cryptoManager = cryptoManager
    )

    @Provides
    fun provideBackupRepository(
        @ApplicationContext context: Context,
        backupService: BackupService,
        gDriveApi: GDriveApi,
        preferencesManager: PreferencesManager,
        securityPreferencesManager: SecurityPreferencesManager,
        configDao: ConfigDao,
        backupWorkManager: BackupWorkManager,
        authRepository: AuthRepository,
        cryptoManager: CryptoManager,
        json: Json,
    ): BackupRepository = BackupRepositoryImpl(
        context = context,
        backupService = backupService,
        gDriveApi = gDriveApi,
        preferencesManager = preferencesManager,
        securityPreferencesManager = securityPreferencesManager,
        configDao = configDao,
        backupWorkManager = backupWorkManager,
        authRepo = authRepository,
        cryptoManager = cryptoManager,
        json = json,
    )

    @Provides
    fun provideBackupWorkManager(
        @ApplicationContext context: Context
    ): BackupWorkManager = BackupWorkManager(context)

    @BackupFeature
    @Provides
    fun provideBackupNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<String> = BackupNotificationHelper(context)

    @Provides
    fun provideAppLockServiceManager(
        @ApplicationContext context: Context
    ): AppLockServiceManager = AppLockServiceManager(context)

    @AppInitFeature
    @Provides
    fun provideAppInitNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<Unit> = AppInitNotificationHelper(context)

    @Provides
    fun provideAppInitWorkManager(
        @ApplicationContext context: Context
    ): AppInitWorkManager = AppInitWorkManager(context)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleApis

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackupFeature

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppInitFeature