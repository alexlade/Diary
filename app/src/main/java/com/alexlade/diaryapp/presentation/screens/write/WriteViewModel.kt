package com.alexlade.diaryapp.presentation.screens.write

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexlade.diaryapp.data.database.entity.ImageToUpload
import com.alexlade.diaryapp.data.database.entity.ImagesToUploadDao
import com.alexlade.diaryapp.data.repository.MongoDB
import com.alexlade.diaryapp.model.Diary
import com.alexlade.diaryapp.model.GalleryImage
import com.alexlade.diaryapp.model.GalleryState
import com.alexlade.diaryapp.model.Mood
import com.alexlade.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.alexlade.diaryapp.model.RequestState
import com.alexlade.diaryapp.util.fetchImagesFromFirebase
import com.alexlade.diaryapp.util.toRealmInstant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val imagesToUploadDao: ImagesToUploadDao
) : ViewModel() {

    var galleryState = GalleryState()
    var uiState by mutableStateOf(UiState())
        private set

    init {
        getDiaryIdArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryIdArgument() {
        uiState = uiState.copy(
            diaryId = savedStateHandle.get<String>(
                key = WRITE_SCREEN_ARGUMENT_KEY
            )
        )
    }

    private fun fetchSelectedDiary() {
        if (uiState.diaryId != null) {
            viewModelScope.launch(Dispatchers.Main) {
                MongoDB.getSelectedDairy(
                    diaryId = ObjectId.Companion.from(uiState.diaryId!!)
                ).catch {
                    emit(RequestState.Error(Exception("Diary is already deleted.")))
                }
                    .collect { state ->
                        if (state is RequestState.Success) {
                            setTitle(state.data.title)
                            setDescription(state.data.description)
                            setMood(Mood.valueOf(state.data.mood))
                            setDiary(diary = state.data)

                            fetchImagesFromFirebase(
                                remoteImagePaths = state.data.images,
                                onImageDownloaded = { downloadedImage ->
                                    galleryState.addImage(
                                        GalleryImage(
                                            image = downloadedImage,
                                            remoteImagePath = extractImagePath(
                                                downloadedImage.toString()
                                            ),
                                        )
                                    )
                                },
                                onImageDownloadedFailed = {

                                },
                                onReadyToDisplay = {

                                },
                            )
                        }
                    }
            }
        }
    }

    private fun extractImagePath(fullImageUrl: String): String {
        val chunks = fullImageUrl.split("%2F")
        val imageName = chunks[2].split("?").first()
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"
    }

    private fun setDiary(diary: Diary) {
        uiState = uiState.copy(diary = diary)
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

    fun setDateTime(zonedDateTime: ZonedDateTime) {
        uiState = zonedDateTime
            .let { uiState.copy(updatedDateTime = it.toInstant().toRealmInstant()) }
    }

    fun upsertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.diaryId == null) {
                insertDiary(diary = diary, onSuccess = onSuccess, onError = onError)
            } else {
                updateDiary(diary = diary, onSuccess = onSuccess, onError = onError)
            }
        }
    }

    private suspend fun insertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        when (val requestState = MongoDB.insertDiary(diary = diary.apply {
            if (uiState.updatedDateTime != null) {
                date = uiState.updatedDateTime!!
            }
        })) {
            is RequestState.Success -> {
                uploadImagesToFirebase()
                withContext(Dispatchers.Main) { onSuccess() }
            }

            is RequestState.Error, RequestState.Idle, RequestState.Loading -> {
                val error = (requestState as? RequestState.Error)?.error?.message
                    ?: "Idle/ Loading Request State"
                onError(error)
            }
        }
    }

    private suspend fun updateDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        when (val requestState = MongoDB.updateDiary(
            diary = diary.apply {
                _id = ObjectId.Companion.from(uiState.diaryId!!)
                date = if (uiState.updatedDateTime != null) {
                    uiState.updatedDateTime!!
                } else {
                    uiState.diary!!.date
                }
            })) {
            is RequestState.Success -> {
                uploadImagesToFirebase()
                deleteImagesFromFirebase()
                withContext(Dispatchers.Main) { onSuccess() }
            }

            is RequestState.Error, RequestState.Idle, RequestState.Loading -> {
                val error = (requestState as? RequestState.Error)?.error?.message
                    ?: "Idle/ Loading Request State"
                onError(error)
            }
        }
    }

    fun deleteDiary(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            if (uiState.diaryId == null) onError("No selected diary")

            when (val requestState =
                MongoDB.deleteDiary(id = ObjectId.Companion.from(uiState.diaryId!!))) {
                is RequestState.Success -> {
                    withContext(Dispatchers.Main) {
                        uiState.diary?.let { deleteImagesFromFirebase(images = it.images) }
                        onSuccess()
                    }
                }

                is RequestState.Error, RequestState.Idle, RequestState.Loading -> {
                    val error = (requestState as? RequestState.Error)?.error?.message
                        ?: "Idle/ Loading Request State"
                    onError(error)
                }
            }
        }
    }

    fun addImage(
        image: Uri,
        imageType: String,
    ) {
        val remoteImagePath = "images/" +
                "${FirebaseAuth.getInstance().currentUser?.uid}/" +
                "${image.lastPathSegment}-${System.currentTimeMillis()}.$imageType"
        galleryState.addImage(
            GalleryImage(
                image = image,
                remoteImagePath = remoteImagePath,
            )
        )
    }

    private fun uploadImagesToFirebase() {
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach { image ->
            val imagePath = storage.child(image.remoteImagePath)
            imagePath.putFile(image.image)
                .addOnProgressListener {
                    it.uploadSessionUri?.let { sessionUri ->
                        viewModelScope.launch(Dispatchers.IO) {
                            imagesToUploadDao.addImageToUpload(
                                ImageToUpload(
                                    remoteImagePath = image.remoteImagePath,
                                    imageUri = image.image.toString(),
                                    sessionUri = sessionUri.toString(),
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun deleteImagesFromFirebase(images: List<String>? = null) {
        val storage = FirebaseStorage.getInstance().reference
        images?.forEach { remotePath ->
            storage.child(remotePath).delete()
        } ?: run {
            galleryState.imagesToDelete.forEach { image ->
                storage.child(image.remoteImagePath).delete()
            }
        }
    }

}

data class UiState(
    val diaryId: String? = null,
    val diary: Diary? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val updatedDateTime: RealmInstant? = null,
)
