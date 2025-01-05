package com.justvinny.github.noadsepubreader.viewbook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.justvinny.github.noadsepubreader.LoadingScreen
import com.justvinny.github.noadsepubreader.search.SearchComponent
import com.justvinny.github.noadsepubreader.ui.theme.NoAdsEpubReaderTheme
import com.justvinny.github.noadsepubreader.utils.HorizontalFillSpacer
import com.justvinny.github.noadsepubreader.utils.ScreenPreviews

@Composable
fun ViewBookScreen(
    importEpub: () -> Unit,
    viewModel: ViewBookViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        LoadingScreen()
    } else {
        Column(modifier = modifier) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = importEpub) {
                    Icon(
                        imageVector = Icons.Filled.FileOpen,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                HorizontalFillSpacer()

                SearchComponent(
                    searchKeyword = state.searchTerm,
                    matchedResultIndex = state.matchedResultIndex,
                    matchedResultsIndices = state.matchedResultsIndices,
                    onSearchValueChanged = viewModel::search,
                    onUpArrowClicked = viewModel::arrowUp,
                    onDownArrowClicked = viewModel::arrowDown,
                )
            }

            ViewBookComponent(
                matchedResultIndex = state.matchedResultIndex,
                matchedResultsIndices = state.matchedResultsIndices,
                contents = state.contents,
            )
        }
    }
}

@Composable
private fun ViewBookComponent(
    matchedResultIndex: Int,
    matchedResultsIndices: List<Int>,
    contents: List<String>,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(matchedResultIndex, matchedResultsIndices) {
        if (matchedResultsIndices.isNotEmpty()) {
            listState.animateScrollToItem(matchedResultsIndices[matchedResultIndex])
        }
    }

    LazyColumn(state = listState) {
        itemsIndexed(
            items = contents,
            key = { index, line -> "$index-$line".hashCode() }
        ) { _, line ->
            Text(modifier = Modifier.padding(bottom = 12.dp), text = line)
        }
    }
}

@ScreenPreviews
@Composable
fun ViewBookScreenLoadingPreview() {
    NoAdsEpubReaderTheme {
        ViewBookComponent(
            matchedResultIndex = 0,
            matchedResultsIndices = listOf(),
            contents = listOf(),
        )
    }
}

@ScreenPreviews
@Composable
fun ViewBookScreenPreview() {
    NoAdsEpubReaderTheme {
        ViewBookComponent(
            matchedResultIndex = 0,
            matchedResultsIndices = listOf(),
            contents = listOf("Some text.", "Second line", "gskjlgnsdkvnksn mf alcmlc"),
        )
    }
}
