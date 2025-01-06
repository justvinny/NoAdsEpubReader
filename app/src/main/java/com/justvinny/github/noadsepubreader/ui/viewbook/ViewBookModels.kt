package com.justvinny.github.noadsepubreader.ui.viewbook

import androidx.compose.foundation.lazy.LazyListState

data class ViewBookState(
    val lazyListState: LazyListState = LazyListState(),
    val contents: List<String> = listOf(),
    val searchTerm: String = "",
    val matchedResultIndex: Int = 0,
    val matchedResultsIndices: List<Int> = listOf(),
    val isLoading: Boolean = false,
)
