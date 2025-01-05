package com.justvinny.github.noadsepubreader.bottombar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.justvinny.github.noadsepubreader.R
import com.justvinny.github.noadsepubreader.search.SearchComponent
import com.justvinny.github.noadsepubreader.ui.theme.NoAdsEpubReaderTheme
import com.justvinny.github.noadsepubreader.utils.ComponentPreviews
import com.justvinny.github.noadsepubreader.utils.HorizontalFillSpacer
import com.justvinny.github.noadsepubreader.viewbook.ViewBookViewModel

@Composable
fun BottomBar(
    importEpub: () -> Unit,
    showAppBar: Boolean,
    viewBookViewModel: ViewBookViewModel
) {
    val viewBookState by viewBookViewModel.state.collectAsState()

    if (!viewBookState.isLoading && showAppBar) {
        BottomBarContent(
            importEpub = importEpub,
            searchTerm = viewBookState.searchTerm,
            matchedResultIndex = viewBookState.matchedResultIndex,
            matchedResultsIndices = viewBookState.matchedResultsIndices,
            onSearch = viewBookViewModel::search,
            onArrowUp = viewBookViewModel::arrowUp,
            onArrowDown = viewBookViewModel::arrowDown,
        )
    }
}

@Composable
private fun BottomBarContent(
    importEpub: () -> Unit,
    searchTerm: String,
    matchedResultIndex: Int,
    matchedResultsIndices: List<Int>,
    onSearch: (String) -> Unit,
    onArrowUp: () -> Unit,
    onArrowDown: () -> Unit,
) {
    BottomAppBar {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = importEpub) {
                Icon(
                    imageVector = Icons.Filled.FileOpen,
                    contentDescription = stringResource(R.string.open_epub_label_desc),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            HorizontalFillSpacer()

            SearchComponent(
                searchKeyword = searchTerm,
                matchedResultIndex = matchedResultIndex,
                matchedResultsIndices = matchedResultsIndices,
                onSearchValueChanged = onSearch,
                onUpArrowClicked = onArrowUp,
                onDownArrowClicked = onArrowDown,
            )
        }
    }
}

@ComponentPreviews
@Composable
fun BottomBarContentPreview() {
    NoAdsEpubReaderTheme {
        BottomBarContent(
            importEpub = {},
            searchTerm = "",
            matchedResultIndex = 0,
            matchedResultsIndices = listOf(),
            onSearch = {},
            onArrowUp = {},
            onArrowDown = {},
        )
    }
}