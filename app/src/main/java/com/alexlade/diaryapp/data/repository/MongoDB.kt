package com.alexlade.diaryapp.data.repository

import com.alexlade.diaryapp.model.Diary
import com.alexlade.diaryapp.util.Constants.APP_ID
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration

object MongoDB : MongoRepository {

    private val app = App.Companion.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm
    override fun configureTheReal() {
        if (user != null) {
            val config = SyncConfiguration.Builder(user, setOf(Diary::class))
                .initialSubscriptions { sub ->
                    add(
                        query = sub.query("ownerId == $0", user.identity),
                        name = "User's Diaries"
                    )
                }
                .log(LogLevel.ALL)
                .build()

            realm = Realm.open(config)
        }
    }
}