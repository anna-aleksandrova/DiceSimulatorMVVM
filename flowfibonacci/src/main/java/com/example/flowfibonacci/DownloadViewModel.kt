package com.example.flowfibonacci

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DownloadUiState(
    val progress: Int = 0,
    val status: String = "Очікування"
)

class DownloadViewModel : ViewModel() {

    private val fileRepository = FileRepository()

    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    private var downloadJob: Job? = null

    fun startDownload() {
        downloadJob?.cancel()
        _uiState.value = DownloadUiState(progress = 0, status = "Завантаження: 0%")

        downloadJob = viewModelScope.launch {
            var wasCancelled = false
            fileRepository.downloadFile()
                .onCompletion { cause ->
                    if (cause != null) {
                        wasCancelled = true
                    }
                }
                .collect { value ->
                    _uiState.update {
                        it.copy(
                            progress = value,
                            status = if (value == 100) {
                                "Файл успішно скачано!"
                            } else {
                                "Завантаження: $value%"
                            }
                        )
                    }
                }

            if (wasCancelled) {
                _uiState.update {
                    it.copy(status = "Завантаження скасовано")
                }
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        _uiState.update {
            it.copy(status = "Завантаження скасовано")
        }
    }
}