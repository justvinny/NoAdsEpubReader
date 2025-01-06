@file:OptIn(ExperimentalReadiumApi::class)

package com.justvinny.github.noadsepubreader.utils

import com.justvinny.github.noadsepubreader.utils.EpubParser.Companion.toTextualElementStringList
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.readium.r2.shared.ExperimentalReadiumApi
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.content.Content
import org.readium.r2.shared.publication.services.content.content
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.data.ReadError
import org.readium.r2.streamer.PublicationOpener

class EpubParserTests {
    private lateinit var assetRetriever: AssetRetriever
    private lateinit var publicationOpener: PublicationOpener
    private lateinit var epubParser: EpubParser

    @Before
    fun setup() {
        assetRetriever = mockk()
        publicationOpener = mockk()
        epubParser = EpubParser(assetRetriever, publicationOpener)

        mockkStatic(android.util.Log::class)
        every { android.util.Log.e(any(), any()) } returns 0
    }

    @Test
    fun `parseEpubElements emits empty list on assetRetriever failure`() = runTest {
        // Arrange
        val url = mockk<AbsoluteUrl>()
        val failure: Try<Asset, AssetRetriever.RetrieveUrlError> = Try.Failure(
            AssetRetriever.RetrieveUrlError.SchemeNotSupported(scheme = Url.Scheme("unsupported"), cause = null)
        )

        coEvery { assetRetriever.retrieve(url) } returns failure

        // Act
        val result = epubParser.parseEpubElements(url)

        // Assert
        assertEquals(emptyList<Content.Element>(), result)
        coVerify(exactly = 1) { assetRetriever.retrieve(url) }
        coVerify(exactly = 0) { publicationOpener.open(any(), allowUserInteraction = any()) }
        verify(exactly = 1) { android.util.Log.e(any(), any()) }
    }

    @Test
    fun `parseEpubElements emits empty list on publicationOpener failure`() = runTest {
        // Arrange
        val url = mockk<AbsoluteUrl>()
        val asset = mockk<Asset>()
        val failure: Try<Publication, PublicationOpener.OpenError> = Try.Failure(
            PublicationOpener.OpenError.Reading(ReadError.UnsupportedOperation("unsupported"))
        )

        coEvery { assetRetriever.retrieve(url) } returns Try.Success(asset)
        coEvery { publicationOpener.open(asset, allowUserInteraction = true) } returns failure

        // Act
        val result = epubParser.parseEpubElements(url)

        // Assert
        assertEquals(emptyList<Content.Element>(), result)
        coVerify(exactly = 1) { assetRetriever.retrieve(url) }
        coVerify(exactly = 1) { publicationOpener.open(any(), allowUserInteraction = any()) }
        verify(exactly = 1) { android.util.Log.e(any(), any()) }
    }

    @Test
    fun `parseEpubElements emits empty list when publication content is null`() = runTest {
        // Arrange
        val url = mockk<AbsoluteUrl>()
        val asset = mockk<Asset>()
        val publication = mockk<Publication>()

        coEvery { assetRetriever.retrieve(url) } returns Try.Success(asset)
        coEvery { publicationOpener.open(asset, allowUserInteraction = true) } returns Try.Success(publication)
        every { publication.content() } returns null

        // Act
        val result = epubParser.parseEpubElements(url)

        // Assert
        assertEquals(emptyList<Content.Element>(), result)
        coVerify(exactly = 1) { assetRetriever.retrieve(url) }
        coVerify(exactly = 1) { publicationOpener.open(any(), allowUserInteraction = any()) }
        verify(exactly = 0) { android.util.Log.e(any(), any()) }
    }

    @Test
    fun `parseEpubElements emits empty list when publication content elements is empty`() = runTest {
        // Arrange
        val url = mockk<AbsoluteUrl>()
        val asset = mockk<Asset>()
        val publication = mockk<Publication>()
        val content = mockk<Content>()

        coEvery { assetRetriever.retrieve(url) } returns Try.Success(asset)
        coEvery { publicationOpener.open(asset, allowUserInteraction = true) } returns Try.Success(publication)
        every { publication.content() } returns content
        coEvery { content.elements() } returns listOf()

        // Act
        val result = epubParser.parseEpubElements(url)

        // Assert
        assertEquals(emptyList<Content.Element>(), result)
        coVerify(exactly = 1) { assetRetriever.retrieve(url) }
        coVerify(exactly = 1) { publicationOpener.open(any(), allowUserInteraction = any()) }
        verify(exactly = 0) { android.util.Log.e(any(), any()) }
    }

    @Test
    fun `parseEpubElements emits non-empty list when publication content elements is not empty`() = runTest {
        // Arrange
        val url = mockk<AbsoluteUrl>()
        val asset = mockk<Asset>()
        val publication = mockk<Publication>()
        val content = mockk<Content>()
        val expectedElements = listOf(mockk<Content.TextualElement>(), mockk<Content.ImageElement>())

        coEvery { assetRetriever.retrieve(url) } returns Try.Success(asset)
        coEvery { publicationOpener.open(asset, allowUserInteraction = true) } returns Try.Success(publication)
        every { publication.content() } returns content
        coEvery { content.elements() } returns expectedElements

        // Act
        val result = epubParser.parseEpubElements(url)

        // Assert
        assertEquals(expectedElements, result)
        coVerify(exactly = 1) { assetRetriever.retrieve(url) }
        coVerify(exactly = 1) { publicationOpener.open(any(), allowUserInteraction = any()) }
        verify(exactly = 0) { android.util.Log.e(any(), any()) }
    }

    @Test
    fun `toTextualElementStringList returns empty list when given a non-empty list of non-textual elements`() {
        // Arrange
        val elements = listOf(mockk<Content.EmbeddedElement>(), mockk<Content.EmbeddedElement>())

        // Act
        val result = elements.toTextualElementStringList()

        // Assert
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `toTextualElementStringList returns string list with 4 elements when given a list of 4 valid elements and 1 invalid element`() {
        // Arrange
        val expected = listOf(
            "Some textualElement text",
            "Some imageElement text",
            "Some videoElement text",
            "Some audioElement text",
        )

        val textualElement = mockk<Content.TextualElement>()
        val imageElement = mockk<Content.ImageElement>()
        val videoElement = mockk<Content.VideoElement>()
        val audioElement = mockk<Content.AudioElement>()

        every { textualElement.text } returns expected[0]
        every { imageElement.text } returns expected[1]
        every { videoElement.text } returns expected[2]
        every { audioElement.text } returns expected[3]

        val elements = listOf(
            mockk<Content.EmbeddedElement>(),
            textualElement,
            imageElement,
            videoElement,
            audioElement,
        )

        // Act
        val result = elements.toTextualElementStringList()

        // Assert
        assertEquals(expected, result)
        assertEquals(expected.size, result.size)

        verifySequence {
            textualElement.text
            imageElement.text
            videoElement.text
            audioElement.text
        }
    }
}