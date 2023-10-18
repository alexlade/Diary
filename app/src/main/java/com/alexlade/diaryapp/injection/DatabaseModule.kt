package com.alexlade.diaryapp.injection

import android.content.Context
import androidx.room.Room
import com.alexlade.diaryapp.data.database.entity.ImagesDatabase
import com.alexlade.diaryapp.util.Constants.IMAGES_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    @Singleton
    fun providesDatabase(
        @ApplicationContext context: Context,
    ): ImagesDatabase {
        return Room
            .databaseBuilder(
                context,
                ImagesDatabase::class.java,
                IMAGES_DATABASE
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providesFistDao(
        database: ImagesDatabase,
    ) = database.imagesToUploadDao()

    @Provides
    @Singleton
    fun providesSecondDao(
        database: ImagesDatabase,
    ) = database.imagesToDeleteDao()

}