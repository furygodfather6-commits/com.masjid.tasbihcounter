// Path: app/src/main/java/com/masjid/tasbihcounter/AppDataStore.kt
package com.masjid.tasbihcounter

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// DataStore ka ek instance banayein jise poori app istemal kar sake
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tasbih_settings")