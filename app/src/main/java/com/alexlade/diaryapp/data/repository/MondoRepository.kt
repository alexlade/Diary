package com.alexlade.diaryapp.data.repository

import com.alexlade.diaryapp.model.Diary
import com.alexlade.diaryapp.util.RequestState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>
interface MongoRepository {
    fun configureTheReal()
    fun getAllDiaries(): Flow<Diaries>
}