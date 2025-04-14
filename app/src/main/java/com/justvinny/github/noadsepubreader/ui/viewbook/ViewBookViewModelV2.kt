package com.justvinny.github.noadsepubreader.ui.viewbook

import androidx.lifecycle.ViewModel
import com.justvinny.github.noadsepubreader.logic.Constants
import io.documentnode.epub4j.domain.Book
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ViewBookViewModelV2(
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): ViewModel() {
    private val _state = MutableStateFlow(ViewBookState())
    val state = _state
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(Constants.DEFAULT_FLOW_SUBSCRIPTION_TIMEOUT),
            initialValue = ViewBookState(),
        )

    fun updateContents(book: Book?) {
        if (book == null) {
            // TODO - Think about how to handle if this is null. Maybe display a message to the user.
            return
        }

        val mapped = book
            .tableOfContents
            .tocReferences
            .map { tocRef ->
                TOCData(
                    title = tocRef.title,
                    href = tocRef.resource.href,
                )
            }
        val finalMapped = mapped.mapIndexed { index, tocData ->
            tocData.copy(
                next = mapped.getOrNull(index + 1),
                previous = mapped.getOrNull(index - 1),
            )
        }

        for (resource in book.resources.all) {
            println(resource)
        }

        _state.update {
            it.copy(contentsV2 = finalMapped, current = finalMapped.firstOrNull())
        }
    }

    fun updateCachedDirPath(path: String) {
        _state.update {
            it.copy(cachedDirPath = path)
        }
    }

    fun setLoading(isLoading: Boolean) {
        _state.update {
            it.copy(isLoading = isLoading)
        }
    }
}
