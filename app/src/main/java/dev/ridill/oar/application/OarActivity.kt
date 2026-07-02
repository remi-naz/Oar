package dev.ridill.oar.application

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptCallback
import androidx.biometric.auth.startClass2BiometricOrCredentialAuthentication
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.BiometricUtil
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.domain.util.LocaleUtil
import dev.ridill.oar.core.domain.util.logI
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.circularReveal
import dev.ridill.oar.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.oar.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.oar.core.ui.navigation.BottomSheetSceneStrategy
import dev.ridill.oar.core.ui.navigation.DashboardRoute
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.OnboardingRoute
import dev.ridill.oar.core.ui.navigation.buildOarEntryProvider
import dev.ridill.oar.core.ui.navigation.rememberNavResultBusNavEntryDecorator
import dev.ridill.oar.core.ui.navigation.rememberOarNavigator
import dev.ridill.oar.core.ui.theme.OarTheme
import dev.ridill.oar.core.ui.util.LocalCurrencyPreference
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.core.ui.util.isPermissionGranted
import dev.ridill.oar.settings.domain.modal.AppTheme
import dev.ridill.oar.settings.presentation.securitySettings.AppLockedOverlay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OarActivity : AppCompatActivity() {

    private val viewModel: OarViewModel by viewModels()
    private val pendingDeepLink = mutableStateOf<NavKey?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        this.intent?.let {
            val runConfigRestore = it.getBooleanExtra(RUN_CONFIG_RESTORE_EXTRA, false)
            logI(OarActivity::class.simpleName) { "$RUN_CONFIG_RESTORE_EXTRA = $runConfigRestore" }
            if (runConfigRestore) viewModel.startConfigRestore()
        }
        pendingDeepLink.value = OarDeepLink.resolve(intent)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            OarViewModel.OarEvent.LaunchBiometricAuthentication -> {
                                checkAndLaunchBiometric()
                            }
                        }
                    }
                }

                launch {
                    viewModel.screenSecurityEnabled.collectLatest { enabled ->
                        if (enabled) {
                            window.setFlags(
                                WindowManager.LayoutParams.FLAG_SECURE,
                                WindowManager.LayoutParams.FLAG_SECURE
                            )
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                    }
                }
            }
        }

        setContent {
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle(AppTheme.SYSTEM_DEFAULT)
            val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle(false)
            val dynamicTheme by viewModel.dynamicThemeEnabled.collectAsStateWithLifecycle(false)
            val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle(false)
            val appLockErrorMessage by viewModel.appLockAuthErrorMessage.collectAsStateWithLifecycle()
            val appCurrencyPreference by viewModel.currencyPreference
                .collectAsStateWithLifecycle(LocaleUtil.defaultCurrency)
            val darkTheme = when (appTheme) {
                AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            CollectFlowEffect(snapshotFlow { darkTheme }) { isDarkTheme ->
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                        detectDarkMode = { isDarkTheme }
                    ),
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                        detectDarkMode = { isDarkTheme }
                    )
                )
            }

            val backStack = rememberNavBackStack(DashboardRoute)
            val navigator = rememberOarNavigator(backStack)

            LaunchedEffect(showOnboarding) {
                if (showOnboarding) {
                    backStack.clear()
                    backStack.add(OnboardingRoute)
                }
            }

            val deepLinkKey = pendingDeepLink.value
            LaunchedEffect(deepLinkKey, showOnboarding) {
                if (deepLinkKey != null && !showOnboarding) {
                    backStack.clear()
                    backStack.add(DashboardRoute)
                    backStack.add(deepLinkKey)
                    pendingDeepLink.value = null
                }
            }

            CompositionLocalProvider(
                LocalCurrencyPreference provides appCurrencyPreference
            ) {
                ScreenContent(
                    navigator = navigator,
                    darkTheme = darkTheme,
                    dynamicTheme = dynamicTheme,
                    appLockErrorMessage = appLockErrorMessage,
                    isAppLocked = isAppLocked,
                    onUnlockClick = ::checkAndLaunchBiometric,
                    closeApp = ::finish
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAppPermissions()
        viewModel.refreshTransactionAutoDetectFeatureEnabledState()
    }

    override fun onStop() {
        viewModel.startAppAutoLockTimerIfApplicable()
        super.onStop()
    }

    override fun onStart() {
        viewModel.startAppUnlockOrServiceStop()
        super.onStart()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val runConfigRestore = intent.getBooleanExtra(RUN_CONFIG_RESTORE_EXTRA, false)
        if (runConfigRestore) viewModel.startConfigRestore()
        pendingDeepLink.value = OarDeepLink.resolve(intent)
    }

    private fun checkAppPermissions() {
        if (viewModel.showOnboarding.value) return
        val isSmsPermissionGranted = isPermissionGranted(Manifest.permission.RECEIVE_SMS)
        viewModel.onSmsPermissionCheck(isSmsPermissionGranted)

        if (BuildUtil.isNotificationRuntimePermissionNeeded()) {
            val isNotificationPermissionGranted =
                isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
            viewModel.onNotificationPermissionCheck(isNotificationPermissionGranted)
        }
    }

    private fun checkAndLaunchBiometric() {
        val canAuthenticate = BiometricManager.from(this)
            .canAuthenticate(BiometricUtil.DefaultBiometricAuthenticators) == BiometricManager.BIOMETRIC_SUCCESS
        if (!canAuthenticate) return

        val title = getString(
            R.string.biometric_prompt_title_app_name,
            getString(R.string.app_name)
        )
        val subtitle = getString(R.string.biometric_or_screen_lock_prompt_message)
        val authPromptCallback = object : AuthPromptCallback() {
            override fun onAuthenticationError(
                activity: FragmentActivity?,
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(activity, errorCode, errString)
                viewModel.updateAppLockErrorMessage(UiText.DynamicString(errString.toString()))
            }

            override fun onAuthenticationSucceeded(
                activity: FragmentActivity?,
                result: BiometricPrompt.AuthenticationResult
            ) {
                super.onAuthenticationSucceeded(activity, result)
                viewModel.onAppLockAuthSucceeded()
            }

            override fun onAuthenticationFailed(activity: FragmentActivity?) {
                super.onAuthenticationFailed(activity)
                viewModel.updateAppLockErrorMessage(UiText.StringResource(R.string.error_biometric_auth_failed))
            }
        }

        startClass2BiometricOrCredentialAuthentication(
            title = title,
            subtitle = subtitle,
            callback = authPromptCallback
        )
    }
}

const val RUN_CONFIG_RESTORE_EXTRA = "RUN_CONFIG_RESTORE_EXTRA"

@Composable
private fun ScreenContent(
    navigator: OarNavigator,
    darkTheme: Boolean,
    dynamicTheme: Boolean,
    appLockErrorMessage: UiText?,
    isAppLocked: Boolean,
    onUnlockClick: () -> Unit,
    closeApp: () -> Unit
) {
    val unlockIconAnimProgress = remember { Animatable(0f) }
    val lockScreenVisibilityProgress = remember { Animatable(0f) }
    var showAppLock by rememberSaveable { mutableStateOf(isAppLocked) }

    LaunchedEffect(isAppLocked) {
        if (isAppLocked) {
            unlockIconAnimProgress.snapTo(0f)
            showAppLock = true
            lockScreenVisibilityProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = APP_SCREEN_VISIBILITY_ANIM_DURATION)
            )
        } else {
            unlockIconAnimProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = UNLOCK_ICON_ANIM_DURATION)
            )
            lockScreenVisibilityProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = APP_SCREEN_VISIBILITY_ANIM_DURATION)
            )
            showAppLock = false
        }
    }

    OarTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicTheme
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val motionScheme = MaterialTheme.motionScheme
            val slideAnimationSpec = motionScheme.slowSpatialSpec<IntOffset>()
            val scaleAnimation = motionScheme.slowSpatialSpec<Float>()
            NavDisplay(
                backStack = navigator.backStack,
                onBack = navigator::goBack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                    rememberNavResultBusNavEntryDecorator(),
                ),
                entryProvider = buildOarEntryProvider(
                    navigator = navigator,
                    motionScheme = motionScheme
                ),
                sceneStrategies = listOf(
                    SinglePaneSceneStrategy(),
                    BottomSheetSceneStrategy()
                ),
                transitionSpec = {
                    ContentTransform(
                        targetContentEnter = slideInHorizontallyWithFadeIn(
                            slideAnimationSpec = slideAnimationSpec,
                            fadeAnimationSpec = scaleAnimation,
                            initialOffsetX = { it / 2 }
                        ),
                        initialContentExit = scaleOut(
                            animationSpec = scaleAnimation,
                            targetScale = NAV_ANIM_SCALE,
                            transformOrigin = TransformOrigin.Center,
                        ) + fadeOut(animationSpec = scaleAnimation),
                    )
                },
                popTransitionSpec = {
                    ContentTransform(
                        targetContentEnter = scaleIn(
                            animationSpec = scaleAnimation,
                            initialScale = NAV_ANIM_SCALE,
                            transformOrigin = TransformOrigin.Center,
                        ) + fadeIn(animationSpec = scaleAnimation),
                        initialContentExit = slideOutHorizontallyWithFadeOut(
                            slideAnimationSpec = slideAnimationSpec,
                            fadeAnimationSpec = scaleAnimation,
                            targetOffsetX = { it / 2 }
                        )
                    )
                },
                predictivePopTransitionSpec = {
                    ContentTransform(
                        targetContentEnter = scaleIn(
                            animationSpec = scaleAnimation,
                            initialScale = NAV_ANIM_SCALE,
                            transformOrigin = TransformOrigin.Center,
                        ) + fadeIn(animationSpec = scaleAnimation),
                        initialContentExit = slideOutHorizontallyWithFadeOut(
                            slideAnimationSpec = slideAnimationSpec,
                            fadeAnimationSpec = scaleAnimation,
                            targetOffsetX = { it / 2 }
                        )
                    )
                }
            )

            if (showAppLock) {
                AppLockedOverlay(
                    onBack = closeApp,
                    unlockAnimProgress = unlockIconAnimProgress.asState(),
                    onUnlockClick = onUnlockClick,
                    errorMessage = appLockErrorMessage,
                    modifier = Modifier
                        .circularReveal(
                            transitionProgress = lockScreenVisibilityProgress.asState(),
                            revealFrom = Offset(0f, 0f)
                        )
                )
            }
        }
    }
}

private const val UNLOCK_ICON_ANIM_DURATION = 800
private const val APP_SCREEN_VISIBILITY_ANIM_DURATION = 500
internal const val NAV_ANIM_SCALE = 0.95f