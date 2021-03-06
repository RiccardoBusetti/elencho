package com.riccardobusetti.unibztimetable.utils.custom

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.riccardobusetti.unibztimetable.ui.custom.TimetableViewModel

/**
 * Custom scroll listener for the recyclerview which detects when the user reached the bottom of
 * the list and keeps track of the current page.
 *
 * @see [https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews-and-RecyclerView]
 */
class EndlessRecyclerViewScrollListener(
    private val mLayoutManager: RecyclerView.LayoutManager,
    private val listState: TimetableViewModel.ListState,
    private val callback: (Int) -> Unit
) : RecyclerView.OnScrollListener() {

    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private var visibleThreshold = 5

    // Sets the starting page index
    private val startingPageIndex = 0

    private fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        val totalItemCount = mLayoutManager.itemCount
        val lastVisibleItemPosition =
            (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < listState.previousTotalItemCount) {
            listState.currentPage = this.startingPageIndex
            listState.previousTotalItemCount = totalItemCount

            if (totalItemCount == 0) {
                listState.loading = true
            }
        }
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (listState.loading && totalItemCount > listState.previousTotalItemCount) {
            listState.loading = false
            listState.previousTotalItemCount = totalItemCount
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (!listState.loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
            listState.currentPage++
            callback(listState.currentPage)
            listState.loading = true
        }
    }
}