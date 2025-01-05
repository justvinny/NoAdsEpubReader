package com.justvinny.github.noadsepubreader.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.justvinny.github.noadsepubreader.R
import com.justvinny.github.noadsepubreader.ui.theme.NoAdsEpubReaderTheme
import com.justvinny.github.noadsepubreader.utils.ComponentPreviews

@Composable
fun SearchComponent(
    searchKeyword: String,
    matchedResultIndex: Int,
    matchedResultsIndices: List<Int>,
    onSearchValueChanged: (String) -> Unit,
    onUpArrowClicked: () -> Unit,
    onDownArrowClicked: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(192.dp)) {
            OutlinedTextField(
                value = searchKeyword,
                onValueChange = {
                    onSearchValueChanged(it)
                },
                label = {
                    Text(stringResource(R.string.search_label))
                },
                suffix = {
                    Text(
                        text = searchKeyword.getTextFieldSuffix(
                            matchedResultIndex = matchedResultIndex,
                            matchedResultsIndicesSize = matchedResultsIndices.size,
                        ),
                    )
                }
            )
        }

        val enableUpButton = matchedResultsIndices.isNotEmpty()
        IconButton(
            onClick = onUpArrowClicked,
            enabled = enableUpButton,
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = stringResource(R.string.up_arrow_button_desc),
                tint = enableUpButton.getArrowButtonColor(),
            )
        }

        val enableDownButton = matchedResultsIndices.isNotEmpty()
        IconButton(
            onClick = onDownArrowClicked,
            enabled = enableDownButton,
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(R.string.down_arrow_button_desc),
                tint = enableDownButton.getArrowButtonColor(),
            )
        }
    }
}

@ReadOnlyComposable
@Composable
private fun String.getTextFieldSuffix(matchedResultIndex: Int, matchedResultsIndicesSize: Int): String {
    return when (this.isNotBlank()) {
        true -> "${matchedResultIndex + 1} / $matchedResultsIndicesSize"
        false -> ""
    }
}

@ReadOnlyComposable
@Composable
private fun Boolean.getArrowButtonColor(): Color {
    return when (this) {
        true -> MaterialTheme.colorScheme.primary
        false -> LocalContentColor.current
    }
}

@ComponentPreviews
@Composable
fun SearchComponentWithTextAndUpArrowDisabledPreview() {
    NoAdsEpubReaderTheme {
        SearchComponent(
            searchKeyword = "Test",
            matchedResultIndex = 0,
            matchedResultsIndices = listOf(4, 10, 24),
            onSearchValueChanged = {},
            onUpArrowClicked = {},
            onDownArrowClicked = {},
        )
    }
}

@ComponentPreviews
@Composable
fun SearchComponentWithTextAndDownArrowDisabledPreview() {
    NoAdsEpubReaderTheme {
        SearchComponent(
            searchKeyword = "Test",
            matchedResultIndex = 2,
            matchedResultsIndices = listOf(4, 10, 24),
            onSearchValueChanged = {},
            onUpArrowClicked = {},
            onDownArrowClicked = {},
        )
    }
}

@ComponentPreviews
@Composable
fun SearchComponentEmptyAndArrowsDisabledPreview() {
    NoAdsEpubReaderTheme {
        SearchComponent(
            searchKeyword = "",
            matchedResultIndex = 0,
            matchedResultsIndices = listOf(),
            onSearchValueChanged = {},
            onUpArrowClicked = {},
            onDownArrowClicked = {},
        )
    }
}
