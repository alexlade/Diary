package com.alexlade.diaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.alexlade.diaryapp.data.database.entity.ImageToUpload
import com.alexlade.diaryapp.data.database.entity.ImageToUploadDao
import com.alexlade.diaryapp.navigation.Screen
import com.alexlade.diaryapp.navigation.SetupNavGraph
import com.alexlade.diaryapp.ui.theme.DiaryAppTheme
import com.alexlade.diaryapp.util.Constants.APP_ID
import com.alexlade.diaryapp.util.retryUploadingImageToFirebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageToUploadDao: ImageToUploadDao

    private var keepSplashOpened = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FirebaseApp.initializeApp(this)
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
        cleanupCheck(lifecycleScope, imageToUploadDao)
    }

    private fun getStartDestination(): String {
        val user = App.Companion.create(APP_ID).currentUser
        return if (user?.loggedIn == true) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }
    }

    private fun cleanupCheck(
        scope: CoroutineScope,
        imageToUploadDao: ImageToUploadDao,
    ) {
        scope.launch(Dispatchers.IO) {
            val result = imageToUploadDao.getAllImages()
            result.forEach { imageToUpload: ImageToUpload ->
                retryUploadingImageToFirebase(
                    imageToUpload = imageToUpload,
                    onSuccess = {
                        scope.launch(Dispatchers.IO) {
                            imageToUploadDao.cleanupImage(imageToUpload.id)
                        }
                    }
                )

            }
        }
    }

}