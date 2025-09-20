// Path: app/src/main/java/com/masjid/tasbihcounter/MainViewModel.kt
package com.masjid.tasbihcounter

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// UI State mein ab settings bhi hongi
data class AppUiState(
    val tasbihList: List<Tasbih> = emptyList(),
    val activeTasbihId: Long? = null,
    val settings: AppSettings = AppSettings() // Default settings
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val dataStore = getApplication<Application>().applicationContext.dataStore

    companion object {
        private val TASBIH_LIST_KEY = stringPreferencesKey("tasbih_list_json")
        private val SETTINGS_KEY = stringPreferencesKey("app_settings_json")
    }

    init {
        // Tasbih list ko load karein
        dataStore.data.map { preferences ->
            val jsonString = preferences[TASBIH_LIST_KEY]
            if (jsonString != null) Json.decodeFromString<List<Tasbih>>(jsonString)
            else listOf(Tasbih(name = "SubhanAllah", target = 33))
        }.onEach { tasbihList ->
            _uiState.update {
                it.copy(
                    tasbihList = tasbihList,
                    activeTasbihId = it.activeTasbihId ?: tasbihList.firstOrNull()?.id
                )
            }
        }.launchIn(viewModelScope)

        // Settings ko load karein
        dataStore.data.map { preferences ->
            val jsonString = preferences[SETTINGS_KEY]
            if (jsonString != null) Json.decodeFromString<AppSettings>(jsonString)
            else AppSettings() // Default settings
        }.onEach { settings ->
            _uiState.update { it.copy(settings = settings) }
        }.launchIn(viewModelScope)
    }

    private suspend fun saveSettings(settings: AppSettings) {
        val jsonString = Json.encodeToString(settings)
        dataStore.edit { preferences ->
            preferences[SETTINGS_KEY] = jsonString
        }
    }

    // Settings update karne ke liye functions
    fun updateTheme(theme: ThemeSetting) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(theme = theme)
            saveSettings(newSettings)
        }
    }

    fun toggleVibration(isOn: Boolean) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(isVibrationOn = isOn)
            saveSettings(newSettings)
        }
    }

    // Baaki ke functions waise hi rahenge...
    private suspend fun saveList(list: List<Tasbih>) {
        val jsonString = Json.encodeToString(list)
        dataStore.edit { preferences ->
            preferences[TASBIH_LIST_KEY] = jsonString
        }
    }
    fun addTasbih(name: String, target: Int) {
        viewModelScope.launch {
            val newList = _uiState.value.tasbihList + Tasbih(name = name, target = target)
            saveList(newList)
            _uiState.update { it.copy(activeTasbihId = newList.last().id) }
        }
    }
    fun selectTasbih(id: Long) {
        _uiState.update { it.copy(activeTasbihId = id) }
    }
    fun incrementActiveTasbih() {
        viewModelScope.launch {
            val activeId = _uiState.value.activeTasbihId ?: return@launch
            val newList = _uiState.value.tasbihList.map { tasbih ->
                if (tasbih.id == activeId) {
                    val newCount = if (tasbih.count + 1 >= tasbih.target) 0 else tasbih.count + 1
                    tasbih.copy(count = newCount)
                } else {
                    tasbih
                }
            }
            saveList(newList)
        }
    }
    fun resetActiveTasbih() {
        viewModelScope.launch {
            val activeId = _uiState.value.activeTasbihId ?: return@launch
            val newList = _uiState.value.tasbihList.map {
                if (it.id == activeId) it.copy(count = 0) else it
            }
            saveList(newList)
        }
    }
    fun deleteTasbih(id: Long) {
        viewModelScope.launch {
            val newList = _uiState.value.tasbihList.filterNot { it.id == id }
            saveList(newList)
            if (_uiState.value.activeTasbihId == id) {
                _uiState.update { it.copy(activeTasbihId = newList.firstOrNull()?.id) }
            }
        }
    }
}