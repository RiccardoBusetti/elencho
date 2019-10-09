package com.riccardobusetti.unibztimetable.ui.today

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.riccardobusetti.unibztimetable.R
import com.riccardobusetti.unibztimetable.domain.entities.Day
import com.riccardobusetti.unibztimetable.domain.usecases.GetTodayTimetableUseCase
import com.riccardobusetti.unibztimetable.utils.custom.TimetableViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class TodayViewModel(
    private val context: Context,
    private val todayUseCase: GetTodayTimetableUseCase
) : TimetableViewModel<List<Day>>() {

    companion object {
        private const val TAG = "TodayViewModel"
    }

    fun loadTodayTimetable(department: String, degree: String, academicYear: String, page: String) {
        viewModelScope.launchWithSupervisor {
            loadingState.value = true

            val work = async(Dispatchers.IO) {
                todayUseCase.getTodayTimetable(
                    department,
                    degree,
                    academicYear,
                    page
                )
            }

            val newTimetable = try {
                work.await()
            } catch (e: Exception) {
                Log.d(TAG, "This error occurred while parsing the timetable -> $e")
                error.value = context.getString(R.string.error_fetching)
                null
            }

            loadingState.value = false
            newTimetable?.let {
                if (newTimetable.isEmpty())
                    error.value = context.getString(R.string.error_no_courses)
                else
                    timetable.value = newTimetable
            }
        }
    }
}