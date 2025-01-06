package com.justvinny.github.noadsepubreader.viewbook

import app.cash.turbine.test
import com.justvinny.github.noadsepubreader.utils.countdowntimer.ObservableCountdownTimer
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ViewBookViewModelTests {

    private lateinit var countdownTimer: ObservableCountdownTimer
    private lateinit var viewModel: ViewBookViewModel

    @Before
    fun setup() {
        countdownTimer = mockk(relaxed = true, relaxUnitFun = true)
        viewModel = ViewBookViewModel(countdownTimer)
    }

    @Test
    fun `updateContents emits correct contents`() = runTest {
        // Arrange
        val expectedList = listOf("Test 1", "Some other text", "Some test")

        // Act
        viewModel.updateContents(expectedList)

        // Assert
        viewModel.state.test {
            assertEquals(emptyList<List<String>>(), awaitItem().contents) // Initial State
            assertEquals(expectedList, awaitItem().contents)
            expectNoEvents()
        }
    }

    @Test
    fun `setLoading emits correct isLoading state`() = runTest {
        // Arrange
        val expected = true

        // Act
        viewModel.setLoading(expected)

        // Assert
        viewModel.state.test {
            assertEquals(false, awaitItem().isLoading) // Initial State
            assertEquals(expected, awaitItem().isLoading)
            expectNoEvents()
        }
    }

    @Test
    fun `updateScrollPosition emits correct scrollPosition state`() = runTest {
        // Arrange
        val expected = 50

        // Act
        viewModel.updateScrollPosition(expected)

        // Assert
        viewModel.state.test {
            assertEquals(0, awaitItem().lazyListState.firstVisibleItemIndex) // Initial State
            assertEquals(expected, awaitItem().lazyListState.firstVisibleItemIndex)
            expectNoEvents()
        }
    }

    @Test
    fun `arrowUp emits the same state given matchedResultsIndices is empty`() = runTest {
        // Arrange
        val expected = 0

        // Act
        viewModel.arrowUp()

        // Assert
        viewModel.state.test {
            assertEquals(expected, awaitItem().matchedResultIndex) // Initial State
            assertEquals(expected, awaitItem().matchedResultIndex) // Same as initial state
            expectNoEvents()
        }
    }

    @Test
    fun `arrowDown emits the same state given matchedResultsIndices is empty`() = runTest {
        // Arrange
        val expected = 0

        // Act
        viewModel.arrowDown()

        // Assert
        viewModel.state.test {
            assertEquals(expected, awaitItem().matchedResultIndex) // Initial State
            assertEquals(expected, awaitItem().matchedResultIndex) // Same as initial state
            expectNoEvents()
        }
    }

    @Test
    fun `search emits correct states`() = runTest {
        // Arrange
        val contents = listOf("Expected", "Testing some words expe", "To search expected")
        val firstSearchTerm = "expe"
        val finalSearchTerm = "expected"

        // Act & Assert
        viewModel.state.test {
            viewModel.updateContents(contents)
            awaitItem() // Discard InitialState
            assertEquals(contents, awaitItem().contents)


            viewModel.search(firstSearchTerm)
            awaitItem().also {
                assertEquals(firstSearchTerm, it.searchTerm)
                assertEquals(0, it.matchedResultsIndices.size)
            }

            viewModel.search(finalSearchTerm)
            awaitItem().also {
                assertEquals(finalSearchTerm, it.searchTerm)
                assertEquals(0, it.matchedResultsIndices.size)
            }

            expectNoEvents()
        }

        verify(exactly = 2) { countdownTimer.cancel() }
        verify(exactly = 2) { countdownTimer.start() }
    }

    @Test
    fun `executeOnFinish calls updateSearchResults given search matches and emits correct matched indices`() = runTest {
        // Arrange
        val contents = listOf("Expected", "Testing some words expe", "To search expected")
        val searchTerm = "expected"
        val expectedMatchedResultsIndices = listOf(0, 2)

        // Act & Assert
        viewModel.state.test {
            viewModel.updateContents(contents)
            awaitItem() // Discard InitialState
            assertEquals(contents, awaitItem().contents)

            viewModel.search(searchTerm)
            awaitItem().also {
                assertEquals(searchTerm, it.searchTerm)
                assertEquals(0, it.matchedResultsIndices.size)
            }

            viewModel.executeOnFinish()
            awaitItem().also {
                assertEquals(expectedMatchedResultsIndices, it.matchedResultsIndices)
            }

            expectNoEvents()
        }

        verify(exactly = 1) { countdownTimer.cancel() }
        verify(exactly = 1) { countdownTimer.start() }
    }

    @Test
    fun `executeOnFinish calls updateSearchResults given search does not match does not update state after`() = runTest {
        // Arrange
        val contents = listOf("Expected", "Testing some words expe", "To search expected")
        val searchTerm = "no match"

        // Act & Assert
        viewModel.state.test {
            viewModel.updateContents(contents)
            awaitItem() // Discard InitialState
            assertEquals(contents, awaitItem().contents)

            viewModel.search(searchTerm)
            awaitItem().also {
                assertEquals(searchTerm, it.searchTerm)
                assertEquals(0, it.matchedResultsIndices.size)
            }

            viewModel.executeOnFinish() // State should stay the same so no need to assert

            expectNoEvents() // Would have thrown an exception if state didn't stay the same
        }

        verify(exactly = 1) { countdownTimer.cancel() }
        verify(exactly = 1) { countdownTimer.start() }
    }

    @Test
    fun `executeOnFinish calls updateSearchResults given search blank does not update the state`() = runTest {
        // Arrange
        val contents = listOf("Expected", "Testing some words expe", "To search expected")

        // Act & Assert
        viewModel.state.test {
            viewModel.updateContents(contents)
            awaitItem() // Discard InitialState
            assertEquals(contents, awaitItem().contents)

            viewModel.executeOnFinish() // State should stay the same as search is blank so no need to assert

            expectNoEvents() // Would have thrown an exception if state didn't stay the same
        }
    }
}