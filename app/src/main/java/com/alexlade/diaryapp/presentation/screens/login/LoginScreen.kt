package com.alexlade.diaryapp.presentation.screens.login

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.alexlade.diaryapp.util.Constants.CLIENT_ID
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState
import com.stevdzasan.onetap.OneTapSignInState
import com.stevdzasan.onetap.OneTapSignInWithGoogle
import java.lang.Exception

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(
    loggedIn: Boolean,
    loadingState: Boolean,
    oneTapState: OneTapSignInState,
    messageBarState: MessageBarState,
    onTokenIdReceived: (String) -> Unit,
    onDialogDismiss: (String) -> Unit,
    onClick: () -> Unit,
    navigateToHome: () -> Unit,
) {
    ContentWithMessageBar(messageBarState = messageBarState) {
        Scaffold {
            LoginContent(loadingState = loadingState, onClick = onClick)
        }
    }
    OneTapSignInWithGoogle(
        state = oneTapState,
        clientId = CLIENT_ID,
        onTokenIdReceived = { token ->
            onTokenIdReceived(token)
        },
        onDialogDismissed = { msg ->
            onDialogDismiss(msg)
            messageBarState.addError(Exception(msg))
        }
    )

    LaunchedEffect(key1 = loggedIn) {
        if (loggedIn) navigateToHome()
    }
}