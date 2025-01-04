package com.justvinny.github.noadsepubreader.viewbook

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

    fun updateContent(bookText: String) {
        _state.update {
            it.copy(bookText = bookText)
        }
    }

    fun setLoading(isLoading: Boolean) {
        _state.update {
            it.copy(isLoading = isLoading)
        }
    }
}