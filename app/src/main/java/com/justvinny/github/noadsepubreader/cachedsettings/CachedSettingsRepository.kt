package com.justvinny.github.noadsepubreader.cachedsettings

import android.content.Context
import com.justvinny.github.noadsepubreader.CachedSettings
import kotlinx.coroutines.flow.Flow

class CachedSettingsRepository(
    private val context: Context,
) {
    val cachedSettings: Flow<CachedSettings> = context.cachedSettingsDataStore.data

    suspend fun updateBookFileUri(bookFileUri: String) {
        context.cachedSettingsDataStore.updateData {
            it.toBuilder()
                .setBookFileUri(bookFileUri)
                .build()
        }
    }
}
