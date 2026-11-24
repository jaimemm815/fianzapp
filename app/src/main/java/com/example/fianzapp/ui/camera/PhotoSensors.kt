package com.example.fianzapp.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.core.content.ContextCompat
import com.example.fianzapp.domain.LABEL_THRESHOLD
import com.example.fianzapp.domain.PhotoLabel
import com.example.fianzapp.domain.encodeLabels
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume

/** A9: true when the person granted the approximate or the precise location. */
fun hasLocationPermission(context: Context): Boolean =
    listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

/**
 * A9: where the phone is now, or null: no permission, location turned off, or no position in time.
 * getCurrentLocation suits a photo better than lastLocation, which can be of any age.
 */
@SuppressLint("MissingPermission") // checked just above the call
suspend fun currentLocation(context: Context): Location? {
    if (!hasLocationPermission(context)) return null
    val client = LocationServices.getFusedLocationProviderClient(context)
    return withTimeoutOrNull(10_000) {
        suspendCancellableCoroutine { continuation ->
            val token = CancellationTokenSource()
            continuation.invokeOnCancellation { token.cancel() }
            try {
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.token)
                    .addOnSuccessListener { location: Location? -> continuation.resume(location) }
                    .addOnFailureListener { continuation.resume(null) }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }
        }
    }
}

/**
 * A10: labels a saved photo with ML Kit's bundled model, keeping those above [LABEL_THRESHOLD].
 * Returns "" when nothing passes the threshold, and null when the photo cannot be read or the
 * labeler fails (the three exits of S12 Guide, section 4.10).
 */
suspend fun labelPhoto(context: Context, file: File): String? {
    val image = try {
        InputImage.fromFilePath(context, Uri.fromFile(file))
    } catch (e: IOException) {
        return null
    }
    val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder().setConfidenceThreshold(LABEL_THRESHOLD).build()
    )
    return try {
        withTimeoutOrNull(20_000) {
            suspendCancellableCoroutine { continuation ->
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        continuation.resume(encodeLabels(labels.map { PhotoLabel(it.text, it.confidence) }))
                    }
                    .addOnFailureListener { continuation.resume(null) }
            }
        }
    } finally {
        labeler.close()
    }
}
