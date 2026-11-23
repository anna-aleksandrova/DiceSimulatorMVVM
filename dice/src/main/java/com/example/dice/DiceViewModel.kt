package com.example.dice

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import kotlin.random.Random

class DiceViewModel : ViewModel() {

    companion object {
        private const val DICE_COUNT = 5
        private const val TICK_DELAY_MS = 100L
        private val DICE_DURATIONS_MS = listOf(1500L, 1800L, 2100L, 2400L, 2700L)
    }

    private val _diceValues = MutableLiveData(List(DICE_COUNT) { 1 })
    val diceValues: LiveData<List<Int>> = _diceValues

    private val _isRolling = MutableLiveData(false)
    val isRolling: LiveData<Boolean> = _isRolling

    private val activeThreads = CopyOnWriteArrayList<Thread>()

    @Volatile
    private var isCancelled = false

    fun rollDice() {
        if (_isRolling.value == true) return

        stopActiveThreads()
        isCancelled = false
        _isRolling.value = true

        val currentValues = _diceValues.value?.toMutableList() ?: MutableList(DICE_COUNT) { 1 }
        val latch = CountDownLatch(DICE_COUNT)

        for (index in 0 until DICE_COUNT) {
            val durationMs = DICE_DURATIONS_MS[index % DICE_DURATIONS_MS.size]
            val worker = thread(start = true, name = "DiceWorker-$index") {
                try {
                    val startTime = SystemClock.elapsedRealtime()
                    while (!isCancelled && !Thread.currentThread().isInterrupted) {
                        val elapsed = SystemClock.elapsedRealtime() - startTime
                        if (elapsed >= durationMs) break

                        val rolledValue = Random.nextInt(1, 7)
                        synchronized(currentValues) {
                            currentValues[index] = rolledValue
                            _diceValues.postValue(currentValues.toList())
                        }

                        Thread.sleep(TICK_DELAY_MS)
                    }
                } catch (_: InterruptedException) {
                } finally {
                    latch.countDown()
                }
            }
            activeThreads.add(worker)
        }

        thread(start = true, name = "DiceCompletionWatcher") {
            try {
                latch.await()
            } catch (_: InterruptedException) {
            } finally {
                activeThreads.removeAll { !it.isAlive }
                if (!isCancelled) {
                    _isRolling.postValue(false)
                }
            }
        }
    }

    private fun stopActiveThreads() {
        isCancelled = true
        activeThreads.forEach { it.interrupt() }
        activeThreads.clear()
    }

    override fun onCleared() {
        super.onCleared()
        stopActiveThreads()
        _isRolling.value = false
    }
}