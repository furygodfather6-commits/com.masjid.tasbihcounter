// Path: app/src/main/java/com/masjid/tasbihcounter/MainViewModel.kt
package com.masjid.tasbihcounter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TasbihUiState(
    val count: Int = 0,
    val target: Int = 33
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TasbihUiState())
    val uiState: StateFlow<TasbihUiState> = _uiState.asStateFlow()

    fun incrementCount() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val newCount = if (currentState.count + 1 > currentState.target) {
                    0
                } else {
                    currentState.count + 1
                }
                currentState.copy(count = newCount)
            }
        }
    }

    fun resetCount() {
        viewModelScope.launch {
            _uiState.update { it.copy(count = 0) }
        }
    }

    fun setTarget(newTarget: Int) {
        if (newTarget > 0) {
            viewModelScope.launch {
                _uiState.update { it.copy(target = newTarget, count = 0) }
            }
        }
    }
}