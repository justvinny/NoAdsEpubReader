package com.justvinny.github.noadsepubreader.viewbook

import android.os.CountDownTimer
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justvinny.github.noadsepubreader.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ViewBookViewModel: ViewModel() {
    private val _state = MutableStateFlow(ViewBookState())
    val state = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(Constants.DEFAULT_FLOW_SUBSCRIPTION_TIMEOUT),
            initialValue = ViewBookState(),
        )

    private val timer = object : CountDownTimer(
        Constants.DEFAULT_TIMER_MAX_MS,
        Constants.DEFAULT_TIMER_INTERVAL_MS,
    ) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            updateSearchResults()
        }
    }

    fun updateContents(contents: List<String>) {
        _state.update {
            it.copy(contents = contents)
        }
    }

    fun setLoading(isLoading: Boolean) {
        _state.update {
            it.copy(isLoading = isLoading)
        }
    }

    fun search(searchTerm: String) {
        _state.update {
            it.copy(searchTerm = searchTerm)
        }

        timer.cancel()
        timer.start()
    }

    fun arrowUp() {
        _state.update {
            if (it.matchedResultsIndices.isNotEmpty()) {
                it.copy(matchedResultIndex = (it.matchedResultIndex - 1).mod(it.matchedResultsIndices.size))
            } else{
                it
            }
        }
    }

    fun arrowDown() {
        _state.update {
            if (it.matchedResultsIndices.isNotEmpty()) {
                it.copy(matchedResultIndex = (it.matchedResultIndex + 1).mod(it.matchedResultsIndices.size))
            } else{
                it
            }
        }
    }

    fun updateScrollPosition(scrollPosition: Int) {
        _state.update {
            it.copy(lazyListState = LazyListState(scrollPosition))
        }
    }

    private fun updateSearchResults() {
        _state.update {
            val matchedResultsIndices = mutableListOf<Int>()

            if (it.searchTerm.isNotBlank()) {
                for ((index, line) in it.contents.withIndex()) {
                    if (line.contains(it.searchTerm, ignoreCase = true)) {
                        matchedResultsIndices.add(index)
                    }
                }
            }

            it.copy(
                matchedResultIndex = 0,
                matchedResultsIndices = matchedResultsIndices,
            )
        }
    }
}
