package com.riccardobusetti.unibztimetable.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.riccardobusetti.unibztimetable.R
import com.riccardobusetti.unibztimetable.domain.entities.app.AppFragment
import com.riccardobusetti.unibztimetable.ui.adapters.SetupFragmentsAdapter
import com.riccardobusetti.unibztimetable.ui.choosefaculty.ChooseFacultyFragment
import com.riccardobusetti.unibztimetable.ui.choosenotificationtime.ChooseNotificationTimeFragment
import com.riccardobusetti.unibztimetable.ui.custom.BackableFragment
import com.riccardobusetti.unibztimetable.ui.main.MainActivity
import com.riccardobusetti.unibztimetable.utils.custom.views.NonSwipeableViewPager
import kotlinx.android.synthetic.main.activity_setup.*

class SetupActivity : AppCompatActivity() {

    private var currentSetupPhaseIndex = 1

    private val setupFragments: List<AppFragment>
        get() = listOf(
            AppFragment(
                index = 0,
                fragment = ChooseFacultyFragment()
            ),
            AppFragment(
                index = 1,
                fragment = ChooseNotificationTimeFragment()
            )
        )

    private lateinit var viewPager: NonSwipeableViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        setupUI()
    }

    private fun setupUI() {
        viewPager = activity_setup_view_pager
        viewPager.adapter = SetupFragmentsAdapter(setupFragments, supportFragmentManager)
    }

    fun nextSetupPhase() {
        if (currentSetupPhaseIndex < setupFragments.size) {
            currentSetupPhaseIndex++
            updateViewPagerPosition()
        } else {
            finishSetup()
        }
    }

    fun previousSetupPhase() {
        if (currentSetupPhaseIndex > 1) {
            currentSetupPhaseIndex--
            updateViewPagerPosition()
        } else {
            finish()
        }
    }

    private fun updateViewPagerPosition() {
        viewPager.currentItem = currentSetupPhaseIndex
    }

    private fun finishSetup() {
        Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(this)
            finish()
        }
    }

    override fun onBackPressed() {
        val currentVisibleFragment = getCurrentVisibleFragment()
        if (currentVisibleFragment is BackableFragment) {
            currentVisibleFragment.onBackPressed()
        }
    }

    private fun getCurrentVisibleFragment() =
        supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.activity_setup_view_pager + ":" + viewPager.currentItem)
}