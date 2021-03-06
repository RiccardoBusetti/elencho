package com.riccardobusetti.unibztimetable.ui.timemachine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.riccardobusetti.unibztimetable.R
import com.riccardobusetti.unibztimetable.domain.entities.app.AppSection
import com.riccardobusetti.unibztimetable.domain.repositories.TimetableRepository
import com.riccardobusetti.unibztimetable.domain.repositories.UserPrefsRepository
import com.riccardobusetti.unibztimetable.domain.strategies.LocalTimetableStrategy
import com.riccardobusetti.unibztimetable.domain.strategies.RemoteTimetableStrategy
import com.riccardobusetti.unibztimetable.domain.strategies.SharedPreferencesUserPrefsStrategy
import com.riccardobusetti.unibztimetable.domain.usecases.GetIntervalDateTimetableUseCase
import com.riccardobusetti.unibztimetable.domain.usecases.GetUserPrefsUseCase
import com.riccardobusetti.unibztimetable.ui.custom.TimetableFragment
import com.riccardobusetti.unibztimetable.ui.custom.TimetableViewModel
import com.riccardobusetti.unibztimetable.utils.DateUtils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.bottom_sheet_date_interval.view.*
import kotlinx.android.synthetic.main.fragment_time_machine.*

class TimeMachineFragment : TimetableFragment<TimeMachineViewModel>() {

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()

    private lateinit var bottomSheetView: View
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var fromDateText: Button
    private lateinit var toDateText: Button
    private lateinit var timeTravelButton: Button
    private lateinit var floatingActionButton: FloatingActionButton

    override val appSection: AppSection
        get() = AppSection.TIME_MACHINE

    override fun initViewModel(): TimeMachineViewModel {
        val timetableRepository =
            TimetableRepository(
                LocalTimetableStrategy(requireContext()),
                RemoteTimetableStrategy()
            )

        val userPrefsRepository =
            UserPrefsRepository(SharedPreferencesUserPrefsStrategy(requireContext()))

        return ViewModelProviders.of(
            this,
            TimeMachineViewModelFactory(
                requireContext(),
                GetIntervalDateTimetableUseCase(timetableRepository),
                GetUserPrefsUseCase(userPrefsRepository)
            )
        ).get(TimeMachineViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_time_machine, container, false)
    }

    override fun setupUI() {
        parentLayout = fragment_time_machine_parent

        bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_date_interval, null)

        bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.setOnCancelListener { changeBottomSheetState() }

        loadingView = fragment_time_machine_lottie_loading_view

        statusView = fragment_time_machine_status_view

        fromDateText = bottomSheetView.bottom_sheet_date_interval_from_text
        fromDateText.setOnClickListener {
            MaterialDialog(requireContext()).show {
                datePicker(
                    currentDate = model?.getCurrentFromDate()
                ) { _, date ->
                    model?.updateFromDate(DateUtils.formatDateToString(date.time))
                }
            }
        }

        toDateText = bottomSheetView.bottom_sheet_date_interval_to_text
        toDateText.setOnClickListener {
            MaterialDialog(requireContext()).show {
                datePicker(
                    currentDate = model?.getCurrentToDate()
                ) { _, date ->
                    model?.updateToDate(DateUtils.formatDateToString(date.time))
                }
            }
        }

        timeTravelButton = bottomSheetView.bottom_sheet_date_interval_button
        timeTravelButton.setOnClickListener {
            reloadTimetable()
            changeBottomSheetState()
        }

        swipeToRefreshLayout = fragment_time_machine_swipe_refresh

        recyclerView = fragment_time_machine_recycler_view
        recyclerView?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = groupAdapter
            scrollListener = onEndReached(model?.listState!!) { page ->
                loadTimetableNewPage(page)
            }
        }

        floatingActionButton = fragment_time_machine_fab
        floatingActionButton.setOnClickListener { changeBottomSheetState() }
    }

    override fun attachObservers() {
        model?.let {
            it.timetable.observe(this, Observer { timetable ->
                groupAdapter.apply {
                    recyclerView?.let { recyclerView ->
                        groupAdapter.clearAndAddTimetable(timetable, recyclerView)
                    }
                }
            })

            it.error.observe(this, Observer { error ->
                if (error != null) {
                    showError(error)
                } else {
                    hideError()
                }
            })

            it.loadingState.observe(this, Observer { loadingState ->
                when (loadingState) {
                    TimetableViewModel.TimetableLoadingState.LOADING_FROM_SCRATCH -> showLoadingView()
                    TimetableViewModel.TimetableLoadingState.LOADING_WITH_DATA -> {
                    }
                    TimetableViewModel.TimetableLoadingState.NOT_LOADING -> hideLoadingView()
                    else -> hideLoadingView()
                }
            })

            it.selectedDateInterval.observe(this, Observer { interval ->
                fromDateText.text = interval.first
                toDateText.text = interval.second
            })

            it.bottomSheetState.observe(this, Observer { bottomSheetState ->
                when (bottomSheetState) {
                    TimeMachineViewModel.BottomSheetState.OPENED -> bottomSheetDialog.show()
                    TimeMachineViewModel.BottomSheetState.CLOSED -> bottomSheetDialog.hide()
                    else -> bottomSheetDialog.hide()
                }
            })
        }
    }

    override fun loadData() {
        loadTimetable(true)
    }

    override fun reloadData() {
        reloadTimetable()
    }

    private fun loadTimetable(isReset: Boolean) {
        model?.requestTimetable(
            TimetableViewModel.TimetableRequest(
                model!!.selectedDateInterval.value!!.first,
                model!!.selectedDateInterval.value!!.second,
                model!!.currentPage,
                isReset
            )
        )
    }

    private fun reloadTimetable() {
        model?.let {
            recyclerView?.scrollToPosition(0)
            it.enableListAnimation()
            it.resetListState()
            it.updateCurrentPage(TimetableViewModel.DEFAULT_PAGE)
            loadTimetable(true)
        }
    }

    private fun loadTimetableNewPage(page: String) {
        model?.let {
            it.updateCurrentPage(page)
            loadTimetable(false)
        }
    }

    private fun changeBottomSheetState() {
        model?.updateBottomSheetState(
            when (model!!.bottomSheetState.value) {
                TimeMachineViewModel.BottomSheetState.CLOSED -> TimeMachineViewModel.BottomSheetState.OPENED
                TimeMachineViewModel.BottomSheetState.OPENED -> TimeMachineViewModel.BottomSheetState.CLOSED
                null -> TimeMachineViewModel.BottomSheetState.CLOSED
            }
        )
    }
}