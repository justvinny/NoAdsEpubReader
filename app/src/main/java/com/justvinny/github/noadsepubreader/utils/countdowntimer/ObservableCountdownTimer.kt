package com.justvinny.github.noadsepubreader.utils.countdowntimer

import android.os.CountDownTimer
import com.justvinny.github.noadsepubreader.Constants

class ObservableCountdownTimer(
    millisInFuture: Long = Constants.DEFAULT_TIMER_MAX_MS,
    countDownInterval: Long = Constants.DEFAULT_TIMER_INTERVAL_MS,
): CountDownTimer(millisInFuture, countDownInterval) {
    private val observers: MutableList<ICountdownObserver> = mutableListOf()

    override fun onTick(millisUntilFinished: Long) {}

    override fun onFinish() {
        observers.forEach { it.executeOnFinish() }
    }

    fun addObserver(observer: ICountdownObserver) {
        observers.add(observer)
    }
}
