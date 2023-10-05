package com.alexlade.diaryapp.presentation.screens.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexlade.diaryapp.util.Constants.APP_ID
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {
    val loggedIn = mutableStateOf(false)
    val loadingState = mutableStateOf(false)

    fun setLoadingState(state: Boolean) {
        loadingState.value = state
    }

    fun signInWithMongoAtlas(
        tokenId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        viewModelScope.launch {
            try {
               val result = withContext(Dispatchers.IO) {
                   App.Companion
                       .create(APP_ID)
                       .login(Credentials.jwt(tokenId))
                }.loggedIn
                withContext(Dispatchers.Main) {
                    if (result) {
                        onSuccess()
                        delay(500)
                        loggedIn.value = true
                    } else {
                        onError(Exception("User is not logged in."))
                    }

                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

}