package com.alexlade.diaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.alexlade.diaryapp.data.database.entity.ImageToDelete
import com.alexlade.diaryapp.data.database.entity.ImageToDeleteDao
import com.alexlade.diaryapp.data.database.entity.ImageToUpload
import com.alexlade.diaryapp.data.database.entity.ImagesToUploadDao
import com.alexlade.diaryapp.navigation.Screen
import com.alexlade.diaryapp.navigation.SetupNavGraph
import com.alexlade.diaryapp.ui.theme.DiaryAppTheme
import com.alexlade.diaryapp.util.Constants.APP_ID
import com.alexlade.diaryapp.util.retryDeletingImageFromFirebase
import com.alexlade.diaryapp.util.retryUploadingImageToFirebase
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imagesToUploadDao: ImagesToUploadDao

    @Inject
    lateinit var imageToDeleteDao: ImageToDeleteDao

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
        cleanupCheck(lifecycleScope, imagesToUploadDao, imageToDeleteDao)
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
        imagesToUploadDao: ImagesToUploadDao,
        imageToDeleteDao: ImageToDeleteDao,
    ) {
        scope.launch(Dispatchers.IO) {
            val result = imagesToUploadDao.getAllImages()
            result.forEach { imageToUpload: ImageToUpload ->
                retryUploadingImageToFirebase(
                    imageToUpload = imageToUpload,
                    onSuccess = {
                        scope.launch(Dispatchers.IO) {
                            imagesToUploadDao.cleanupImage(imageToUpload.id)
                        }
                    }
                )
            }
            val result2 = imageToDeleteDao.getAllImages()
            result2.forEach { imageToDelete ->
                retryDeletingImageFromFirebase(
                    imageToDelete = imageToDelete,
                    onSuccess = {
                        scope.launch(Dispatchers.IO) {
                            Dispatchers.IO
                            imageToDeleteDao.cleanupImage(imageToDelete.id)
                        }
                    }
                )
            }
        }
    }

}