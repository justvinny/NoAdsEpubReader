package com.justvinny.github.noadsepubreader.logic

import android.util.Log
import com.justvinny.github.noadsepubreader.exceptions.InvalidBookException
import io.documentnode.epub4j.domain.Book
import io.documentnode.epub4j.epub.EpubReader
import java.io.File
import java.io.InputStream

const val CACHED_DIR_NAME = "extracted_epub"
private const val TAG = "EpubParserV2"

class EpubParserV2(
    private val epubReader: EpubReader
) {
    fun parse(inputStream: InputStream, cachedDir: File): Book? {
        try {
            val book = epubReader.readEpub(inputStream)

            val resources = book.resources.all

            if (resources.isEmpty()) {
                throw InvalidBookException("Epub has no resources")
            }

            val outputDir = File(cachedDir, CACHED_DIR_NAME)

            if (outputDir.exists()) {
                outputDir.deleteRecursively()
            }

            for (resource in resources) {
                if (resource.mediaType.name.contains("css")) {
                    continue
                }

                val outFile = File(outputDir, resource.href)
                outFile.parentFile?.mkdirs()
                outFile.outputStream().use { outputStream ->
                    resource.inputStream.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            return book
        } catch (ex: Exception) {
            Log.e(TAG, ex.message, ex)
            return null
        }
    }
}
