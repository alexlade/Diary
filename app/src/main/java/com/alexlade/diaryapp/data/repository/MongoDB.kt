package com.alexlade.diaryapp.data.repository

import com.alexlade.diaryapp.model.Diary
import com.alexlade.diaryapp.util.Constants.APP_ID
import com.alexlade.diaryapp.model.RequestState
import com.alexlade.diaryapp.util.toInstance
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId

object MongoDB : MongoRepository {

    private val app = App.Companion.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureTheReal()
    }

    override fun configureTheReal() {
        if (user != null) {
            val config = SyncConfiguration.Builder(user, setOf(Diary::class))
                .initialSubscriptions { sub ->
                    add(
                        query = sub.query<Diary>(query = "ownerId == $0", user.identity),
                        name = "User's Diaries"
                    )
                }
                .log(LogLevel.ALL)
                .build()

            realm = Realm.open(config)
        }
    }

    override fun getAllDiaries(): Flow<Diaries> {
        return if (user == null) {
            flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        } else {
            try {
                realm.query<Diary>(query = "ownerId == $0", user.identity)
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map { result ->
                        RequestState.Success(
                            data = result.list.groupBy {
                                it.date.toInstance()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }
            } catch (e: Exception) {
                flow { emit(RequestState.Error(e)) }
            }
        }
    }

    override fun getSelectedDairy(diaryId: ObjectId): Flow<RequestState<Diary>> {
        if (user == null) flow { emit(RequestState.Error(UserNotAuthenticatedException())) }
        return flow {
            emit(
                try {
                    RequestState.Success(
                        data = realm.query<Diary>(query = "_id == $0", diaryId)
                        .find()
                        .first()
                    )
                } catch (e: Exception) { RequestState.Error(e) }
            )
        }
    }

override suspend fun insertDiary(diary: Diary): RequestState<Diary> {
    return if (user != null) {
        try {
            realm.write {
                val addedDiary = copyToRealm(diary.apply { ownerId = user.identity })
                RequestState.Success(data = addedDiary)
            }
        } catch (e: Exception) {
            RequestState.Error(e)
        }
    } else {
        RequestState.Error(UserNotAuthenticatedException())
    }
}

override suspend fun updateDiary(diary: Diary): RequestState<Diary> {
    return if (user != null) {
        realm.write {
            query<Diary>("_id == $0", diary._id)
                .first()
                .find()
                ?.let {
                    it.title = diary.title
                    it.description = diary.description
                    it.mood = diary.mood
                    it.images = diary.images
                    it.date = diary.date
                    RequestState.Success(data = it)
                } ?: RequestState.Error(Exception("Queried diary does not exist"))
        }
    } else {
        RequestState.Error(UserNotAuthenticatedException())
    }
}

    override suspend fun deleteDiary(id: ObjectId): RequestState<Diary> {
        return user?.let { user ->
            realm.write {
                try {
                    query<Diary>("_id == $0 AND ownerId = $1", id, user.identity)
                        .first()
                        .find()
                        ?.let { diary ->
                            delete(diary)
                            RequestState.Success(data = diary)
                        } ?: RequestState.Error(Exception("No diary found"))
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } ?: RequestState.Error(UserNotAuthenticatedException())
    }
}


private class UserNotAuthenticatedException : Exception("User is not Logged in.")