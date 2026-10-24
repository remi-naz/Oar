package dev.ridill.oar.onboarding.presentation

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import dev.ridill.oar.core.domain.util.One
import dev.ridill.oar.core.ui.components.MultiplePermissionsState
import dev.ridill.oar.core.ui.components.OarScaffold
import dev.ridill.oar.core.ui.components.SnackbarController
import dev.ridill.oar.core.ui.theme.BorderWidthStandard
import dev.ridill.oar.core.ui.theme.spacing
import dev.ridill.oar.onboarding.domain.model.OnboardingPage
import dev.ridill.oar.onboarding.presentation.components.PermissionsPage
import dev.ridill.oar.onboarding.presentation.components.SetupBudgetCyclesPage
import dev.ridill.oar.onboarding.presentation.components.SignInAndDataRestore
import dev.ridill.oar.onboarding.presentation.components.WelcomeMessagePage

@Composable
fun OnboardingScreen(
    snackbarController: SnackbarController,
    pagerState: PagerState,
    permissionsState: MultiplePermissionsState,
    state: OnboardingState,
    budgetInputState: TextFieldState,
    navigateToCurrencySelection: () -> Unit,
    actions: OnboardingActions
) {
    val view = LocalView.current
    val isBackgroundLight = MaterialTheme.colorScheme.primary.luminance() > 0.5f
    if (!view.isInEditMode) {
        // Match status bar icons to the primary background for Onboarding Screen only
        // and revert to the original value when screen disposes
        DisposableEffect(view, isBackgroundLight) {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            val originalLightStatusBarValue = insetsController.isAppearanceLightStatusBars
            insetsController.isAppearanceLightStatusBars = isBackgroundLight
            onDispose {
                insetsController.isAppearanceLightStatusBars = originalLightStatusBarValue
            }
        }
    }
    OarScaffold(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        snackbarController = snackbarController,
        modifier = Modifier
            .imePadding()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(Float.One)
            ) { page ->
                when (page) {
                    OnboardingPage.WELCOME.ordinal -> {
                        WelcomeMessagePage()
                    }

                    OnboardingPage.APP_PERMISSIONS.ordinal -> {
                        PermissionsPage(
                            permissionsState = permissionsState,
                            onGivePermissionClick = actions::onGivePermissionsClick,
                            onSkipClick = actions::onSkipPermissionsClick
                        )
                    }

                    OnboardingPage.ACCOUNT_SIGN_IN_AND_DATA_RESTORE.ordinal -> {
                        SignInAndDataRestore(
                            state = state.signInAndDataRestoreState,
                            authState = state.authState,
                            onSignInClick = actions::onSignInClick,
                            onSignInSkip = actions::onSkipSignInClick,
                            restoreState = state.dataRestoreState,
                            onCheckForBackupClick = actions::onCheckOrRestoreClick,
                            onSkipClick = actions::onDataRestoreSkip,
                            showEncryptionPasswordInput = state.showEncryptionPasswordInput,
                            onEncryptionPasswordInputDismiss = actions::onEncryptionPasswordInputDismiss,
                            onEncryptionPasswordSubmit = actions::onEncryptionPasswordSubmit,
                            appRestartTimer = state.appRestartTimer
                        )
                    }

                    OnboardingPage.SETUP_BUDGET_CYCLES.ordinal -> {
                        SetupBudgetCyclesPage(
                            budgetInput = budgetInputState,
                            selectedCurrency = state.appCurrency,
                            onCurrencyClick = navigateToCurrencySelection,
                            onStartBudgetingClick = actions::onStartBudgetingClick
                        )
                    }
                }
            }

            WelcomeFlowProgress(
                pageCount = pagerState.pageCount,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WelcomeFlowProgress(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            MaterialTheme.spacing.medium,
            Alignment.CenterHorizontally
        )
    ) {
        repeat(pageCount) { page ->
            val isCurrentOrPrevious = remember(currentPage) {
                page <= currentPage
            }
            val backgroundColor by animateColorAsState(
                targetValue = if (isCurrentOrPrevious) LocalContentColor.current
                else Color.Transparent,
                label = "PageIndicatorBackgroundColor"
            )
            Box(
                modifier = Modifier
                    .size(WelcomeFlowProgressIndicatorSize)
                    .clip(CircleShape)
                    .border(
                        width = BorderWidthStandard,
                        color = LocalContentColor.current,
                        shape = CircleShape
                    )
                    .drawBehind {
                        drawCircle(color = backgroundColor)
                    }
            )
        }
    }
}

private val WelcomeFlowProgressIndicatorSize = 12.dp