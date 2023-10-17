package com.alexlade.diaryapp.presentation.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexlade.diaryapp.data.repository.Diaries
import com.alexlade.diaryapp.data.repository.MongoDB
import com.alexlade.diaryapp.model.RequestState
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    val diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)

    init {
        observeAllDiaries()
    }

    private fun observeAllDiaries() {
        viewModelScope.launch {
            MongoDB.getAllDiaries().collect { result ->
                diaries.value = result
            }
        }
    }

}