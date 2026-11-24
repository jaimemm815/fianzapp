package com.example.fianzapp.domain

/*
 * A9 and A10: the rules about where a photo was taken and whether a pair shows the same thing.
 * No Android and no ML Kit inside, so they are tested on the computer.
 */

/** A9: farther than this from the flat is "far". Wide on purpose: the approximate location can be ~1 km off. */
const val FAR_FROM_FLAT_METRES = 5_000f

fun isFarFromFlat(distanceMetres: Float?): Boolean = distanceMetres != null && distanceMetres > FAR_FROM_FLAT_METRES

/** A10: one label of ML Kit's image labeler. */
data class PhotoLabel(val text: String, val confidence: Float)

/** A10: the minimum confidence a label needs to count. */
const val LABEL_THRESHOLD = 0.7f

/**
 * Labels that two photos of the same room share whatever they show, or that describe the photo
 * rather than a thing in it (a covered webcam gives Monochrome). They do not count.
 */
val GENERIC_LABELS = setOf("Room", "Wall", "Monochrome")

enum class Comparison { SAME, DIFFERENT, CANNOT_TELL }

/**
 * The same thing when the two photos share a label above [threshold] that is not generic.
 * Cannot tell when either photo has no such label: it never says "different" without labels to back it.
 */
fun comparePhotos(
    moveIn: List<PhotoLabel>,
    moveOut: List<PhotoLabel>,
    threshold: Float = LABEL_THRESHOLD
): Comparison {
    fun useful(labels: List<PhotoLabel>) =
        labels.filter { it.confidence >= threshold && it.text !in GENERIC_LABELS }.map { it.text }.toSet()

    val a = useful(moveIn)
    val b = useful(moveOut)
    return when {
        a.isEmpty() || b.isEmpty() -> Comparison.CANNOT_TELL
        a.intersect(b).isNotEmpty() -> Comparison.SAME
        else -> Comparison.DIFFERENT
    }
}

/** Labels as the text kept in Room: "Sink:0.91;Tile:0.80". An empty list gives "". */
fun encodeLabels(labels: List<PhotoLabel>): String =
    labels.joinToString(";") { "${it.text}:${"%.2f".format(java.util.Locale.ROOT, it.confidence)}" }

/** The opposite of [encodeLabels]. Null or unreadable parts give no label instead of a crash. */
fun parseLabels(text: String?): List<PhotoLabel> =
    text.orEmpty().split(";").mapNotNull { part ->
        val i = part.lastIndexOf(':')
        if (i <= 0) return@mapNotNull null
        val confidence = part.substring(i + 1).toFloatOrNull() ?: return@mapNotNull null
        PhotoLabel(part.substring(0, i), confidence)
    }
