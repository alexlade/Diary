package com.alexlade.diaryapp.presentation.screens.auth

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.alexlade.diaryapp.util.Constants.CLIENT_ID
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState
import com.stevdzasan.onetap.OneTapSignInState
import com.stevdzasan.onetap.OneTapSignInWithGoogle
import java.lang.Exception

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AuthenticationScreen(
    loadingState: Boolean,
    oneTapState: OneTapSignInState,
    messageBarState: MessageBarState,
    onClick: () -> Unit,
) {
    ContentWithMessageBar(messageBarState = messageBarState) {
        Scaffold {
            AuthenticationContent(loadingState = loadingState, onClick = onClick)
        }
    }
    OneTapSignInWithGoogle(
        state = oneTapState,
        clientId = CLIENT_ID,
        onTokenIdReceived = { token ->
            Log.d("Auth", token)
            messageBarState.addSuccess("Logged in successfully")
        },
        onDialogDismissed = { msg ->
            Log.d("Auth", msg)
            messageBarState.addError(Exception(msg))
        }
    )
}