package com.alexlade.diaryapp.presentation.screens.write

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexlade.diaryapp.data.repository.MongoDB
import com.alexlade.diaryapp.model.Diary
import com.alexlade.diaryapp.model.Mood
import com.alexlade.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.alexlade.diaryapp.util.RequestState
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WriteViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    init {
        getDiaryIdArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryIdArgument() {
        uiState = uiState.copy(
            diaryId = savedStateHandle.get<String>(
                key = WRITE_SCREEN_ARGUMENT_KEY
            )
        )
    }

    private fun fetchSelectedDiary() {
        if (uiState.diaryId != null) {
            viewModelScope.launch(Dispatchers.Main) {
                MongoDB.getSelectedDairy(
                    diaryId = ObjectId.Companion.from(uiState.diaryId!!)
                ).collect { state ->
                    if (state is RequestState.Success) {
                        setTitle(state.data.title)
                        setDescription(state.data.description)
                        setMood(Mood.valueOf(state.data.mood))
                        setDiary(diary = state.data)
                    }
                }
            }
        }
    }

    private fun setDiary(diary: Diary) {
        uiState = uiState.copy(diary = diary)
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

    fun writeDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
       viewModelScope.launch(Dispatchers.IO) {
           when (val requestState = MongoDB.insertDiary(diary = diary)) {
               is RequestState.Success -> { withContext(Dispatchers.IO) { onSuccess() } }
               is RequestState.Error, RequestState.Idle, RequestState.Loading -> {
                   val error = (requestState as? RequestState.Error)?.error?.message
                       ?: "Idle/ Loading Request State"
                   onError(error)
               }
           }
       }
    }

}

data class UiState(
    val diaryId: String? = null,
    val diary: Diary? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
)
