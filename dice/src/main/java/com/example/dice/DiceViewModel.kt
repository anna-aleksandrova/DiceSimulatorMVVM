package com.example.dice

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    private val diceModel = DiceModel()

    private val _diceValues = MutableLiveData(List(DICE_COUNT) { 1 })
    val diceValues: LiveData<List<Int>> = _diceValues

    private val _isRolling = MutableLiveData(false)
    val isRolling: LiveData<Boolean> = _isRolling

    private val _buttonText = MutableLiveData("ROLL")
    val buttonText: LiveData<String> = _buttonText

    private val activeThreads = CopyOnWriteArrayList<Thread>()

    @Volatile
    private var isCancelled = false

    private var rollJob: Job? = null

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

    fun rollOrRestartDice() {
        rollJob?.cancel()
        _isRolling.value = true
        _buttonText.value = "RESTART"

        val baseValues = _diceValues.value ?: List(DiceModel.DICE_COUNT) { 1 }

        rollJob = viewModelScope.launch {
            try {
                val finalValues = diceModel.rollAllDice(baseValues) { index, value ->
                    val updated = (_diceValues.value ?: List(DiceModel.DICE_COUNT) { 1 }).toMutableList()
                    updated[index] = value
                    _diceValues.value = updated
                }
                _diceValues.value = finalValues
                _isRolling.value = false
                _buttonText.value = "ROLL"
            } finally {
                if (rollJob?.isCancelled == true) {
                    _isRolling.value = false
                    _buttonText.value = "ROLL"
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