package com.justvinny.github.noadsepubreader.cachedsettings

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.justvinny.github.noadsepubreader.CachedSettings
import java.io.InputStream
import java.io.OutputStream

object CachedSettingsSerializer : Serializer<CachedSettings> {
    override val defaultValue: CachedSettings = CachedSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): CachedSettings {
        try {
            return CachedSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: CachedSettings,
        output: OutputStream) = t.writeTo(output)
}

val Context.cachedSettingsDataStore: DataStore<CachedSettings> by dataStore(
    fileName = "settings.pb",
    serializer = CachedSettingsSerializer,
)
