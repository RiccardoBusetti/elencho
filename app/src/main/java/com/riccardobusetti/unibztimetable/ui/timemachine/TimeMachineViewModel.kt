package com.riccardobusetti.unibztimetable.ui.timemachine

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.riccardobusetti.unibztimetable.domain.entities.Course
import com.riccardobusetti.unibztimetable.domain.entities.DisplayableCourseGroup
import com.riccardobusetti.unibztimetable.domain.entities.UserPrefs
import com.riccardobusetti.unibztimetable.domain.entities.app.AppSection
import com.riccardobusetti.unibztimetable.domain.entities.params.TimetableParams
import com.riccardobusetti.unibztimetable.domain.entities.safeGet
import com.riccardobusetti.unibztimetable.domain.usecases.GetIntervalDateTimetableUseCase
import com.riccardobusetti.unibztimetable.domain.usecases.GetUserPrefsUseCase
import com.riccardobusetti.unibztimetable.ui.custom.TimetableViewModel
import com.riccardobusetti.unibztimetable.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.util.*

class TimeMachineViewModel(
    private val context: Context,
    private val getIntervalDateTimetableUseCase: GetIntervalDateTimetableUseCase,
    private val getUserPrefsUseCase: GetUserPrefsUseCase
) : TimetableViewModel() {

    /**
     * Enum representing the state of the bottomsheet which pops up when the user
     * needs to check for an interval of dates in order to perform a time travel.
     */
    enum class BottomSheetState {
        OPENED,
        CLOSED
    }

    companion object {
        private const val TAG = "TimeMachineViewModel"
    }

    /**
     * Live data object that contains a map with the date interval that has been
     * currently selected.
     */
    private val _selectedDateInterval = MutableLiveData<Pair<String, String>>().apply {
        this.value =
            DateUtils.getCurrentDateFormatted() to DateUtils.getCurrentDatePlusDaysFormatted(7)
    }
    val selectedDateInterval: LiveData<Pair<String, String>>
        get() = _selectedDateInterval

    /**
     * Live data object that contains the state of the bottomsheet which appears when
     * choosing the date interval.
     */
    private val _bottomSheetState =
        MutableLiveData<BottomSheetState>().apply { this.value = BottomSheetState.CLOSED }
    val bottomSheetState: LiveData<BottomSheetState>
        get() = _bottomSheetState

    override fun start() {
        viewModelScope.safeLaunch(TAG) {
            for (request in timetableRequests) {
                hideError()
                showLoading(request.isReset)

                loadTimetable(getUserPrefs(), request.fromDate!!, request.toDate!!, request.page)
                    .flowOn(Dispatchers.IO)
                    .onEach {
                        showTimetable(it, request.isReset)
                        hideLoading()
                    }
                    .handleErrors(TAG)
                    .collect()
            }
        }
    }

    private suspend fun getUserPrefs() = withContext(Dispatchers.IO) {
        getUserPrefsUseCase.execute(null)
    }

    private fun loadTimetable(
        userPrefs: UserPrefs, fromDate: String,
        toDate: String,
        page: String
    ) = getIntervalDateTimetableUseCase.execute(
        TimetableParams(
            department = userPrefs.prefs.safeGet(UserPrefs.Pref.DEPARTMENT_KEY),
            degree = userPrefs.prefs.safeGet(UserPrefs.Pref.DEGREE_KEY),
            studyPlan = userPrefs.prefs.safeGet(UserPrefs.Pref.STUDY_PLAN_KEY),
            fromDate = fromDate,
            toDate = toDate,
            page = page
        )
    )

    override fun coursesToCourseGroups(courses: List<Course>): List<DisplayableCourseGroup> {
        return DisplayableCourseGroup.build(courses, AppSection.TIME_MACHINE)
    }

    fun updateBottomSheetState(newBottomSheetState: BottomSheetState) {
        _bottomSheetState.value = newBottomSheetState
    }

    fun getCurrentFromDate(): Calendar? {
        val fromDate = DateUtils.formatStringToDate(_selectedDateInterval.value!!.first)

        return if (fromDate != null) {
            DateUtils.getCalendarFromDate(fromDate)
        } else {
            null
        }
    }

    fun updateFromDate(newDate: String) {
        _selectedDateInterval.value = newDate to _selectedDateInterval.value!!.second
    }

    fun getCurrentToDate(): Calendar? {
        val fromDate = DateUtils.formatStringToDate(_selectedDateInterval.value!!.second)

        return if (fromDate != null) {
            DateUtils.getCalendarFromDate(fromDate)
        } else {
            null
        }
    }

    fun updateToDate(newDate: String) {
        _selectedDateInterval.value = _selectedDateInterval.value!!.first to newDate
    }
}