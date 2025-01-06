package com.justvinny.github.noadsepubreader.logic.countdowntimer

import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ObservableCountdownTimerTest {

    private lateinit var countdownTimer: ObservableCountdownTimer
    private val observer: ICountdownObserver = mockk(relaxed = true) // Mock observer

    @Before
    fun setup() {
        countdownTimer = ObservableCountdownTimer()
        countdownTimer.addObserver(observer)
    }

    @Test
    fun `onFinish should notify all observers`() {
        // Act
        countdownTimer.onFinish()

        // Assert
        verify(exactly = 1) { observer.executeOnFinish() } // Verify observer was notified
    }

    @Test
    fun `addObserver should add observer to the list`() {
        // Arrange
        val newObserver: ICountdownObserver = mockk(relaxed = true)

        // Act
        countdownTimer.addObserver(newObserver)
        countdownTimer.onFinish()

        // Assert
        verify(exactly = 1) { newObserver.executeOnFinish() } // Verify new observer was notified
    }
}
