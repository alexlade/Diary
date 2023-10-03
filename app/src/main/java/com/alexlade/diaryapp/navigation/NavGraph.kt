package com.alexlade.diaryapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alexlade.diaryapp.presentation.screens.auth.LoginScreen
import com.alexlade.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState


@Composable
fun SetupNavGraph(startDestination: String, navHostController: NavHostController) {
    NavHost(navController = navHostController, startDestination = startDestination) {
        loginRoute()
        homeRoute()
        writeRoute()
    }
}

fun NavGraphBuilder.loginRoute() {
    composable(route = Screen.Login.route) {
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()
        LoginScreen(
            loadingState = oneTapState.opened,
            oneTapState = oneTapState,
            messageBarState = messageBarState,
            onClick = { oneTapState.open() },
        )
    }
}

fun NavGraphBuilder.homeRoute() {
    composable(route = Screen.Home.route) {

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
