package com.alexlade.diaryapp.data.repository

import com.alexlade.diaryapp.model.Diary
import com.alexlade.diaryapp.model.RequestState
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZonedDateTime

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>
interface MongoRepository {
    fun configureTheReal()
    fun getAllDiaries(): Flow<Diaries>
    fun getFilteredDairies(zonedDateTime: ZonedDateTime): Flow<Diaries>
    fun getSelectedDairy(diaryId: ObjectId): Flow<RequestState<Diary>>

    suspend fun insertDiary(diary: Diary): RequestState<Diary>
    suspend fun updateDiary(diary: Diary): RequestState<Diary>

    suspend fun deleteDiary(id: ObjectId): RequestState<Diary>
    suspend fun deleteAllDiaries(): RequestState<Boolean>
}