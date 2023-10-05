package com.alexlade.diaryapp.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alexlade.diaryapp.presentation.screens.login.LoginScreen
import com.alexlade.diaryapp.presentation.screens.login.LoginViewModel
import com.alexlade.diaryapp.util.Constants.APP_ID
import com.alexlade.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception


@Composable
fun SetupNavGraph(startDestination: String, navHostController: NavHostController) {
    NavHost(navController = navHostController, startDestination = startDestination) {
        loginRoute(
            navigateToHome = {
                navHostController.popBackStack()
                navHostController.navigate(Screen.Home.route)
            }
        )
        homeRoute(
            navigateToLogin = {
                navHostController.popBackStack()
                navHostController.navigate(Screen.Login.route)
            }
        )
        writeRoute()
    }
}

fun NavGraphBuilder.loginRoute(
    navigateToHome: () -> Unit,
) {
    composable(route = Screen.Login.route) {
        val viewModel: LoginViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val loggedIn by viewModel.loggedIn
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LoginScreen(
            loggedIn = loggedIn,
            loadingState = loadingState,
            oneTapState = oneTapState,
            messageBarState = messageBarState,
            onClick = {
                oneTapState.open()
                viewModel.setLoadingState(true)
            },
            onTokenIdReceived = { tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        if (it) {
                            messageBarState.addSuccess("Successfully Logged In")
                        }
                        viewModel.setLoadingState(false)
                    },
                    onError = {
                        messageBarState.addError(it)
                        viewModel.setLoadingState(false)
                    }
                )

            },
            onDialogDismiss = { msg ->
                messageBarState.addError(Exception(msg))
            },
            navigateToHome = navigateToHome
        )
    }
}

fun NavGraphBuilder.homeRoute(
    navigateToLogin: () -> Unit,
) {
    composable(route = Screen.Home.route) {
        val scope = rememberCoroutineScope()
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        App.Companion.create(APP_ID).currentUser?.logOut()
                    }
                    navigateToLogin()
                }
            ) {
                Text("Logout")
            }
        }
    }
}

fun NavGraphBuilder.writeRoute() {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {

    }
}
