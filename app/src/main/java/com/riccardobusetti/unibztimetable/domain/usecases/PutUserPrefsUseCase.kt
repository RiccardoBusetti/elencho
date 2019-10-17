package com.riccardobusetti.unibztimetable.domain.usecases

import android.util.Log
import com.riccardobusetti.unibztimetable.domain.entities.UserPrefs
import com.riccardobusetti.unibztimetable.domain.repositories.UserPrefsRepository

/**
 * Use case that will manage the saving of user preferences.
 *
 * @author Riccardo Busetti
 */
class PutUserPrefsUseCase(
    private val userPrefsRepository: UserPrefsRepository
) : UseCase {

    companion object {
        private const val TAG = "PutUserPrefsUseCase"
    }

    fun putUserPrefs(userPrefs: UserPrefs) = try {
        userPrefsRepository.putUserPrefs(userPrefs)
        true
    } catch (e: Exception) {
        Log.d(TAG, "An error occurred while saving user preferences -> $e")

        false
    }

    fun putUserPrefs(departmentId: String, degreeId: String, studyPlanId: String) = putUserPrefs(
        UserPrefs(
            mapOf(
                UserPrefs.Pref.DEPARTMENT_ID to departmentId,
                UserPrefs.Pref.DEGREE_ID to degreeId,
                UserPrefs.Pref.STUDY_PLAN_ID to studyPlanId
            )
        )
    )
}