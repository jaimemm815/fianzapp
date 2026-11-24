package com.example.fianzapp.data.remote

import android.content.Context
import com.google.firebase.FirebaseApp

/*
 * A6: where the reports live in Firestore.
 *
 *   leases/{FIRESTORE_LEASE_ID}                 created by hand in the console, with the fields
 *                                               tenantUid and landlordUid (S9 Guide, section 12.8)
 *   leases/{FIRESTORE_LEASE_ID}/reports/{phase} one document per report: MOVE_IN and MOVE_OUT
 *
 * Field names are text, so a typo is not caught by the compiler: they are written only here.
 */

/** The id of the lease's document, written in one place (S9 Guide, sections 8.1 and 8.2). */
const val FIRESTORE_LEASE_ID = "fianzapp-lease"

const val LEASES = "leases"
const val REPORTS = "reports"

/**
 * True once app/google-services.json is in place and the google-services plugin is on: Firebase
 * then starts by itself. Until then every Firebase screen says so instead of crashing.
 */
fun isFirebaseConfigured(context: Context): Boolean = FirebaseApp.getApps(context).isNotEmpty()

/**
 * A report as it goes to Firestore: the same class is always used to write and read it
 * (S9 Guide, section 6.5). No photos: only when and where they were taken and the comparison.
 * Every field has a default, so Firestore can build it from a document.
 */
data class ReportDocument(
    val phase: String = "",
    val address: String = "",
    val landlordName: String = "",
    val moveInDate: String = "",
    val depositCents: Long = 0,
    val keysReturnedDate: String? = null,
    val rooms: List<RoomDocument> = emptyList(),
    /** The two signatures, as the text that encodeSignature gives. */
    val tenantSignature: String = "",
    val landlordSignature: String = "",
    val signedAt: Long = 0,
    val sentAt: Long = 0
)

data class RoomDocument(
    val name: String = "",
    val typeId: String = "",
    val items: List<ItemDocument> = emptyList()
)

data class ItemDocument(
    /** Null for damage that is not on the checklist. */
    val itemId: Int? = null,
    val name: String = "",
    val note: String? = null,
    val takenAt: Long = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    /** SAME, DIFFERENT or CANNOT_TELL; null when the item has only one of its two photos. */
    val comparison: String? = null
)
