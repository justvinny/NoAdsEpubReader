package com.justvinny.github.noadsepubreader.viewbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justvinny.github.noadsepubreader.R
import com.justvinny.github.noadsepubreader.utils.ScreenPreviews

@Composable
fun EmptyViewBookScreen(
    importEpub: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        onClick = importEpub,
    ){
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier.size(128.dp),
                imageVector = Icons.Filled.FileOpen,
                contentDescription = stringResource(R.string.open_epub_label_desc),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.no_epub_selected_label),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@ScreenPreviews
@Composable
fun EmptyViewBookScreenPreview() {
    EmptyViewBookScreen(
        importEpub = {},
    )
}