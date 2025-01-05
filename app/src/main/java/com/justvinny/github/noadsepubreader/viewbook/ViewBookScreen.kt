package com.justvinny.github.noadsepubreader.viewbook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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
                listState = state.lazyListState,
                searchTerm = state.searchTerm,
                matchedResultIndex = state.matchedResultIndex,
                matchedResultsIndices = state.matchedResultsIndices,
                contents = state.contents,
            )
        }
    }
}

@Composable
private fun ViewBookComponent(
    listState: LazyListState,
    searchTerm: String,
    matchedResultIndex: Int,
    matchedResultsIndices: List<Int>,
    contents: List<String>,
) {
    val textModifier = Modifier.padding(bottom = 12.dp)

    LaunchedEffect(matchedResultIndex, matchedResultsIndices) {
        if (matchedResultsIndices.isNotEmpty()) {
            listState.animateScrollToItem(matchedResultsIndices[matchedResultIndex])
        }
    }

    LazyColumn(state = listState) {
        itemsIndexed(
            items = contents,
            key = { index, line -> "$index-$line".hashCode() }
        ) { index, line ->
            if (index in matchedResultsIndices) {
                val regex = searchTerm.toRegex(RegexOption.IGNORE_CASE)
                val allMatches = regex.findAll(line)

                Text(
                    modifier = textModifier,
                    text = buildAnnotatedString {
                        append(line)

                        for (matched in allMatches) {
                            addStyle(
                                style = SpanStyle(
                                    background = MaterialTheme.colorScheme.primary,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                ),
                                start = matched.range.first,
                                end = matched.range.last + 1,
                            )
                        }
                    }
                )
            } else {
                Text(modifier = textModifier, text = line)
            }
        }
    }
}

@ScreenPreviews
@Composable
fun ViewBookScreenLoadingPreview() {
    NoAdsEpubReaderTheme {
        ViewBookComponent(
            listState = LazyListState(),
            searchTerm = "",
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
            listState = LazyListState(),
            searchTerm = "",
            matchedResultIndex = 0,
            matchedResultsIndices = listOf(),
            contents = listOf("Some text.", "Second line", "gskjlgnsdkvnksn mf alcmlc"),
        )
    }
}
