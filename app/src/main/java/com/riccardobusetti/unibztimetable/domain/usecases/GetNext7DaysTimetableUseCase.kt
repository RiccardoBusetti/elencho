package com.riccardobusetti.unibztimetable.domain.usecases

import com.riccardobusetti.unibztimetable.domain.entities.Day
import com.riccardobusetti.unibztimetable.domain.repositories.TimetableRepository
import com.riccardobusetti.unibztimetable.network.WebSiteLink

/**
 * Use case which will manage the next 7 days timetable that is responsible of
 * showing to the user the timetable of the next 7 days.
 *
 * @author Riccardo Busetti
 */
class GetNext7DaysTimetableUseCase(
    private val timetableRepository: TimetableRepository
) : UseCase {

    /**
     * Gets the timetable for the next 7 days of the week.
     */
    fun getNext7DaysTimetable(
        department: String,
        degree: String,
        academicYear: String,
        page: String
    ): List<Day> {
        val webSiteLink =
            WebSiteLink.Builder()
                .useDeviceLanguage()
                .withDepartment(department)
                .withDegree(degree)
                .withAcademicYear(academicYear)
                .fromToday()
                .toNext7Days()
                .atPage(page)
                .build()

        return timetableRepository.getTimetable(webSiteLink)
    }
}