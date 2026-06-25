package dev.ridill.oar.core.ui.navigation.destinations

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.Empty
import dev.ridill.oar.core.domain.util.NewLine
import dev.ridill.oar.core.ui.components.navigateUpWithResult
import dev.ridill.oar.tags.presentation.tagSelection.SingleTagSelectionSheet

data object TagSelectionSheetSpec : BottomSheetSpec {

    override val route: String
        get() = """
            tag_selection
            ?$ARG_PRE_SELECTED_ID={$ARG_PRE_SELECTED_ID}
        """.trimIndent()
            .replace(String.NewLine, String.Empty)

    override val labelRes: Int
        get() = R.string.destination_tag_selection

    override val arguments: List<NamedNavArgument>
        get() = listOf(
            navArgument(ARG_PRE_SELECTED_ID) {
                type = NavType.LongArrayType
                nullable = false
                defaultValue = longArrayOf()
            }
        )

    fun routeWithArgs(
        preselectedId: Long? = null,
    ) = route
        .replace(
            oldValue = "$ARG_PRE_SELECTED_ID={$ARG_PRE_SELECTED_ID}",
            newValue = preselectedId?.toString().orEmpty()
        )

    const val SELECTED_TAG_ID = "SELECTED_TAG_IDS"

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val preSelectedId = navBackStackEntry.arguments
            ?.getLong(ARG_PRE_SELECTED_ID)

        SingleTagSelectionSheet(
            preSelectedId = preSelectedId,
            onDismiss = navController::navigateUp,
            onConfirm = { selectedId ->
                navController.navigateUpWithResult(
                    key = SELECTED_TAG_ID,
                    result = selectedId
                )
            }
        )
    }
}

private const val ARG_PRE_SELECTED_ID = "ARG_PRE_SELECTED_IDS"