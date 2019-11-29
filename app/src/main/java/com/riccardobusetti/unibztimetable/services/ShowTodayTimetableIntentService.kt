package com.riccardobusetti.unibztimetable.services

import android.app.IntentService
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.riccardobusetti.unibztimetable.R
import com.riccardobusetti.unibztimetable.domain.entities.Course
import com.riccardobusetti.unibztimetable.domain.repositories.TimetableRepository
import com.riccardobusetti.unibztimetable.domain.strategies.LocalTimetableStrategy
import com.riccardobusetti.unibztimetable.domain.strategies.RemoteTimetableStrategy
import com.riccardobusetti.unibztimetable.domain.usecases.GetTodayTimetableUseCase
import com.riccardobusetti.unibztimetable.utils.NotificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ShowTodayTimetableIntentService : IntentService(ShowTodayTimetableIntentService::class.java.simpleName) {

    private val getTodayTimetableUseCase = GetTodayTimetableUseCase(
        TimetableRepository(
            LocalTimetableStrategy(this),
            RemoteTimetableStrategy()
        )
    )

    override fun onHandleIntent(p0: Intent?) {
        runBlocking {
            val localTimetable = withContext(Dispatchers.IO) {
                getTodayTimetableUseCase.getLocalTodayTimetable()
            }

            showNotification(localTimetable)
        }
    }

    private fun showNotification(courses: List<Course>) {
        val builder = NotificationCompat.Builder(
            this@ShowTodayTimetableIntentService,
            NotificationUtils.DAILY_UPDATES_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_today)
            .setContentTitle("You have ${courses.size} courses today.")
            .setContentText("The first course will be ${courses.first().description}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this@ShowTodayTimetableIntentService)) {
            notify(1, builder.build())
        }
    }
}