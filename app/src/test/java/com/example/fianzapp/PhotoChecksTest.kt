package com.example.fianzapp

import com.example.fianzapp.domain.Comparison
import com.example.fianzapp.domain.PhotoLabel
import com.example.fianzapp.domain.comparePhotos
import com.example.fianzapp.domain.encodeLabels
import com.example.fianzapp.domain.isFarFromFlat
import com.example.fianzapp.domain.parseLabels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** A9 and A10. Expected answers worked out by hand from the rules. */
class PhotoChecksTest {

    private val sink = PhotoLabel("Sink", 0.91f)
    private val tile = PhotoLabel("Tile", 0.80f)
    private val couch = PhotoLabel("Couch", 0.88f)

    @Test
    fun sharedLabelAboveThreshold_same() {
        assertEquals(Comparison.SAME, comparePhotos(listOf(sink, tile), listOf(tile)))
    }

    @Test
    fun noSharedLabel_different() {
        assertEquals(Comparison.DIFFERENT, comparePhotos(listOf(sink), listOf(couch)))
    }

    @Test
    fun noLabelAboveThreshold_cannotTell() {
        val weak = PhotoLabel("Sink", 0.40f)
        assertEquals(Comparison.CANNOT_TELL, comparePhotos(listOf(weak), listOf(sink)))
        assertEquals(Comparison.CANNOT_TELL, comparePhotos(emptyList(), emptyList()))
    }

    @Test
    fun onlyGenericLabels_cannotTell() {
        val room = PhotoLabel("Room", 0.95f)
        val wall = PhotoLabel("Wall", 0.90f)
        assertEquals(Comparison.CANNOT_TELL, comparePhotos(listOf(room, wall), listOf(room)))
    }

    @Test
    fun labels_roundTripThroughText() {
        val text = encodeLabels(listOf(sink, tile))
        assertEquals("Sink:0.91;Tile:0.80", text)
        assertEquals(listOf(sink, tile), parseLabels(text))
        assertTrue(parseLabels(null).isEmpty())
        assertTrue(parseLabels("").isEmpty())
    }

    @Test
    fun far_onlyBeyondFiveKilometres() {
        assertFalse(isFarFromFlat(null))
        assertFalse(isFarFromFlat(1_200f))
        assertTrue(isFarFromFlat(11_100f))
    }
}
