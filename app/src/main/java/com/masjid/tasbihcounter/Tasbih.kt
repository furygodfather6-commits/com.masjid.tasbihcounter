// Path: app/src/main/java/com/masjid/tasbihcounter/Tasbih.kt
package com.masjid.tasbihcounter

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable // Isse hum object ko save kar payenge
data class Tasbih(
    val id: Long = System.currentTimeMillis(), // Har tasbih ke liye ek anokhi ID
    val name: String,
    val count: Int = 0,
    val target: Int = 100
)