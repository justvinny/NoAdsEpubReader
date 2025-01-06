package com.justvinny.github.noadsepubreader.logic

import android.util.Log
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.content.Content
import org.readium.r2.shared.publication.services.content.content
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.getOrElse
import org.readium.r2.streamer.PublicationOpener

private const val TAG = "EpubParser"

@OptIn(ExperimentalReadiumApi::class)
class EpubParser(
    private val assetRetriever: AssetRetriever,
    private val publicationOpener: PublicationOpener,
) {
    suspend fun parseEpubElements(absoluteUrl: AbsoluteUrl): List<Content.Element> {
        val asset = assetRetriever.retrieve(absoluteUrl).getOrElse { exception ->
            Log.e(TAG, "getEpubContent assetRetriever message: ${exception.message} | cause: ${exception.cause}")
        } as? Asset ?: return listOf()

        val publication = publicationOpener.open(asset, allowUserInteraction = true).getOrElse { exception ->
            Log.e(TAG, "getEpubContent publicationOpener message: ${exception.message} | cause: ${exception.cause}")
        } as? Publication ?: return listOf()

        return publication.content()
            ?.elements() ?: listOf()
    }

    companion object {
        fun List<Content.Element>.toTextualElementStringList(): List<String> {
            return this
                .filterIsInstance<Content.TextualElement>()
                .mapNotNull { it.text }
        }
    }
}
