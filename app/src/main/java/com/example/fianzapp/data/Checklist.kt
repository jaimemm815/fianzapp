package com.example.fianzapp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The one Json object of the app. The checklist file brings a field we do not read (`note`),
 * and the plain Json stops on it. Use this object everywhere JSON is read.
 */
val lenientJson = Json { ignoreUnknownKeys = true }

@Serializable
data class Checklist(
    val roomTypes: List<RoomType>
)

@Serializable
data class RoomType(
    /** Short code that never repeats, such as `kitchen`. */
    val id: String,
    val name: String,
    /** Can be empty: `storage` has no items. */
    val items: List<ChecklistItem>
)

@Serializable
data class ChecklistItem(
    /** Never repeats anywhere in the file, not even across room types. */
    val id: Int,
    val name: String,
    /** Can be null: item 30, the floor of the balcony, has no hint. */
    val hint: String? = null
)
