package com.example.fianzapp.ui.room

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** Why a photo could not be shared, so the screen can say it. */
enum class ShareProblem { FILE_MISSING, NO_APP }

/**
 * A2: sends one saved photo to another app with an implicit intent.
 *
 * The file lives in filesDir/photos/, which has no address another app can open, so it goes out as
 * a content:// address from the skeleton's FileProvider, with the read permission granted.
 * The chooser means a phone with no suitable app never crashes (S5 Guide, section 11.5).
 *
 * Returns null when the chooser opened, or the problem otherwise.
 */
fun sharePhoto(context: Context, path: String, chooserTitle: String): ShareProblem? {
    val file = File(path)
    if (!file.exists()) return ShareProblem.FILE_MISSING

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        // Without this flag the other app opens but cannot read the picture.
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // Lets the permission travel through the chooser to the app the person picks.
        clipData = ClipData.newRawUri(null, uri)
    }
    return try {
        context.startActivity(Intent.createChooser(send, chooserTitle))
        null
    } catch (e: ActivityNotFoundException) {
        ShareProblem.NO_APP
    }
}
