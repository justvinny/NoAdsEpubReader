package com.justvinny.github.noadsepubreader

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.justvinny.github.noadsepubreader.ui.theme.NoAdsEpubReaderTheme

@Composable
fun ViewBookScreen(
    importEpub: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ViewBookViewModel = ViewBookViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Button(onClick = importEpub) {
            Text("Open Book")
        }
        Text(state.text)
    }
}

@Preview(showBackground = true)
@Composable
fun ViewBookScreenPreview() {
    NoAdsEpubReaderTheme {
        ViewBookScreen(importEpub = {}, modifier = Modifier.fillMaxSize())
    }
}