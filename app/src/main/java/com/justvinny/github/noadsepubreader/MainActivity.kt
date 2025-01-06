package com.justvinny.github.noadsepubreader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.justvinny.github.noadsepubreader.Constants.EPUB_MIME_TYPE
import com.justvinny.github.noadsepubreader.bottombar.BottomBar
import com.justvinny.github.noadsepubreader.cachedsettings.CachedSettingsRepository
import com.justvinny.github.noadsepubreader.ui.theme.NoAdsEpubReaderTheme
import com.justvinny.github.noadsepubreader.utils.EpubParser
import com.justvinny.github.noadsepubreader.utils.EpubParser.Companion.toTextualElementStringList
import com.justvinny.github.noadsepubreader.viewbook.EmptyViewBookScreen
import com.justvinny.github.noadsepubreader.viewbook.ViewBookScreen
import com.justvinny.github.noadsepubreader.viewbook.ViewBookViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.toAbsoluteUrl
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser

class MainActivity : ComponentActivity() {
    private lateinit var epubLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cachedSettingsRepository: CachedSettingsRepository
    private lateinit var epubParser: EpubParser

    private val viewBookViewModel = ViewBookViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupEpubParser()
        epubLauncher = getEpubContent()
        cachedSettingsRepository = CachedSettingsRepository(applicationContext)

        lifecycleScope.launch {
            useCachedDataIfAvailable()
        }

        setContent {
            val viewBookState by viewBookViewModel.state.collectAsState()
            val hasBookToShow = viewBookState.contents.isNotEmpty()
            var showAppBar by rememberSaveable { mutableStateOf(true) }

            NoAdsEpubReaderTheme {
                if (viewBookState.isLoading) {
                    LoadingScreen()
                } else if (hasBookToShow) {
                    Surface(
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            showAppBar = !showAppBar
                        }
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = { BottomBar(
                                importEpub = ::importEpub,
                                showAppBar = showAppBar,
                                viewBookViewModel = viewBookViewModel,
                            )
                            },
                        ) { innerPadding ->
                            ViewBookScreen(
                                viewModel = viewBookViewModel,
                                modifier = Modifier.padding(innerPadding),
                            )
                        }
                    }
                } else {
                    EmptyViewBookScreen(importEpub = ::importEpub)
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

                it.toAbsoluteUrl()?.let { absoluteUrl ->
                    openEpub(absoluteUrl)
                }
            }
        }
    }

    private fun importEpub() {
        if (::epubLauncher.isInitialized) {
            epubLauncher.launch(arrayOf(EPUB_MIME_TYPE))
        }
    }

    private fun setupEpubParser() {
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

        epubParser = EpubParser(
            assetRetriever = assetRetriever,
            publicationOpener = publicationOpener,
        )
    }

    private suspend fun useCachedDataIfAvailable() {
        val cachedSettings = cachedSettingsRepository.cachedSettings.first()

        Uri.parse(cachedSettings.bookFileUri).toAbsoluteUrl()?.let { absoluteUrl ->
            openEpub(absoluteUrl)
        }

        viewBookViewModel.updateScrollPosition(cachedSettings.lastScrollIndex)
    }

    @OptIn(ExperimentalReadiumApi::class)
    private suspend fun openEpub(absoluteUrl: AbsoluteUrl) {
        withContext(Dispatchers.IO) {
            viewBookViewModel.setLoading(true)
            epubParser
                .parseEpubElements(absoluteUrl)
                .toTextualElementStringList()
                .also { contents ->
                    viewBookViewModel.updateContents(contents)
                }
            viewBookViewModel.setLoading(false)
        }
    }
}
