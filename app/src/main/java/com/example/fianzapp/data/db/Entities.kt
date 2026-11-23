package com.example.fianzapp.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * A4: the tables of the app. Dates are stored as text like 2026-11-01, because Room does not
 * store a LocalDate on its own. Times are epoch milliseconds.
 */

/** Moving in or moving out. Room stores it as its name (MOVE_IN or MOVE_OUT). */
enum class Phase { MOVE_IN, MOVE_OUT }

@Entity(tableName = "lease")
data class LeaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val address: String,
    val landlordName: String,
    /** Text like 2026-11-01. */
    val moveInDate: String,
    /** In cents, so 750,50 € is 75050 and there are no rounding errors. */
    val depositCents: Long,
    /** Text like 2027-06-30. Null until the tenant hands back the keys. */
    val keysReturnedDate: String? = null
)

/** A room of the flat, such as "Bedroom 2", of one type from the checklist file (A5). */
@Entity(
    tableName = "rooms",
    foreignKeys = [
        ForeignKey(
            entity = LeaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["leaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("leaseId")]
)
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leaseId: Long,
    val name: String,
    /** The `id` of a room type in the checklist file, such as `kitchen`. */
    val typeId: String
)

/**
 * One row per photo. The file lives in filesDir/photos/<roomId>/ and only its path is kept here.
 *
 * - A photo of a checklist item has [itemId] set.
 * - A photo of damage that is not on the checklist (something broken) has [itemId] null and a [note].
 *
 * Deleting a room deletes its photo rows (CASCADE). The files on disk are not deleted by Room:
 * the code that deletes a room must also delete its folder.
 */
@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roomId")]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    /** The `id` of the item in the checklist file. Null for a damage photo. */
    val itemId: Int?,
    val phase: Phase,
    /** Absolute path of the file, under filesDir/photos/. */
    val path: String,
    /** When it was taken, in epoch milliseconds. */
    val takenAt: Long,
    /** A9: where it was taken. Null when there was no position. */
    val latitude: Double? = null,
    val longitude: Double? = null,
    /** A10: labels above the threshold, as "Sink:0.91;Tile:0.80". Null until labelled. */
    val labels: String? = null,
    /** What is broken or damaged. Used by damage photos, optional for item photos. */
    val note: String? = null
)

/** The move-in or move-out report of a lease, with the two signatures as encodeSignature text. */
@Entity(
    tableName = "reports",
    primaryKeys = ["leaseId", "phase"],
    foreignKeys = [
        ForeignKey(
            entity = LeaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["leaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReportEntity(
    val leaseId: Long,
    val phase: Phase,
    val tenantSignature: String = "",
    val landlordSignature: String = "",
    /** When both signed, in epoch milliseconds. Null until then. */
    val signedAt: Long? = null
)
