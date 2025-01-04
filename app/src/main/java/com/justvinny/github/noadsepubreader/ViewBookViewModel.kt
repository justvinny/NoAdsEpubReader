package com.justvinny.github.noadsepubreader

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ViewBookViewModel: ViewModel() {
    private val _state = MutableStateFlow(ViewBookState())
    val state = _state.asStateFlow()

    fun updateContent(content: String) {
        _state.update {
            it.copy(text = content)
        }
    }

    fun setLoading(isLoading: Boolean) {
        _state.update {
            it.copy(isLoading = isLoading)
        }
    }
}