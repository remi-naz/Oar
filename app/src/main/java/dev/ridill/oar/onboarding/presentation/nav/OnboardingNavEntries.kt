package dev.ridill.oar.onboarding.presentation.nav

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.ridill.oar.account.presentation.util.rememberCredentialService
import dev.ridill.oar.application.RUN_CONFIG_RESTORE_EXTRA
import dev.ridill.oar.core.domain.util.BuildUtil
import dev.ridill.oar.core.ui.components.CollectFlowEffect
import dev.ridill.oar.core.ui.components.rememberMultiplePermissionsState
import dev.ridill.oar.core.ui.components.rememberSnackbarController
import dev.ridill.oar.core.ui.navigation.CurrencySelectionSheetRoute
import dev.ridill.oar.core.ui.navigation.DashboardRoute
import dev.ridill.oar.core.ui.navigation.OarNavigator
import dev.ridill.oar.core.ui.navigation.OnboardingRoute
import dev.ridill.oar.core.ui.navigation.ResultEffect
import dev.ridill.oar.core.ui.util.restartApplication
import dev.ridill.oar.onboarding.domain.model.OnboardingPage
import dev.ridill.oar.onboarding.presentation.OnboardingScreen
import dev.ridill.oar.onboarding.presentation.OnboardingViewModel
import java.util.Currency

// region Onboarding
@Suppress("LongMethod")
fun EntryProviderScope<NavKey>.onboardingEntries(navigator: OarNavigator) {
    entry<OnboardingRoute> {
        val viewModel: OnboardingViewModel = hiltViewModel()
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(
            pageCount = { OnboardingPage.entries.size }
        )
        val state by viewModel.state.collectAsStateWithLifecycle()
        val budgetInputState = viewModel.budgetInputState

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current
        val activity = LocalActivity.current

        val permissionsState = rememberMultiplePermissionsState(
            permissions = buildList {
                if (BuildUtil.isNotificationRuntimePermissionNeeded())
                    add(Manifest.permission.POST_NOTIFICATIONS)
            },
            onPermissionResult = viewModel::onPermissionsRequestResult
        )

        val credentialService = rememberCredentialService(context)
        val currentPage by remember(pagerState) { derivedStateOf { pagerState.currentPage } }

        LaunchedEffect(currentPage) {
            viewModel.onPageChange(currentPage)
        }

        val authorizationResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            onResult = { result ->
                if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
                result.data?.let(viewModel::onAuthorizationResult)
            }
        )

        ResultEffect<Currency> { currency ->
            viewModel.onCurrencySelected(currency)
        }

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is OnboardingViewModel.OnboardingEvent.NavigateToPage -> {
                    if (!pagerState.isScrollInProgress)
                        pagerState.animateScrollToPage(event.page.ordinal)
                }

                OnboardingViewModel.OnboardingEvent.LaunchPermissionsRequest -> {
                    permissionsState.launchRequest()
                }

                OnboardingViewModel.OnboardingEvent.LaunchAlarmPermissionsSettings -> {
                    if (BuildUtil.isApiLevelAtLeast31) {
                        context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    }
                }

                is OnboardingViewModel.OnboardingEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                OnboardingViewModel.OnboardingEvent.OnboardingConcluded -> {
                    navigator.replaceTop(DashboardRoute)
                }

                OnboardingViewModel.OnboardingEvent.RestartApplication -> {
                    context.restartApplication(editIntent = {
                        putExtra(
                            RUN_CONFIG_RESTORE_EXTRA,
                            true
                        )
                    })
                }

                is OnboardingViewModel.OnboardingEvent.StartAutoSignInFlow -> {
                    activity?.let {
                        val result = credentialService.startGetCredentialFlow(
                            filterByAuthorizedUsers = event.filterByAuthorizedAccounts,
                            activityContext = it
                        )
                        viewModel.onCredentialResult(result)
                    }
                }

                is OnboardingViewModel.OnboardingEvent.StartAuthorizationFlow -> {
                    authorizationResultLauncher.launch(
                        IntentSenderRequest.Builder(event.pendingIntent)
                            .build()
                    )
                }

                OnboardingViewModel.OnboardingEvent.StartManualSignInFlow -> {
                    activity?.let {
                        val result = credentialService.startManualGetCredentialFlow(it)
                        viewModel.onCredentialResult(result)
                    }
                }
            }
        }

        OnboardingScreen(
            snackbarController = snackbarController,
            pagerState = pagerState,
            permissionsState = permissionsState,
            state = state,
            budgetInputState = budgetInputState,
            navigateToCurrencySelection = {
                navigator.navigate(CurrencySelectionSheetRoute(preSelectedCurrCode = state.appCurrency.currencyCode))
            },
            actions = viewModel
        )
    }
}

// endregion
