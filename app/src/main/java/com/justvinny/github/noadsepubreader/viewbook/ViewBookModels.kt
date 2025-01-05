package com.justvinny.github.noadsepubreader.viewbook

data class ViewBookState(
    val contents: List<String> = listOf(),
    val searchTerm: String = "",
    val matchedResultIndex: Int = 0,
    val matchedResultsIndices: List<Int> = listOf(),
    val isLoading: Boolean = false,
)
