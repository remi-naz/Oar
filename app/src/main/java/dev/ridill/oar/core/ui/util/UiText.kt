package dev.ridill.oar.core.ui.util

import android.content.Context
import android.os.Parcelable
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class UiText(
    open val isErrorText: Boolean
) : Parcelable {
    data class StringResource(
        @StringRes val resId: Int,
        override val isErrorText: Boolean = false,
        val args: List<String> = emptyList()
    ) : UiText(isErrorText = isErrorText)

    data class DynamicString(
        val message: String,
        override val isErrorText: Boolean = false
    ) : UiText(isErrorText = isErrorText)

    class PluralResource(
        @PluralsRes val resId: Int,
        val count: Int,
        vararg val args: String
    ) : UiText(isErrorText = false)

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> message
        is StringResource -> stringResource(resId, *(args.toTypedArray()))
        is PluralResource -> pluralStringResource(id = resId, count = count, formatArgs = args)
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> message
        is StringResource -> context.getString(resId, *(args.toTypedArray()))
        is PluralResource -> context.resources.getQuantityString(resId, count, args)
    }
}