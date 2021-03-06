package com.riccardobusetti.unibztimetable.domain.strategies

import android.content.Context
import com.riccardobusetti.unibztimetable.data.sharedprefs.UserPrefsHelper
import com.riccardobusetti.unibztimetable.domain.entities.UserPrefs

/**
 * Shared preferences implementation to fetch the user preferences which are saved on device.
 *
 * @author Riccardo Busetti
 */
class SharedPreferencesUserPrefsStrategy(
    private val context: Context
) : UserPrefsStrategy {

    /**
     * @inheritDoc
     */
    override fun putUserPrefs(userPrefs: UserPrefs) {
        val userPrefsHelper = UserPrefsHelper(context)

        userPrefs.prefs.forEach {
            if (it.value != UserPrefs.NO_VALUE) {
                userPrefsHelper.putString(it.key.key, it.value)
            } else {
                userPrefsHelper.removeString(it.key.key)
            }
        }
    }

    /**
     * @inheritDoc
     */
    override fun getUserPrefs(): UserPrefs {
        val usersPrefsHelper = UserPrefsHelper(context)

        return UserPrefs(
            UserPrefs.Pref.values().mapNotNull {
                val value = usersPrefsHelper.getString(it.key)

                if (value != null) it to value else null
            }.toMap()
        )
    }

    override fun deleteUserPrefs(prefsKeys: List<UserPrefs.Pref>) {
        val usersPrefsHelper = UserPrefsHelper(context)

        prefsKeys.forEach {
            usersPrefsHelper.removeString(it.key)
        }
    }
}