package com.example.fianzapp.ui.camera

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.fianzapp.data.db.FianzappDatabase
import com.example.fianzapp.data.db.Phase
import com.example.fianzapp.data.db.PhotoEntity
import com.example.fianzapp.ui.CameraRoute
import kotlinx.coroutines.launch
import java.io.File

class CameraViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val route = savedStateHandle.toRoute<CameraRoute>()
    private val photoDao = FianzappDatabase.getInstance(application).photoDao()

    /** -1 in the route means a damage photo, which has no checklist item. */
    val itemId: Int? = route.itemId.takeIf { it >= 0 }
    val phase: Phase = Phase.valueOf(route.phase)
    val note: String? = route.note.takeIf { it.isNotBlank() }

    /**
     * A new, unique file for the next photo: filesDir/photos/<roomId>/<item or damage>_<phase>_<time>.jpg.
     * The folder is created here, before CameraX writes into it.
     */
    fun newPhotoFile(): File {
        val folder = File(getApplication<Application>().filesDir, "photos/${route.roomId}")
        folder.mkdirs()
        val what = itemId?.let { "item$it" } ?: "damage"
        return File(folder, "${what}_${phase.name}_${System.currentTimeMillis()}.jpg")
    }

    /** Adds the row of a photo that CameraX has already saved, then calls [onSaved]. */
    fun savePhoto(file: File, onSaved: () -> Unit) {
        viewModelScope.launch {
            photoDao.insert(
                PhotoEntity(
                    roomId = route.roomId,
                    itemId = itemId,
                    phase = phase,
                    path = file.absolutePath,
                    takenAt = System.currentTimeMillis(),
                    note = note
                )
            )
            onSaved()
        }
    }
}
