package com.justvinny.github.noadsepubreader.viewbook

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ViewBookViewModel: ViewModel() {
    private val _state = MutableStateFlow(ViewBookState())
    val state = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ViewBookState(),
        )

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
            val matchedResultsIndices = mutableListOf<Int>()

            if (searchTerm.isNotBlank()) {
                for ((index, line) in it.contents.withIndex()) {
                    if (line.contains(searchTerm, ignoreCase = true)) {
                        matchedResultsIndices.add(index)
                    }
                }
            }

            it.copy(
                searchTerm = searchTerm,
                matchedResultIndex = 0,
                matchedResultsIndices = matchedResultsIndices,
            )
        }
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
}
