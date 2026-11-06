/*
 * Given by the skeleton: leave it as it is, or restyle it if you wish.
 *
 * Drawing with a finger is done for you because it is not taught in class. What is yours is what
 * the report does with the two signatures: when the report counts as signed, where each signature
 * is saved (Room, A4) and what goes to the landlord through Firestore (A6).
 */
package com.example.fianzapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * A signature: the lines drawn with the finger, each one the list of points it went through,
 * in pixels of the pad. An empty list means nobody has signed.
 */
typealias Signature = List<List<Offset>>

/**
 * A white pad, 160 dp high, where a person signs with a finger (or with the mouse in the
 * emulator). The pad does not keep the signature: it receives it in [signature] and reports
 * each new line through [onSignatureChange], so the composable that calls it owns it. To clear
 * the pad, set the signature to an empty list. With [enabled] false, it only shows the
 * signature, for example on the report once it is signed.
 *
 * A list of points does not survive a rotation with rememberSaveable, and Room and Firestore
 * do not store it either. [encodeSignature] turns it into text, and [decodeSignature] turns
 * the text back into a signature:
 *
 * ```
 * var tenantSignature by rememberSaveable { mutableStateOf("") }
 * SignaturePad(
 *     signature = decodeSignature(tenantSignature),
 *     onSignatureChange = { tenantSignature = encodeSignature(it) },
 * )
 * ```
 */
@Composable
fun SignaturePad(
    signature: Signature,
    onSignatureChange: (Signature) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // The line that the finger is drawing now. It is reported only when the finger lifts.
    var currentLine by remember { mutableStateOf(emptyList<Offset>()) }
    // The pointerInput block starts again only when enabled changes, so it must read the
    // latest values through these.
    val latestSignature by rememberUpdatedState(signature)
    val latestOnSignatureChange by rememberUpdatedState(onSignatureChange)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color.White)
            .border(1.dp, Color.Gray)
            // A line that leaves the pad is not drawn over the rest of the screen.
            .clipToBounds()
            .pointerInput(enabled) {
                // A line left half drawn when enabled changed is dropped.
                currentLine = emptyList()
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { start -> currentLine = listOf(start) },
                    onDragEnd = {
                        if (currentLine.isNotEmpty()) {
                            latestOnSignatureChange(latestSignature + listOf(currentLine))
                        }
                        currentLine = emptyList()
                    },
                    onDragCancel = { currentLine = emptyList() },
                    onDrag = { change, _ ->
                        change.consume()
                        currentLine = currentLine + change.position
                    }
                )
            }
    ) {
        val ink = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        for (line in signature + listOf(currentLine)) {
            if (line.isEmpty()) continue
            val path = Path().apply {
                moveTo(line.first().x, line.first().y)
                for (point in line.drop(1)) lineTo(point.x, point.y)
            }
            drawPath(path, Color.Black, style = ink)
        }
    }
}

/** The signature as text, such as "12,40 13,42;80,35 81,37": points by spaces, lines by ";". */
fun encodeSignature(signature: Signature): String =
    signature.joinToString(";") { line ->
        line.joinToString(" ") { "${it.x.roundToInt()},${it.y.roundToInt()}" }
    }

/**
 * The opposite of [encodeSignature]. An empty text gives an empty signature. A text that
 * [encodeSignature] did not write, for example one edited by hand in the Firebase console,
 * loses the points it cannot read instead of closing the app.
 */
fun decodeSignature(text: String): Signature =
    text.split(";").map { line ->
        line.split(" ").mapNotNull { point ->
            val parts = point.split(",")
            // Whole numbers only, as encodeSignature writes them: "NaN" or "1e5" are not points.
            val x = parts.getOrNull(0)?.toIntOrNull()
            val y = parts.getOrNull(1)?.toIntOrNull()
            if (parts.size == 2 && x != null && y != null) Offset(x.toFloat(), y.toFloat()) else null
        }
    }.filter { it.isNotEmpty() }
