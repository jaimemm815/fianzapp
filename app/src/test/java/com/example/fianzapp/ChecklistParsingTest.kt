package com.example.fianzapp

import com.example.fianzapp.data.parseChecklist
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Parses the real recorded file, with its awkward cases. */
class ChecklistParsingTest {

    // Unit tests run with the module folder (app/) as working directory.
    private val checklist = parseChecklist(
        File("src/main/assets/recorded/checklist.json").readText()
    )

    @Test
    fun ignoresNoteAndReadsSevenRoomTypes() {
        assertEquals(7, checklist.roomTypes.size)
    }

    @Test
    fun storageHasNoItems() {
        assertTrue(checklist.roomTypes.single { it.id == "storage" }.items.isEmpty())
    }

    @Test
    fun item30HasNoHint() {
        val item = checklist.roomTypes.flatMap { it.items }.single { it.id == 30 }
        assertNull(item.hint)
    }

    @Test
    fun itemIdsNeverRepeat() {
        val ids = checklist.roomTypes.flatMap { it.items }.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
