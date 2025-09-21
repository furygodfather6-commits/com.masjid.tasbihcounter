package com.masjid.tasbihcounter

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masjid.tasbihcounter.ui.screens.TasbihSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
data class DailyProgress(val date: Long, val totalCount: Int)

data class TasbihSequenceState(
    val tasbihs: List<Tasbih> = emptyList(),
    val currentTasbihIndex: Int = 0,
    val totalCount: Int = 0,
    val cycleCount: Int = 1
) {
    val currentTasbih: Tasbih?
        get() = tasbihs.getOrNull(currentTasbihIndex)

    val overallTarget: Int
        get() = tasbihs.sumOf { it.target }
}

data class AppUiState(
    val tasbihList: List<Tasbih> = emptyList(),
    val activeTasbihId: Long? = null,
    val settings: AppSettings = AppSettings(),
    val history: List<TasbihSession> = emptyList(),
    val sequenceState: TasbihSequenceState = TasbihSequenceState(),
    val progressRecords: List<DailyProgress> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val dataStore = getApplication<Application>().applicationContext.dataStore

    companion object {
        private val TASBIH_LIST_KEY = stringPreferencesKey("tasbih_list_json")
        private val SETTINGS_KEY = stringPreferencesKey("app_settings_json")
        private val PROGRESS_RECORDS_KEY = stringPreferencesKey("progress_records_json")
    }

    init {
        val defaultSequence = TasbihSequenceState(
            tasbihs = listOf(
                Tasbih(id = 1, name = "SubhanAllah", target = 33),
                Tasbih(id = 2, name = "Alhamdulillah", target = 33),
                Tasbih(id = 3, name = "Allahu Akbar", target = 34)
            )
        )
        _uiState.update { it.copy(sequenceState = defaultSequence) }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                val settings = preferences[SETTINGS_KEY]?.let { Json.decodeFromString<AppSettings>(it) } ?: AppSettings()
                val tasbihList = preferences[TASBIH_LIST_KEY]?.let { Json.decodeFromString<List<Tasbih>>(it) } ?: emptyList()
                val progressRecords = preferences[PROGRESS_RECORDS_KEY]?.let { Json.decodeFromString<List<DailyProgress>>(it) } ?: emptyList()
                _uiState.update {
                    it.copy(
                        settings = settings,
                        tasbihList = tasbihList,
                        progressRecords = progressRecords
                    )
                }
            }
        }
    }

    fun incrementSequenceCounter() {
        viewModelScope.launch {
            var currentSequence = _uiState.value.sequenceState
            if (currentSequence.totalCount >= currentSequence.overallTarget) {
                logDailyProgress(currentSequence.overallTarget)
                val newCycleCount = currentSequence.cycleCount + 1
                val resetTasbihs = currentSequence.tasbihs.map { it.copy(count = 0) }
                currentSequence = currentSequence.copy(
                    totalCount = 0,
                    currentTasbihIndex = 0,
                    cycleCount = newCycleCount,
                    tasbihs = resetTasbihs
                )
            }
            val newTotalCount = currentSequence.totalCount + 1
            var currentTasbihIndex = 0
            var countInTasbih = newTotalCount
            var cumulativeTarget = 0
            for ((index, tasbih) in currentSequence.tasbihs.withIndex()) {
                cumulativeTarget += tasbih.target
                if (newTotalCount <= cumulativeTarget) {
                    currentTasbihIndex = index
                    countInTasbih = newTotalCount - (cumulativeTarget - tasbih.target)
                    break
                }
            }
            val updatedTasbihs = currentSequence.tasbihs.mapIndexed { index, tasbih ->
                if (index == currentTasbihIndex) tasbih.copy(count = countInTasbih)
                else if (index < currentTasbihIndex) tasbih.copy(count = tasbih.target)
                else tasbih.copy(count = 0)
            }
            _uiState.update {
                it.copy(
                    sequenceState = currentSequence.copy(
                        totalCount = newTotalCount,
                        currentTasbihIndex = currentTasbihIndex,
                        tasbihs = updatedTasbihs
                    )
                )
            }
        }
    }

    private suspend fun logDailyProgress(count: Int) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val currentRecords = _uiState.value.progressRecords.toMutableList()
        val todayRecordIndex = currentRecords.indexOfFirst { it.date == today }

        if (todayRecordIndex != -1) {
            val updatedRecord = currentRecords[todayRecordIndex].let {
                it.copy(totalCount = it.totalCount + count)
            }
            currentRecords[todayRecordIndex] = updatedRecord
        } else {
            currentRecords.add(DailyProgress(date = today, totalCount = count))
        }

        _uiState.update { it.copy(progressRecords = currentRecords) }
        dataStore.edit { preferences ->
            preferences[PROGRESS_RECORDS_KEY] = Json.encodeToString(currentRecords)
        }
    }

    fun resetSequenceCounter() {
        viewModelScope.launch {
            val currentSequence = _uiState.value.sequenceState
            val resetTasbihs = currentSequence.tasbihs.map { it.copy(count = 0) }
            _uiState.update {
                it.copy(
                    sequenceState = currentSequence.copy(
                        totalCount = 0,
                        currentTasbihIndex = 0,
                        cycleCount = 1,
                        tasbihs = resetTasbihs
                    )
                )
            }
        }
    }

    private suspend fun saveSettings(settings: AppSettings) {
        dataStore.edit { it[SETTINGS_KEY] = Json.encodeToString(settings) }
    }

    fun updateTheme(theme: ThemeSetting) {
        viewModelScope.launch {
            saveSettings(_uiState.value.settings.copy(theme = theme))
        }
    }

    fun updateAdvancedTheme(advancedTheme: AdvancedTheme) {
        viewModelScope.launch {
            saveSettings(_uiState.value.settings.copy(advancedTheme = advancedTheme))
        }
    }

    fun updateVibrationMode(vibrationMode: VibrationMode) {
        viewModelScope.launch {
            saveSettings(_uiState.value.settings.copy(vibrationMode = vibrationMode))
        }
    }

    fun updateSoundMode(soundMode: SoundMode) {
        viewModelScope.launch {
            saveSettings(_uiState.value.settings.copy(soundMode = soundMode))
        }
    }

    fun updateVibrationStrength(strength: Float) {
        viewModelScope.launch {
            saveSettings(_uiState.value.settings.copy(vibrationStrength = strength))
        }
    }

    // ## NAYE FUNCTIONS ADD KIYE GAYE HAIN ##
    fun updateCountingSpeed(speed: Float) {
        viewModelScope.launch {
            saveSettings(_uiState.value.settings.copy(countingSpeed = speed))
        }
    }

    fun toggleBackgroundCounting(enabled: Boolean) {
        viewModelScope.launch {
            saveSettings(_uiState.value.settings.copy(backgroundCountingEnabled = enabled))
        }
    }

    fun updateAdditionalButtonControl(control: AdditionalButtonControl) {
        viewModelScope.launch {
            saveSettings(_uiState.value.settings.copy(additionalButtonControl = control))
        }
    }

    fun toggleFullScreenTap(enabled: Boolean) {
        viewModelScope.launch {
            saveSettings(_uiState.value.settings.copy(fullScreenTapEnabled = enabled))
        }
    }

    private suspend fun saveList(list: List<Tasbih>) {
        val jsonString = Json.encodeToString(list)
        dataStore.edit { preferences -> preferences[TASBIH_LIST_KEY] = jsonString }
    }

    fun addTasbih(name: String, target: Int) {
        viewModelScope.launch {
            val newTasbih = Tasbih(name = name, target = target)
            val newList = _uiState.value.tasbihList + newTasbih
            _uiState.update { it.copy(tasbihList = newList, activeTasbihId = newTasbih.id) }
            saveList(newList)
        }
    }

    fun selectTasbih(id: Long) {
        _uiState.update { it.copy(activeTasbihId = id) }
    }

    fun deleteTasbih(id: Long) {
        viewModelScope.launch {
            val currentList = _uiState.value.tasbihList
            val newList = currentList.filterNot { it.id == id }
            _uiState.update { it.copy(tasbihList = newList) }
            saveList(newList)
        }
    }

    fun incrementCustomTasbih(tasbihId: Long) {
        viewModelScope.launch {
            val currentList = _uiState.value.tasbihList
            val newList = currentList.map {
                if (it.id == tasbihId && it.count < it.target) {
                    it.copy(count = it.count + 1)
                } else {
                    it
                }
            }
            _uiState.update { it.copy(tasbihList = newList) }
            saveList(newList)
        }
    }

    fun resetCustomTasbih(tasbihId: Long) {
        viewModelScope.launch {
            val currentList = _uiState.value.tasbihList
            val newList = currentList.map {
                if (it.id == tasbihId) {
                    it.copy(count = 0)
                } else {
                    it
                }
            }
            _uiState.update { it.copy(tasbihList = newList) }
            saveList(newList)
        }
    }
}