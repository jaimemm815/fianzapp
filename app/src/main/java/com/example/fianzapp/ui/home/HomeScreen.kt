package com.example.fianzapp.ui.home

import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fianzapp.R
import com.example.fianzapp.ui.theme.FianzappTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

/*
 * Home screen: only its look. The three options and where they go are decided in
 * FianzappNavHost.kt. Colours are local to this screen, so the inner screens keep the app theme.
 */

private val Navy = Color(0xFF12324F)
private val NavyDeep = Color(0xFF0A1C2E)
private val Teal = Color(0xFF16877B)
private val Mint = Color(0xFFCDEDE6)
private val Gold = Color(0xFFE2AE45)
private val GoldDark = Color(0xFFB8862B)
private val Cream = Color(0xFFF8F5EF)
private val Coral = Color(0xFFD06A4E)

private data class HomePalette(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val title: Color,
    val body: Color,
    val card: Color,
    val cardBorder: Color,
    val house: Color,
    val window: Color,
    val coin: Color,
    val deadlineAccent: Color
)

@Composable
private fun homePalette(): HomePalette =
    if (isSystemInDarkTheme()) {
        HomePalette(
            backgroundTop = NavyDeep,
            backgroundBottom = Color(0xFF0F3634),
            title = Color.White,
            body = Color(0xFFB9C7D3),
            card = Color(0xFF16283A),
            cardBorder = Color(0x33FFFFFF),
            house = Color(0xFF2A4A68),
            window = Gold,
            coin = Gold,
            deadlineAccent = Gold
        )
    } else {
        HomePalette(
            backgroundTop = Cream,
            backgroundBottom = Mint,
            title = Navy,
            body = Color(0xFF4A5A6A),
            card = Color.White,
            cardBorder = Color(0x14000000),
            house = Navy,
            window = Gold,
            coin = Gold,
            deadlineAccent = GoldDark
        )
    }

/** True when the person turned animations off in the phone's developer or accessibility settings. */
@Composable
private fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}

private class HomeOption(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    onChecklist: () -> Unit,
    onDeadline: () -> Unit,
    onSignatureDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = homePalette()
    val reduceMotion = rememberReduceMotion()
    val options = listOf(
        HomeOption(
            R.string.home_checklist, R.string.home_checklist_subtitle,
            Icons.AutoMirrored.Filled.List, Teal, onChecklist
        ),
        HomeOption(
            R.string.home_deadline, R.string.home_deadline_subtitle,
            Icons.Filled.DateRange, palette.deadlineAccent, onDeadline
        ),
        HomeOption(
            R.string.home_signature_demo, R.string.home_signature_demo_subtitle,
            Icons.Filled.Edit, Coral, onSignatureDemo
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(palette.backgroundTop, palette.backgroundBottom)))
    ) {
        FloatingCoins(color = palette.coin, reduceMotion = reduceMotion, modifier = Modifier.fillMaxSize())

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val wide = maxWidth >= 600.dp && maxWidth > maxHeight
            if (wide) {
                // Landscape, tablets and large windows: house on the left, options on the right.
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HomeHero(palette, reduceMotion, Modifier.weight(1f))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        options.forEachIndexed { index, option ->
                            HomeOptionCard(option, index, palette, reduceMotion)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HomeHero(
                        palette, reduceMotion,
                        Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.height(28.dp))
                    Column(
                        modifier = Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        options.forEachIndexed { index, option ->
                            HomeOptionCard(option, index, palette, reduceMotion)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHero(palette: HomePalette, reduceMotion: Boolean, modifier: Modifier = Modifier) {
    var lightsOn by rememberSaveable { mutableStateOf(true) }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        HouseIllustration(
            palette = palette,
            lightsOn = lightsOn,
            reduceMotion = reduceMotion,
            modifier = Modifier
                .size(width = 210.dp, height = 170.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = stringResource(R.string.home_house_toggle),
                    role = Role.Button
                ) { lightsOn = !lightsOn }
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = palette.title
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.home_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = palette.body,
            textAlign = TextAlign.Center
        )
    }
}

/** A flat with glowing windows and a key hanging from the eave. Tapping it switches the lights. */
@Composable
private fun HouseIllustration(
    palette: HomePalette,
    lightsOn: Boolean,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "house")
    val glow = transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val swing = transition.animateFloat(
        initialValue = -9f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "swing"
    )
    val lights by animateFloatAsState(
        targetValue = if (lightsOn) 1f else 0f,
        animationSpec = tween(if (reduceMotion) 0 else 450),
        label = "lights"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val glowNow = if (reduceMotion) 1f else glow.value
        val swingNow = if (reduceMotion) 0f else swing.value

        // Soft shadow on the ground.
        drawOval(
            color = Color.Black.copy(alpha = 0.08f),
            topLeft = Offset(w * 0.14f, h * 0.86f),
            size = Size(w * 0.72f, h * 0.08f)
        )

        // Chimney, then roof on top of it.
        drawRect(palette.house, topLeft = Offset(w * 0.64f, h * 0.16f), size = Size(w * 0.08f, h * 0.2f))
        val roof = Path().apply {
            moveTo(w * 0.13f, h * 0.47f)
            lineTo(w * 0.5f, h * 0.1f)
            lineTo(w * 0.87f, h * 0.47f)
            close()
        }
        drawPath(roof, Teal)
        drawPath(roof, Teal, style = Stroke(width = 8.dp.toPx(), join = StrokeJoin.Round))

        // Walls.
        drawRoundRect(
            color = palette.house,
            topLeft = Offset(w * 0.2f, h * 0.45f),
            size = Size(w * 0.6f, h * 0.43f),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Two windows: dark glass, then the light that glows.
        val windowSize = Size(w * 0.12f, w * 0.12f)
        listOf(Offset(w * 0.27f, h * 0.54f), Offset(w * 0.61f, h * 0.54f)).forEach { topLeft ->
            val center = Offset(topLeft.x + windowSize.width / 2, topLeft.y + windowSize.height / 2)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.window.copy(alpha = 0.35f * lights * glowNow), Color.Transparent),
                    center = center,
                    radius = w * 0.14f
                ),
                radius = w * 0.14f,
                center = center
            )
            drawRoundRect(Color.White.copy(alpha = 0.18f), topLeft, windowSize, CornerRadius(3.dp.toPx()))
            drawRoundRect(
                palette.window.copy(alpha = lights * (0.55f + 0.45f * glowNow)),
                topLeft, windowSize, CornerRadius(3.dp.toPx())
            )
            val bar = 2.dp.toPx()
            drawLine(palette.house, Offset(center.x, topLeft.y), Offset(center.x, topLeft.y + windowSize.height), bar)
            drawLine(palette.house, Offset(topLeft.x, center.y), Offset(topLeft.x + windowSize.width, center.y), bar)
        }

        // Door with its knob.
        drawRoundRect(
            color = Teal,
            topLeft = Offset(w * 0.44f, h * 0.66f),
            size = Size(w * 0.12f, h * 0.22f),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        drawCircle(Gold, radius = 2.dp.toPx(), center = Offset(w * 0.53f, h * 0.78f))

        // The key, hanging from the right eave and swinging gently.
        val pivot = Offset(w * 0.9f, h * 0.48f)
        rotate(degrees = swingNow, pivot = pivot) {
            val stroke = 2.5f.dp.toPx()
            val ringCenter = Offset(pivot.x, pivot.y + h * 0.12f)
            val ringRadius = w * 0.035f
            drawLine(palette.body.copy(alpha = 0.6f), pivot, Offset(pivot.x, ringCenter.y - ringRadius), 1.dp.toPx())
            drawCircle(Gold, radius = ringRadius, center = ringCenter, style = Stroke(stroke))
            val shaftEnd = Offset(ringCenter.x, ringCenter.y + ringRadius + h * 0.13f)
            drawLine(Gold, Offset(ringCenter.x, ringCenter.y + ringRadius), shaftEnd, stroke, StrokeCap.Round)
            drawLine(Gold, Offset(shaftEnd.x, shaftEnd.y - h * 0.02f), Offset(shaftEnd.x + w * 0.03f, shaftEnd.y - h * 0.02f), stroke, StrokeCap.Round)
            drawLine(Gold, Offset(shaftEnd.x, shaftEnd.y - h * 0.06f), Offset(shaftEnd.x + w * 0.022f, shaftEnd.y - h * 0.06f), stroke, StrokeCap.Round)
        }
    }
}

/** One coin of the background: where it rises, where it starts and how fast (1 or 2 loops). */
private class Coin(val x: Float, val start: Float, val radius: Dp, val loops: Int)

private val coins = listOf(
    Coin(0.07f, 0.00f, 10.dp, 1),
    Coin(0.21f, 0.55f, 6.dp, 2),
    Coin(0.38f, 0.30f, 8.dp, 1),
    Coin(0.52f, 0.85f, 5.dp, 2),
    Coin(0.66f, 0.10f, 7.dp, 1),
    Coin(0.80f, 0.65f, 11.dp, 1),
    Coin(0.93f, 0.40f, 6.dp, 2)
)

/** Deposit coins that rise slowly behind the content and fade in and out. */
@Composable
private fun FloatingCoins(color: Color, reduceMotion: Boolean, modifier: Modifier = Modifier) {
    val rise = rememberInfiniteTransition(label = "coins").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26_000, easing = LinearEasing)),
        label = "rise"
    )
    Canvas(modifier = modifier) {
        val t = if (reduceMotion) 0f else rise.value
        coins.forEach { coin ->
            // Whole numbers of loops keep the movement seamless when the animation restarts.
            val progress = (t * coin.loops + coin.start) % 1f
            val fade = sin(progress * PI).toFloat()
            val center = Offset(size.width * coin.x, size.height * (1.05f - progress * 1.1f))
            val r = coin.radius.toPx()
            drawCircle(color.copy(alpha = 0.20f * fade), radius = r, center = center)
            drawCircle(color.copy(alpha = 0.35f * fade), radius = r * 0.62f, center = center, style = Stroke(1.dp.toPx()))
        }
    }
}

@Composable
private fun HomeOptionCard(option: HomeOption, index: Int, palette: HomePalette, reduceMotion: Boolean) {
    // Cards come in one after another, sliding up a little.
    val appear = remember { Animatable(if (reduceMotion) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            delay(150L + index * 110L)
            appear.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        }
    }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.97f
            hovered -> 1.015f
            else -> 1f
        },
        label = "cardScale"
    )
    val arrowShift by animateFloatAsState(
        targetValue = if (pressed || hovered) 1f else 0f,
        label = "arrowShift"
    )

    Card(
        onClick = option.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = appear.value
                translationY = (1f - appear.value) * 24.dp.toPx()
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 6.dp, hoveredElevation = 5.dp),
        border = BorderStroke(1.dp, palette.cardBorder),
        interactionSource = interaction
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(option.accent.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(option.icon, contentDescription = null, tint = option.accent)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(option.title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.title
                )
                Text(
                    text = stringResource(option.subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.body
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = palette.body,
                modifier = Modifier.graphicsLayer { translationX = arrowShift * 4.dp.toPx() }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun HomeScreenPreview() {
    FianzappTheme {
        HomeScreen(onChecklist = {}, onDeadline = {}, onSignatureDemo = {})
    }
}
