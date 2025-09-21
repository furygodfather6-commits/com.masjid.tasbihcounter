package com.masjid.tasbihcounter

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masjid.tasbihcounter.ui.screens.TasbihSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date

data class AppUiState(
    val tasbihList: List<Tasbih> = emptyList(),
    val activeTasbihId: Long? = null,
    val settings: AppSettings = AppSettings(),
    val history: List<TasbihSession> = emptyList()
)

@OptIn(InternalSerializationApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val dataStore = getApplication<Application>().applicationContext.dataStore

    companion object {
        private val TASBIH_LIST_KEY = stringPreferencesKey("tasbih_list_json")
        private val SETTINGS_KEY = stringPreferencesKey("app_settings_json")
    }

    init {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                val jsonString = preferences[TASBIH_LIST_KEY]
                if (jsonString.isNullOrEmpty()) {
                    listOf(
                        Tasbih(name = "SubhanAllah", target = 33),
                        Tasbih(name = "Alhamdulillah", target = 33),
                        Tasbih(name = "Allahu Akbar", target = 34)
                    )
                } else {
                    Json.decodeFromString<List<Tasbih>>(jsonString)
                }
            }.distinctUntilChanged().collect { tasbihList ->
                _uiState.update { currentState ->
                    val newActiveId = if (currentState.activeTasbihId == null || tasbihList.none { it.id == currentState.activeTasbihId }) {
                        tasbihList.firstOrNull()?.id
                    } else {
                        currentState.activeTasbihId
                    }
                    currentState.copy(
                        tasbihList = tasbihList,
                        activeTasbihId = newActiveId
                    )
                }
            }
        }

        viewModelScope.launch {
            dataStore.data.map { preferences ->
                val jsonString = preferences[SETTINGS_KEY]
                if (jsonString != null) Json.decodeFromString<AppSettings>(jsonString) else AppSettings()
            }.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
    }

    private suspend fun saveSettings(settings: AppSettings) {
        val jsonString = Json.encodeToString(settings)
        dataStore.edit { preferences ->
            preferences[SETTINGS_KEY] = jsonString
        }
    }

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

    fun updateAdvancedTheme(theme: AdvancedTheme) {
        viewModelScope.launch {
            val newSettings = _uiState.value.settings.copy(advancedTheme = theme)
            saveSettings(newSettings)
        }
    }

    private suspend fun saveList(list: List<Tasbih>) {
        val jsonString = Json.encodeToString(list)
        dataStore.edit { preferences ->
            preferences[TASBIH_LIST_KEY] = jsonString
        }
    }

    fun addTasbih(name: String, target: Int) {
        viewModelScope.launch {
            val newList = _uiState.value.tasbihList + Tasbih(name = name, target = target)
            _uiState.update { it.copy(tasbihList = newList, activeTasbihId = newList.last().id) }
            saveList(newList)
        }
    }

    fun selectTasbih(id: Long) {
        _uiState.update { it.copy(activeTasbihId = id) }
    }

    fun incrementActiveTasbih() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val activeId = currentState.activeTasbihId ?: return@launch
            val tasbihList = currentState.tasbihList
            val activeTasbihIndex = tasbihList.indexOfFirst { it.id == activeId }
            if (activeTasbihIndex == -1) return@launch

            val activeTasbih = tasbihList[activeTasbihIndex]

            if (activeTasbih.count >= activeTasbih.target) return@launch

            val newCount = activeTasbih.count + 1
            val newList = tasbihList.toMutableList()
            newList[activeTasbihIndex] = activeTasbih.copy(count = newCount)

            var nextActiveId = activeId
            if (newCount >= activeTasbih.target && (activeTasbih.name == "SubhanAllah" || activeTasbih.name == "Alhamdulillah")) {
                if (activeTasbihIndex < tasbihList.size - 1) {
                    nextActiveId = tasbihList[activeTasbihIndex + 1].id
                }
            }

            _uiState.value = currentState.copy(tasbihList = newList, activeTasbihId = nextActiveId)
            saveList(newList)
        }
    }


    fun resetActiveTasbih() {
        viewModelScope.launch {
            val activeId = _uiState.value.activeTasbihId ?: return@launch
            val newList = _uiState.value.tasbihList.map {
                if (it.id == activeId) it.copy(count = 0) else it
            }
            _uiState.update { it.copy(tasbihList = newList) }
            saveList(newList)
        }
    }

    fun deleteTasbih(id: Long) {
        viewModelScope.launch {
            val currentList = _uiState.value.tasbihList
            val newList = currentList.filterNot { it.id == id }
            var newActiveId = _uiState.value.activeTasbihId

            if (newActiveId == id) {
                newActiveId = newList.firstOrNull()?.id
            }

            _uiState.update { it.copy(tasbihList = newList, activeTasbihId = newActiveId) }
            saveList(newList)
        }
    }
}