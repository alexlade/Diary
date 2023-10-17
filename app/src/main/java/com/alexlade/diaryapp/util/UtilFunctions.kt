package com.alexlade.diaryapp.util

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.alexlade.diaryapp.data.database.entity.ImageToUpload
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import io.realm.kotlin.types.RealmInstant
import java.time.Instant

fun RealmInstant.toInstance(): Instant {
    val sec: Long = this.epochSeconds
    val nano: Int = this.nanosecondsOfSecond
    return if (sec >= 0 ) {
        Instant.ofEpochSecond(sec, nano.toLong())
    } else {
        Instant.ofEpochSecond(sec - 1, 1_000_000 + nano.toLong())
    }
}

fun Instant.toRealmInstant(): RealmInstant {
    val sec: Long = this.epochSecond
    val nano: Int = this.nano
    return if (sec >= 0) {
        RealmInstant.from(sec, nano)
    } else {
        RealmInstant.from(sec+1, -1_000_000+nano)
    }
}

fun fetchImagesFromFirebase(
    remoteImagePaths: List<String>,
    onImageDownloaded: (Uri) -> Unit,
    onImageDownloadedFailed: (Exception) -> Unit = { },
    onReadyToDisplay: () -> Unit = { },
) {
    remoteImagePaths.forEachIndexed { index, image ->
        if (image.trim().isEmpty()) return@forEachIndexed

        FirebaseStorage.getInstance().reference.child(image.trim()).downloadUrl
            .addOnSuccessListener {
                onImageDownloaded(it)
                if (image.lastIndexOf(remoteImagePaths.last()) == index) {
                    onReadyToDisplay()
                }
                Log.d("DownloadUrl", "$it")
            }
            .addOnFailureListener { onImageDownloadedFailed(it) }
    }
}

fun retryUploadingImageToFirebase(
    imageToUpload: ImageToUpload,
    onSuccess: () -> Unit,
) {
    val storage = FirebaseStorage.getInstance().reference
    storage.child(imageToUpload.remoteImagePath).putFile(
        imageToUpload.imageUri.toUri(),
        storageMetadata {  },
        imageToUpload.sessionUri.toUri(),
    ).addOnSuccessListener { onSuccess() }
}