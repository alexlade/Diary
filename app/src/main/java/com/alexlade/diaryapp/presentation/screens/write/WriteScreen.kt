package com.alexlade.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.alexlade.diaryapp.model.Diary
import com.alexlade.diaryapp.model.Mood

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    uiState: UiState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onBackClicked: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    pagerState: PagerState,
    moodName: () -> String,
    onSaveClicked: (Diary) -> Unit,
) {
    LaunchedEffect(key1 = uiState.mood, block = {
        pagerState.scrollToPage(Mood.valueOf(uiState.mood.name).ordinal)
    })
    Scaffold(
        topBar = {
            WriteTopBar(
                diary = uiState.diary,
                onBackClicked = onBackClicked,
                onDeleteConfirmed = onDeleteConfirmed,
                moodName = moodName,
            )
        },
    ) {
        WriteContent(
            uiState = uiState,
            pagerState = pagerState,
            title = uiState.title,
            onTitleChanged = onTitleChanged,
            description = uiState.description,
            onDescriptionChanged = onDescriptionChanged,
            paddingValues = it,
            onSaveClicked = onSaveClicked,
        )
    }
}

