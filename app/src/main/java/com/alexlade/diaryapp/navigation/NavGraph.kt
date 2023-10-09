package com.alexlade.diaryapp.navigation

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.alexlade.diaryapp.model.Mood
import com.alexlade.diaryapp.presentation.components.DisplayAlertDialog
import com.alexlade.diaryapp.presentation.screens.home.HomeScreen
import com.alexlade.diaryapp.presentation.screens.home.HomeViewModel
import com.alexlade.diaryapp.presentation.screens.login.LoginScreen
import com.alexlade.diaryapp.presentation.screens.login.LoginViewModel
import com.alexlade.diaryapp.presentation.screens.write.WriteScreen
import com.alexlade.diaryapp.presentation.screens.write.WriteViewModel
import com.alexlade.diaryapp.util.Constants.APP_ID
import com.alexlade.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.alexlade.diaryapp.util.RequestState
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception


@Composable
fun SetupNavGraph(
    onDataLoaded: () -> Unit,
    startDestination: String,
    navHostController: NavHostController,
) {
    NavHost(navController = navHostController, startDestination = startDestination) {
        loginRoute(
            navigateToHome = {
                navHostController.popBackStack()
                navHostController.navigate(Screen.Home.route)
            },
            onDataLoaded = onDataLoaded,
        )
        homeRoute(
            navigateToLogin = {
                navHostController.popBackStack()
                navHostController.navigate(Screen.Login.route)
            },
            navigateToWrite = { navHostController.navigate(Screen.Write.route) },
            navigateToWriteArgs = {
                navHostController.navigate(Screen.Write.passDiaryId(diaryId = it))
            },
            onDataLoaded = onDataLoaded,
        )
        writeRoute(
            onBackClicked = {
                navHostController.popBackStack()
            })
    }
}

fun NavGraphBuilder.loginRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit,
) {
    composable(route = Screen.Login.route) {
        val viewModel: LoginViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val loggedIn by viewModel.loggedIn
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(key1 = Unit, block = { onDataLoaded() })

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
    navigateToWrite: () -> Unit,
    navigateToWriteArgs: (String) -> Unit,
    onDataLoaded: () -> Unit,

    ) {
    composable(route = Screen.Home.route) {
        val viewModel: HomeViewModel = viewModel()
        val diaries by viewModel.diaries
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpen by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }

        HomeScreen(
            diaries = diaries,
            drawerState = drawerState,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            onSignOutClicked = { signOutDialogOpen = true },
            navigateToWrite = navigateToWrite,
            navigateToWriteArgs = navigateToWriteArgs,
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

@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(
    onBackClicked: () -> Unit,
) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val viewModel: WriteViewModel = viewModel()
        val uiState = viewModel.uiState
        val pagerState = rememberPagerState()
        val pageNumber by remember {
            derivedStateOf {
                pagerState.currentPage
            }
        }

        LaunchedEffect(key1 = uiState, block = {
            Log.d("Diary", "${uiState.diaryId}")
        })

        WriteScreen(
            uiState = uiState,
            pagerState = pagerState,
            onTitleChanged = { viewModel.setTitle(title = it) },
            onDescriptionChanged = { viewModel.setDescription(description = it) },
            onBackClicked = onBackClicked,
            onDeleteConfirmed = { },
            moodName = { Mood.values()[pageNumber].name },
            onSaveClicked = {
                viewModel.upsertDiary(
                    diary = it.apply { mood = Mood.values()[pageNumber].name },
                    onSuccess = { onBackClicked() },
                    onError = {

                    }
                )
            }
        )
    }
}
