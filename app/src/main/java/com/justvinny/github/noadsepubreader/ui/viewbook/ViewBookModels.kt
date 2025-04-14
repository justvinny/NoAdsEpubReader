package com.justvinny.github.noadsepubreader.ui.viewbook

import androidx.compose.foundation.lazy.LazyListState

data class ViewBookState(
    val lazyListState: LazyListState = LazyListState(),
    val contents: List<String> = listOf(),
    val contentsV2: List<TOCData> = listOf(),
    var current: TOCData? = null,
    var cachedDirPath: String = "",
    val searchTerm: String = "",
    val matchedResultIndex: Int = 0,
    val matchedResultsIndices: List<Int> = listOf(),
    val isLoading: Boolean = false,
)

data class TOCData(
    val title: String,
    val href: String,
    val next: TOCData? = null,
    val previous: TOCData? = null,
)