package com.alexlade.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.alexlade.diaryapp.model.Diary

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    diary: Diary?,
    onBackClicked: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    pagerState: PagerState,
    ) {
    Scaffold(
        topBar = {
            WriteTopBar(
                diary = diary,
                onBackClicked = onBackClicked,
                onDeleteConfirmed = onDeleteConfirmed,
            )
        },
    ) {
        WriteContent(
            pagerState = pagerState,
            title = "",
            onTitleChanged = {},
            description = "",
            onDescriptionChanged = {},
            paddingValues = it
        )
    }
}

