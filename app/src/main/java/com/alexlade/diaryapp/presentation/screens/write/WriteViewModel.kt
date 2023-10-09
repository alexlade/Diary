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
                val diary = MongoDB.getSelectedDairy(
                    diaryId = ObjectId.Companion.from(uiState.diaryId!!)
                )
                if (diary is RequestState.Success) {
                    setTitle(diary.data.title)
                    setDescription(diary.data.description)
                    setMood(Mood.valueOf(diary.data.mood))
                    setDiary(diary = diary.data)
                }
            }
        }
    }

    fun setDiary(diary: Diary) {
        uiState = uiState.copy(diary = diary)
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

}

data class UiState(
    val diaryId: String? = null,
    val diary: Diary? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
)
