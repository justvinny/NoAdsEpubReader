package com.justvinny.github.noadsepubreader.ui.viewbook

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.justvinny.github.noadsepubreader.logic.CACHED_DIR_NAME
import com.justvinny.github.noadsepubreader.logic.Constants
import java.io.File

private const val TAG = "ViewBookScreenV2"

@Composable
fun ViewBookScreenV2(
    modifier: Modifier,
    viewModelV2: ViewBookViewModelV2,
) {
    val state by viewModelV2.state.collectAsState()
    val bgColor by rememberUpdatedState(MaterialTheme.colorScheme.background.toCssRgb())
    val fontColor by rememberUpdatedState(MaterialTheme.colorScheme.onBackground.toCssRgb())

    AndroidView(factory = {
        WebView(it).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }, update = { webView ->
        // Should not be an issue enabling JS as we just need it for internal scrolling for local HTML files
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = false
        webView.webViewClient = object : WebViewClient() {
            var pendingAnchor: String? = null

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()

                if (url.startsWith("file://")) { // This is an internal EPUB link
                    val splitUrl = url.split("#")
                    pendingAnchor = splitUrl.getOrNull(1)

                    webView.loadHtmlFromFile(
                        cachedDirPath = state.cachedDirPath,
                        url = splitUrl.first(),
                        bgColor = bgColor,
                        fontColor = fontColor,
                    )
                } else { // External links should be handled outside our app by an actual browser.
                    try {
                        view?.context?.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(view?.context, "No browser found to open the link.", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, e.message, e)
                    }
                }

                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pendingAnchor?.let { anchor ->
                    view?.evaluateJavascript(
                        "document.getElementById('$anchor')?.scrollIntoView();",
                        null
                    )
                    pendingAnchor = null
                }
            }
        }

        webView.loadHtmlFromFile(
            cachedDirPath = state.cachedDirPath,
            url = state.current?.href.orEmpty(),
            bgColor = bgColor,
            fontColor = fontColor,
        )
    }, modifier = modifier)
}

private fun WebView.loadHtmlFromFile(
    cachedDirPath: String,
    url: String,
    bgColor: String,
    fontColor: String,
) {
    if (url.isEmpty()) {
        return
    }

    val cachedFilePath = if (url.startsWith("file://")) {
        url.replaceFirst("file://", "")
    } else {
        "$cachedDirPath/$CACHED_DIR_NAME/$url"
    }

    val cachedFile = File(cachedFilePath)
    loadDataWithBaseURL(
        "file://${cachedFile.parentFile?.absolutePath}/",
        injectResponsiveStyle(cachedFile.readText(), bgColor, fontColor),
        Constants.HTML_MIME_TYPE,
        Constants.UTF_8_ENCODING,
        null,
    )
}

private fun Color.toCssRgb(): String {
    val r = (red.coerceIn(0f, 1f) * 255).toInt()
    val g = (green.coerceIn(0f, 1f) * 255).toInt()
    val b = (blue.coerceIn(0f, 1f) * 255).toInt()
    return "rgb($r, $g, $b)"
}

private fun injectResponsiveStyle(html: String, bgColor: String, onBgColor: String): String {
    val style = """
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            body {
                max-width: 100%;
                overflow-x: hidden;
                word-wrap: break-word;
                background-color: $bgColor;
                color: $onBgColor;
            }
            img {
                max-width: 100%;
                height: auto;
                display: block;
            }
        </style>
    """.trimIndent()

    return if ("<head>" in html) {
        html.replaceFirst("<head>", "<head>$style")
    } else {
        html.replaceFirst("<html>", "<html><head>$style</head>")
    }
}
