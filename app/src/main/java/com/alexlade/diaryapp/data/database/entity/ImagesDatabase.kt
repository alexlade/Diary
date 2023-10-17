package com.alexlade.diaryapp.data.database.entity

import android.media.Image
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ImageToUpload::class],
    version = 1,
    exportSchema = false,
)
abstract class ImagesDatabase : RoomDatabase() {
    abstract fun imagesToUploadDao(): ImageToUploadDao
}