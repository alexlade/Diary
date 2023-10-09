package com.alexlade.diaryapp.data.repository

import com.alexlade.diaryapp.model.Diary
import com.alexlade.diaryapp.util.RequestState
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>
interface MongoRepository {
    fun configureTheReal()
    fun getAllDiaries(): Flow<Diaries>
    fun getSelectedDairy(diaryId: ObjectId): Flow<RequestState<Diary>>

    suspend fun insertDiary(diary: Diary): RequestState<Diary>
}