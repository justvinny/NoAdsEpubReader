@file:OptIn(ExperimentalReadiumApi::class)

package com.justvinny.github.noadsepubreader

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.justvinny.github.noadsepubreader.ui.theme.NoAdsEpubReaderTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.content.Content
import org.readium.r2.shared.publication.services.content.content
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.toUrl
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import java.io.File
import java.io.FileInputStream

const val EPUB_MIME_TYPE = "application/epub+zip"
const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var epubLauncher: ActivityResultLauncher<String>
    private val viewBookViewModel = ViewBookViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        epubLauncher = getEpubContent()

        setContent {
            NoAdsEpubReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ViewBookScreen(
                        importEpub = ::importEpub,
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewBookViewModel,
                    )
                }
            }
        }
    }

    private fun getEpubContent() = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null && it.path != null) {
            val httpClient = DefaultHttpClient()
            val assetRetriever = AssetRetriever(
                contentResolver = applicationContext.contentResolver,
                httpClient = httpClient,
            )
            val publicationOpener = PublicationOpener(
                publicationParser = DefaultPublicationParser(
                    applicationContext,
                    httpClient = httpClient,
                    assetRetriever = assetRetriever,
                    pdfFactory = PdfiumDocumentFactory(applicationContext)
                )
            )

            val fileDescriptor = contentResolver.openFileDescriptor(it, "r") ?: return@registerForActivityResult
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val tempFile = File.createTempFile("temp_epub", ".epub", cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            val url = tempFile.toUrl()

            lifecycleScope.launch {
                viewBookViewModel.setLoading(true)

                withContext(Dispatchers.IO) {
                    val asset = assetRetriever.retrieve(url).getOrElse { exception ->
                        Log.e(TAG, "getEpubContent assetRetriever message: ${exception.message} | cause: ${exception.cause}")
                    } as? Asset ?: return@withContext

                    val publication = publicationOpener.open(asset, allowUserInteraction = true).getOrElse { exception ->
                        Log.e(TAG, "getEpubContent publicationOpener message: ${exception.message} | cause: ${exception.cause}")
                    } as? Publication ?: return@withContext

                    val content = publication.content() ?: return@withContext
                    val textualElements = content
                        .elements()
                        .filterIsInstance<Content.TextualElement>()

                    Log.i(TAG, "getEpubContent length: ${textualElements.count()}")
                    for (element in textualElements) {
                        Log.i(TAG, "getEpubContent element title: ${element.text}")
                    }

                    val wholeText = textualElements
                        .mapNotNull { element -> element.text }
                        .joinToString(separator = "\n\n")

                    viewBookViewModel.updateContent(wholeText)

                    for (link in publication.tableOfContents) {
                        Log.i(TAG, "getEpubContent table of contents: $link")
                    }
                }

                viewBookViewModel.setLoading(false)
            }
        }
    }

    private fun importEpub() {
        if (::epubLauncher.isInitialized) {
            epubLauncher.launch(EPUB_MIME_TYPE)
        }
    }
}

