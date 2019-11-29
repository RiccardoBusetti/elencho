package com.riccardobusetti.unibztimetable.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.riccardobusetti.unibztimetable.services.UpdateTodayTimetableIntentService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                // TODO: set again the alarm when the phone is booted.
            } else {
                context.startService(Intent(context, UpdateTodayTimetableIntentService::class.java))
            }
        }
    }
}