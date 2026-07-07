package dev.ridill.oar.core.ui.components

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.ridill.oar.R
import kotlin.math.hypot

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreateFloatingActionMenu(
    onOptionClick: (CreateOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    BackHandler(expanded) { expanded = false }

    val defaultOption = remember { CreateOption.getDefault() }
    val nonDefaultOptions = remember {
        CreateOption.getNonDefaultOptions()
    }

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            val containerColor = ToggleFloatingActionButtonDefaults.containerColor()
            val containerCornerRadius = ToggleFloatingActionButtonDefaults.containerCornerRadius()
            val containerSize = ToggleFloatingActionButtonDefaults.containerSize()
            val checkedProgress = animateFloatAsState(
                if (expanded) 1f else 0f
            )
            val initialSize = remember(containerSize) { containerSize(0f) }
            Box(Modifier.size(initialSize), contentAlignment = Alignment.Center) {
                val density = LocalDensity.current
                val fabRippleRadius =
                    remember(initialSize) {
                        with(density) {
                            val fabSizeHalf = initialSize.toPx() / 2
                            hypot(fabSizeHalf, fabSizeHalf).toDp()
                        }
                    }
                val shape =
                    remember(density, checkedProgress, containerCornerRadius) {
                        GenericShape { size, _ ->
                            val radius =
                                with(density) { containerCornerRadius(checkedProgress.value).toPx() }
                            addRoundRect(RoundRect(size.toRect(), CornerRadius(radius)))
                        }
                    }
                Box(
                    modifier
                        .graphicsLayer {
                            this.shadowElevation = 3.dp.toPx()
                            this.shape = shape
                            this.clip = true
                        }
                        .drawBehind {
                            val radius =
                                with(density) { containerCornerRadius(checkedProgress.value).toPx() }
                            drawRoundRect(
                                color = containerColor(checkedProgress.value),
                                cornerRadius = CornerRadius(radius),
                            )
                        }
                        .combinedClickable(
                            onClick = {
                                if (expanded) {
                                    expanded = false
                                } else {
                                    onOptionClick(defaultOption)
                                }
                            },
                            onLongClick = { expanded = true },
                            interactionSource = null,
                            indication = ripple(radius = fabRippleRadius)
                        )
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            val sizePx = containerSize(checkedProgress.value).roundToPx()
                            layout(sizePx, sizePx) {
                                placeable.place(
                                    (sizePx - placeable.width) / 2,
                                    (sizePx - placeable.height) / 2,
                                )
                            }
                        }
                ) {
                    val optionIcon = ImageVector.vectorResource(defaultOption.iconRes)
                    val icon by remember {
                        derivedStateOf { if (checkedProgress.value > 0.5f) Icons.Default.Close else optionIcon }
                    }
                    Icon(
                        painter = rememberVectorPainter(icon),
                        contentDescription = stringResource(defaultOption.labelRes),
                        modifier = Modifier
                            .animateIcon({ checkedProgress.value })
                    )
                }
            }
        },
        modifier = modifier,
    ) {
        val closeMenuContentDesc = stringResource(R.string.cd_close_menu)
        nonDefaultOptions.forEachIndexed { index, option ->
            FloatingActionButtonMenuItem(
                modifier =
                    Modifier.semantics {
                        isTraversalGroup = true
                        // Add a custom a11y action to allow closing the menu when focusing
                        // the last menu item, since the close button comes before the first
                        // menu item in the traversal order.
                        if (index == nonDefaultOptions.lastIndex) {
                            customActions = listOf(
                                CustomAccessibilityAction(
                                    label = closeMenuContentDesc,
                                    action = {
                                        expanded = false
                                        true
                                    },
                                )
                            )
                        }
                    },
                onClick = {
                    expanded = false
                    onOptionClick(option)
                },
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(option.iconRes),
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(option.labelRes)) },
            )
        }

    }
}

enum class CreateOption(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
    val default: Boolean,
) {
    CREATE_TRANSACTION(
        labelRes = R.string.new_transaction,
        iconRes = R.drawable.ic_outlined_money_add,
        default = true
    ),
    CREATE_SCHEDULE(
        labelRes = R.string.new_schedule,
        iconRes = R.drawable.ic_outlined_calendar_days,
        default = false
    ),
    CREATE_FOLDER(
        labelRes = R.string.cd_new_folder,
        iconRes = R.drawable.ic_outlined_folder,
        default = false
    );

    companion object {
        fun getDefault(): CreateOption {
            val defaultList = entries.filter { it.default }
            check(defaultList.size == 1) { "There cannot be more than 1 default option" }
            val defaultOption = defaultList.firstOrNull()
            checkNotNull(defaultOption) { "There should be at least 1 default option" }
            return defaultOption
        }

        fun getNonDefaultOptions(): List<CreateOption> = entries
            .filter { !it.default }
    }
}