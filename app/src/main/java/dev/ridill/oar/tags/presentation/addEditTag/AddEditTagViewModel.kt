package dev.ridill.oar.tags.presentation.addEditTag

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.oar.R
import dev.ridill.oar.core.domain.util.EventBus
import dev.ridill.oar.core.domain.util.capitalizeFirstChar
import dev.ridill.oar.core.domain.util.textAsFlow
import dev.ridill.oar.core.ui.navigation.AddEditTagSheetRoute
import dev.ridill.oar.core.ui.navigation.INVALID_ID_LONG
import dev.ridill.oar.core.ui.util.UiText
import dev.ridill.oar.tags.domain.model.Tag
import dev.ridill.oar.tags.domain.repository.TagsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AddEditTagViewModel.Factory::class)
class AddEditTagViewModel @AssistedInject constructor(
    @Assisted val route: AddEditTagSheetRoute,
    private val savedStateHandle: SavedStateHandle,
    private val repo: TagsRepository,
    private val eventBus: EventBus<AddEditTagEvent>
) : ViewModel(), AddEditTagActions {

    @AssistedFactory
    interface Factory {
        fun create(route: AddEditTagSheetRoute): AddEditTagViewModel
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading get() = _isLoading.asStateFlow()

    val tagInput = savedStateHandle.getStateFlow<Tag>(TAG_INPUT, Tag.NEW)
    val nameInputState = savedStateHandle.saveable(
        key = "NAME_INPUT_STATE",
        saver = TextFieldState.Saver,
        init = { TextFieldState() }
    )
    val tagInputError = savedStateHandle.getStateFlow<UiText?>(NEW_TAG_ERROR, null)

    val showTagDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_TAG_CONFIRMATION, false)

    val events = eventBus.eventFlow

    init {
        onInit()
        collectNameState()
    }

    private fun onInit() = viewModelScope.launch {
        val tag = repo.getTagById(route.tagId) ?: Tag.NEW
        savedStateHandle[TAG_INPUT] = tag
        if (route.tagId == INVALID_ID_LONG) {
            nameInputState.setTextAndPlaceCursorAtEnd(route.prefilledName.capitalizeFirstChar())
        } else {
            nameInputState.setTextAndPlaceCursorAtEnd(tag.name)
        }
    }

    private fun collectNameState() {
        nameInputState.textAsFlow()
            .onEach {
                savedStateHandle[NEW_TAG_ERROR] = null
            }.launchIn(viewModelScope)
    }

    override fun onColorSelect(color: Color) {
        savedStateHandle[TAG_INPUT] = tagInput.value.copy(colorCode = color.toArgb())
    }

    override fun onExclusionChange(excluded: Boolean) {
        savedStateHandle[TAG_INPUT] = tagInput.value.copy(excluded = excluded)
    }

    override fun onConfirm() {
        viewModelScope.launch {
            val tagInput = tagInput.value
            val name = nameInputState.text.toString().trim()
            if (name.isEmpty()) {
                savedStateHandle[NEW_TAG_ERROR] = UiText.StringResource(
                    R.string.error_invalid_tag_name,
                    isErrorText = true
                )
                return@launch
            }
            _isLoading.update { true }
            val colorCode = tagInput.colorCode
            val savedId = repo.saveTag(
                name = name,
                colorCode = colorCode,
                id = tagInput.id,
                timestamp = tagInput.createdTimestamp,
                excluded = tagInput.excluded
            )
            _isLoading.update { false }
            eventBus.send(AddEditTagEvent.TagSaved(savedId))
        }
    }

    override fun onDeleteClick() {
        savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = true
    }

    override fun onDeleteTagDismiss() {
        savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = false
    }

    override fun onDeleteTagConfirm() {
        viewModelScope.launch {
            repo.deleteTagById(route.tagId)
            savedStateHandle[SHOW_DELETE_TAG_CONFIRMATION] = false
            eventBus.send(AddEditTagEvent.TagDeleted)
        }
    }

    sealed interface AddEditTagEvent {
        data class TagSaved(val tagId: Long) : AddEditTagEvent
        data object TagDeleted : AddEditTagEvent
    }
}

private const val TAG_INPUT = "TAG_INPUT"
private const val NEW_TAG_ERROR = "NEW_TAG_ERROR"
private const val SHOW_DELETE_TAG_CONFIRMATION = "SHOW_DELETE_TAG_CONFIRMATION"
