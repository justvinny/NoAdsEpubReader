package com.justvinny.github.noadsepubreader.ui.viewbook

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
    val onBgColor by rememberUpdatedState(MaterialTheme.colorScheme.onBackground.toCssRgb())

    AndroidView(factory = {
        WebView(it).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }, update = { webView ->
        webView.settings.allowFileAccess = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()

                if (url.startsWith("file://")) { // This is an internal EPUB link
                    view?.loadUrl(url)
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
        }

        state.current?.href?.also { href ->
            val cachedFile = File(state.cachedDirPath, "$CACHED_DIR_NAME/$href")
            webView.loadDataWithBaseURL(
                "file://${cachedFile.parentFile?.absolutePath}/",
                injectResponsiveStyle(cachedFile.readText(), bgColor, onBgColor),
                Constants.HTML_MIME_TYPE,
                Constants.UTF_8_ENCODING,
                null,
            )
        }
    }, modifier = modifier)
}

fun Color.toCssRgb(): String {
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
