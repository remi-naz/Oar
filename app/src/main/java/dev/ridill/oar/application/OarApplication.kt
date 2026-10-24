package dev.ridill.oar.application

import android.app.Application
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import dev.ridill.oar.core.data.logging.FileLoggingTree
import dev.ridill.oar.core.data.logging.LogFileManager
import dev.ridill.oar.core.domain.crypto.KeystoreCryptoManager
import dev.ridill.oar.core.domain.logging.LogRedactor
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.domain.util.logE
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.oar.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.security.KeyStore
import javax.inject.Inject

@HiltAndroidApp
class OarApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var logFileManager: LogFileManager

    @Inject
    lateinit var logRedactor: LogRedactor

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupTimber()
        clearLegacyAccessTokenCache()
    }

    private fun setupTimber() {
        if (BuildUtil.isDebug || BuildUtil.isInternal) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(FileLoggingTree(logFileManager, logRedactor))
        logUncaughtExceptions()
        logI { "App started - version ${BuildUtil.versionName}" }
    }

    // Persists the crash to the log file before handing over to the previous handler (Crashlytics)
    private fun logUncaughtExceptions() {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logE(throwable) { "Uncaught exception on thread ${thread.name}" }
            logFileManager.flushBlocking()
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    // Drive access tokens used to be cached locally; they're now fetched from Play Services on demand
    private fun clearLegacyAccessTokenCache() = applicationScope.launch(Dispatchers.IO) {
        try {
            preferencesDataStoreFile(LEGACY_ACCESS_TOKEN_PREFS_NAME)
                .takeIf { it.exists() }
                ?.delete()

            val keyStore = KeyStore.getInstance(KeystoreCryptoManager.ANDROID_KEYSTORE)
                .apply { load(null) }
            if (keyStore.containsAlias(LEGACY_ACCESS_TOKEN_KEY_ALIAS))
                keyStore.deleteEntry(LEGACY_ACCESS_TOKEN_KEY_ALIAS)
        } catch (t: Throwable) {
            t.rethrowIfCoroutineCancellation()
            logE(t) { "Failed to clear legacy access token cache" }
        }
    }

    companion object {
        private const val LEGACY_ACCESS_TOKEN_PREFS_NAME = "access_token_preferences"
        private const val LEGACY_ACCESS_TOKEN_KEY_ALIAS = "oar_access_token_key"
    }
}