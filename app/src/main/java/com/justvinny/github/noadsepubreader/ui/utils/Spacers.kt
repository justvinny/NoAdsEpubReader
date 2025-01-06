package com.justvinny.github.noadsepubreader.ui.utils

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ColumnScope.VerticalFillSpacer(weight: Float = 1f) {
    Spacer(modifier = Modifier.weight(weight))
}

@Composable
fun RowScope.HorizontalFillSpacer(weight: Float = 1f) {
    Spacer(modifier = Modifier.weight(weight))
}