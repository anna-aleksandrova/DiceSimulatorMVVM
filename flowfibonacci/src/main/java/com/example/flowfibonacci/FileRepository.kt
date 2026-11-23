package com.example.flowfibonacci

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FileRepository {

    fun downloadFile(): Flow<Int> = flow {
        var progress = 0
        while (progress <= 100) {
            emit(progress)
            if (progress < 100) {
                delay(150L)
            }
            progress += 5
        }
    }.flowOn(Dispatchers.IO)
}
