package com.alexlade.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.alexlade.diaryapp.model.Diary

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    diary: Diary?,
    onBackClicked: () -> Unit,
    onDeleteConfirmed: () -> Unit,
) {
    Scaffold(
        topBar = { WriteTopBar(
            diary = diary,
            onBackClicked = onBackClicked,
            onDeleteConfirmed = onDeleteConfirmed,
        ) },
    ) {

    }
}

