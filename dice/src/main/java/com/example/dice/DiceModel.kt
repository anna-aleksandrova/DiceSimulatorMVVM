package com.example.dice

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.random.Random

class DiceModel {

    companion object {
        const val DICE_COUNT = 5
        private const val TICK_DELAY_MS = 100L
        private val DURATION_RANGE_MS = 1500..3000
    }

    suspend fun rollSingleDie(
        durationMs: Long,
        onTick: (Int) -> Unit
    ): Int {
        var elapsed = 0L
        var lastValue = Random.nextInt(1, 7)
        onTick(lastValue)

        while (elapsed < durationMs) {
            delay(TICK_DELAY_MS)
            elapsed += TICK_DELAY_MS
            lastValue = Random.nextInt(1, 7)
            onTick(lastValue)
        }

        return lastValue
    }

    suspend fun rollAllDice(
        initialValues: List<Int>,
        onDieTick: (index: Int, value: Int) -> Unit
    ): List<Int> = coroutineScope {
        val current = initialValues.toMutableList()
        val tasks = (0 until DICE_COUNT).map { index ->
            val durationMs = Random.nextInt(DURATION_RANGE_MS.first, DURATION_RANGE_MS.last + 1).toLong()
            async {
                rollSingleDie(durationMs) { value ->
                    current[index] = value
                    onDieTick(index, value)
                }
            }
        }
        tasks.awaitAll()
    }
}