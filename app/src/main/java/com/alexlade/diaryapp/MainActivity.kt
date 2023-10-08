package com.alexlade.diaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.alexlade.diaryapp.navigation.Screen
import com.alexlade.diaryapp.navigation.SetupNavGraph
import com.alexlade.diaryapp.ui.theme.DiaryAppTheme
import com.alexlade.diaryapp.util.Constants.APP_ID
import io.realm.kotlin.mongodb.App

class MainActivity : ComponentActivity() {

    var keepSplashOpened = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DiaryAppTheme {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navHostController = navController,
                    onDataLoaded = {
                        keepSplashOpened = false
                    }
                )
            }
        }
    }

    private fun getStartDestination(): String {
        val user = App.Companion.create(APP_ID).currentUser
        return if (user?.loggedIn == true) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }
    }

}