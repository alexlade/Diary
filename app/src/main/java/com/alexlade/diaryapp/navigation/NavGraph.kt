package com.alexlade.diaryapp.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alexlade.diaryapp.presentation.components.DisplayAlertDialog
import com.alexlade.diaryapp.presentation.screens.home.HomeScreen
import com.alexlade.diaryapp.presentation.screens.login.LoginScreen
import com.alexlade.diaryapp.presentation.screens.login.LoginViewModel
import com.alexlade.diaryapp.util.Constants.APP_ID
import com.alexlade.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            },
            navigateToWrite = { navHostController.navigate(Screen.Write.route) }
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
                        messageBarState.addSuccess("Successfully Logged In")
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
    navigateToWrite: () -> Unit
) {
    composable(route = Screen.Home.route) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpen by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        HomeScreen(
            drawerState = drawerState,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            onSignOutClicked = { signOutDialogOpen = true },
            navigateToWrite = navigateToWrite,
        )

        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to Sign Out from your Google account?",
            dialogOpened = signOutDialogOpen,
            onCloseDialog = { signOutDialogOpen = false },
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        navigateToLogin()
                    }
                    val user = App.create(APP_ID).currentUser
                    user?.logOut()
                }
            }
        )
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
