package com.alexlade.diaryapp.presentation.screens.login

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.alexlade.diaryapp.util.Constants.CLIENT_ID
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState
import com.stevdzasan.onetap.OneTapSignInState
import com.stevdzasan.onetap.OneTapSignInWithGoogle
import kotlin.Exception

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(
    loggedIn: Boolean,
    loadingState: Boolean,
    oneTapState: OneTapSignInState,
    messageBarState: MessageBarState,
    onSuccessfulFirebaseLogin: (String) -> Unit,
    onFailureFirebaseLogin: (Exception) -> Unit,
    onDialogDismiss: (String) -> Unit,
    onClick: () -> Unit,
    navigateToHome: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        content = {
            ContentWithMessageBar(messageBarState = messageBarState) {
                LoginContent(loadingState = loadingState, onClick = onClick)
            }
        }
    )

    OneTapSignInWithGoogle(
        state = oneTapState,
        clientId = CLIENT_ID,
        onTokenIdReceived = { token ->
            val credentials = GoogleAuthProvider.getCredential(token, null)
            FirebaseAuth
                .getInstance()
                .signInWithCredential(credentials)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccessfulFirebaseLogin(token)
                    } else {
                        task.exception?.let { onFailureFirebaseLogin(it) }
                    }
                }
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