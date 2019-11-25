package com.riccardobusetti.unibztimetable.domain.entities

data class DisplayableCourseGroup(
    val title: String,
    val isNow: Boolean = false,
    val courses: List<DisplayableCourse>
) {


    companion object {

        fun build(courses: List<Kourse>, customGrouping: ((Kourse) -> String?)? = null) =
            courses.groupBy {
                if (customGrouping != null) {
                    customGrouping(it) ?: defaultGrouping(it)
                } else {
                    defaultGrouping(it)
                }
            }.map {
                DisplayableCourseGroup(
                    title = it.key,
                    isNow = it.value.first().isOngoing(),
                    courses = it.value.map { course -> DisplayableCourse.build(course) }
                )
            }

        private fun defaultGrouping(course: Kourse) =
            "${course.endDateTime.dayOfMonth} ${course.endDateTime.month} ${course.endDateTime.year}"
    }
}