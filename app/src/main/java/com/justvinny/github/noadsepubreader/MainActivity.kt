@file:OptIn(ExperimentalReadiumApi::class)

package com.justvinny.github.noadsepubreader

import android.content.Intent
import android.net.Uri
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
import com.justvinny.github.noadsepubreader.cachedsettings.CachedSettingsRepository
import com.justvinny.github.noadsepubreader.ui.theme.NoAdsEpubReaderTheme
import com.justvinny.github.noadsepubreader.viewbook.ViewBookScreen
import com.justvinny.github.noadsepubreader.viewbook.ViewBookViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
    private lateinit var epubLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var assetRetriever: AssetRetriever
    private lateinit var publicationOpener: PublicationOpener
    private lateinit var cachedSettingsRepository: CachedSettingsRepository

    private val viewBookViewModel = ViewBookViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        epubLauncher = getEpubContent()
        cachedSettingsRepository = CachedSettingsRepository(applicationContext)

        val httpClient = DefaultHttpClient()
        assetRetriever = AssetRetriever(
            contentResolver = applicationContext.contentResolver,
            httpClient = httpClient,
        )
        publicationOpener = PublicationOpener(
            publicationParser = DefaultPublicationParser(
                applicationContext,
                httpClient = httpClient,
                assetRetriever = assetRetriever,
                pdfFactory = PdfiumDocumentFactory(applicationContext)
            )
        )

        lifecycleScope.launch {
            val cachedSettings = cachedSettingsRepository.cachedSettings.first()

            val uri = cachedSettings.bookFileUri
            if (!uri.isNullOrEmpty()) {
                openEpub(uri)
            }

            val lastScrollIndex = cachedSettings.lastScrollIndex
            if (lastScrollIndex > 0) {
                viewBookViewModel.updateScrollPosition(lastScrollIndex)
            }
        }

        setContent {
            NoAdsEpubReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ViewBookScreen(
                        importEpub = ::importEpub,
                        viewModel = viewBookViewModel,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (::cachedSettingsRepository.isInitialized) {
            lifecycleScope.launch {
                val lastScrollIndex = viewBookViewModel.state.first().lazyListState.firstVisibleItemIndex
                cachedSettingsRepository.updateLastScrollIndex(lastScrollIndex)
            }
        }
    }

    private fun getEpubContent() = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null && it.path != null) {
            lifecycleScope.launch {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                cachedSettingsRepository.updateBookFileUri(it.toString())
                openEpub(it.toString())
            }
        }
    }

    private fun importEpub() {
        if (::epubLauncher.isInitialized) {
            epubLauncher.launch(arrayOf(EPUB_MIME_TYPE))
        }
    }

    private suspend fun openEpub(uriString: String) {
        viewBookViewModel.setLoading(true)

        withContext(Dispatchers.IO) {
            val fileDescriptor = contentResolver.openFileDescriptor(Uri.parse(uriString), "r") ?: return@withContext
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val tempFile = File.createTempFile("temp_epub", ".epub", cacheDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            val url = tempFile.toUrl()

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
                .mapNotNull { it.text }

            viewBookViewModel.updateContents(textualElements)

            for (link in publication.tableOfContents) {
                Log.i(TAG, "getEpubContent table of contents: $link")
            }

            tempFile.deleteOnExit()
        }

        viewBookViewModel.setLoading(false)
    }
}
