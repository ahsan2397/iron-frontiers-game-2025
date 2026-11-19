package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameWorldSeed
import com.example.model.RegionState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.HoverYellow
import com.example.ui.theme.KingGold
import com.example.ui.theme.PakistanBright
import com.example.ui.theme.WarCrimson
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private data class MapPolygonSpec(
    val fill: Color,
    val points: List<Pair<Float, Float>>
)

val RrOceanBlue = Color(0xFF4F6592)
private val MapBorderStroke = Color(0xFF1B2332)

private val politicalWorldPolygons: List<MapPolygonSpec> = listOf(
    // North America & Greenland
    MapPolygonSpec(Color(0xFF8C969E), listOf(0.05f to 0.30f, 0.11f to 0.25f, 0.12f to 0.32f, 0.06f to 0.34f)), // Alaska
    MapPolygonSpec(Color(0xFF1F2937), listOf(0.11f to 0.24f, 0.22f to 0.17f, 0.29f to 0.22f, 0.26f to 0.30f, 0.12f to 0.32f)), // Northern Canada
    MapPolygonSpec(Color(0xFF16A34A), listOf(0.16f to 0.28f, 0.21f to 0.27f, 0.20f to 0.34f, 0.15f to 0.34f)), // Central Canada green
    MapPolygonSpec(Color(0xFFDC2626), listOf(0.21f to 0.27f, 0.25f to 0.26f, 0.25f to 0.33f, 0.20f to 0.34f)), // East Canada crimson
    MapPolygonSpec(Color(0xFF22C55E), listOf(0.29f to 0.15f, 0.40f to 0.14f, 0.37f to 0.26f, 0.32f to 0.28f, 0.28f to 0.22f)), // Greenland
    MapPolygonSpec(Color(0xFFFACC15), listOf(0.11f to 0.34f, 0.17f to 0.34f, 0.17f to 0.42f, 0.11f to 0.40f)), // Western USA
    MapPolygonSpec(Color(0xFF06B6D4), listOf(0.17f to 0.34f, 0.23f to 0.34f, 0.22f to 0.42f, 0.17f to 0.42f)), // Central USA
    MapPolygonSpec(Color(0xFF2563EB), listOf(0.23f to 0.32f, 0.29f to 0.31f, 0.27f to 0.40f, 0.22f to 0.41f)), // East Coast USA
    MapPolygonSpec(Color(0xFF0F766E), listOf(0.15f to 0.42f, 0.22f to 0.42f, 0.21f to 0.47f, 0.16f to 0.46f)), // Texas / South USA
    MapPolygonSpec(Color(0xFF15803D), listOf(0.14f to 0.46f, 0.20f to 0.47f, 0.22f to 0.53f, 0.17f to 0.52f)), // Mexico & Central America

    // South America
    MapPolygonSpec(Color(0xFF06B6D4), listOf(0.23f to 0.53f, 0.28f to 0.52f, 0.27f to 0.59f, 0.22f to 0.57f)), // Colombia/Venezuela
    MapPolygonSpec(Color(0xFF16A34A), listOf(0.25f to 0.55f, 0.34f to 0.59f, 0.32f to 0.68f, 0.25f to 0.65f)), // Brazil
    MapPolygonSpec(Color(0xFFDC2626), listOf(0.21f to 0.58f, 0.26f to 0.60f, 0.25f to 0.68f, 0.22f to 0.65f)), // Peru/Ecuador
    MapPolygonSpec(Color(0xFF38BDF8), listOf(0.24f to 0.67f, 0.30f to 0.68f, 0.27f to 0.80f, 0.23f to 0.77f)), // Argentina/Chile
    MapPolygonSpec(Color(0xFFF59E0B), listOf(0.29f to 0.64f, 0.33f to 0.66f, 0.30f to 0.72f, 0.27f to 0.69f)), // Bolivia/Paraguay

    // Europe
    MapPolygonSpec(Color(0xFFDC2626), listOf(0.41f to 0.18f, 0.44f to 0.17f, 0.44f to 0.24f, 0.41f to 0.24f)), // UK & Ireland
    MapPolygonSpec(Color(0xFF06B6D4), listOf(0.46f to 0.14f, 0.50f to 0.13f, 0.49f to 0.21f, 0.46f to 0.20f)), // Norway
    MapPolygonSpec(Color(0xFFFACC15), listOf(0.49f to 0.13f, 0.53f to 0.14f, 0.52f to 0.22f, 0.48f to 0.21f)), // Sweden/Finland
    MapPolygonSpec(Color(0xFF3B82F6), listOf(0.43f to 0.25f, 0.47f to 0.24f, 0.47f to 0.31f, 0.43f to 0.30f)), // France
    MapPolygonSpec(Color(0xFFEA580C), listOf(0.40f to 0.31f, 0.44f to 0.31f, 0.44f to 0.36f, 0.40f to 0.35f)), // Spain/Portugal
    MapPolygonSpec(Color(0xFF64748B), listOf(0.47f to 0.21f, 0.51f to 0.21f, 0.50f to 0.28f, 0.47f to 0.28f)), // Germany/Central EU
    MapPolygonSpec(Color(0xFF16A34A), listOf(0.46f to 0.29f, 0.50f to 0.30f, 0.49f to 0.36f, 0.47f to 0.35f)), // Italy
    MapPolygonSpec(Color(0xFF9333EA), listOf(0.50f to 0.22f, 0.55f to 0.22f, 0.54f to 0.30f, 0.50f to 0.29f)), // Eastern Europe

    // Africa
    MapPolygonSpec(Color(0xFF16A34A), listOf(0.42f to 0.38f, 0.49f to 0.38f, 0.48f to 0.48f, 0.40f to 0.46f)), // Morocco/Algeria
    MapPolygonSpec(Color(0xFF22C55E), listOf(0.49f to 0.39f, 0.54f to 0.40f, 0.54f to 0.49f, 0.48f to 0.48f)), // Libya/Egypt
    MapPolygonSpec(Color(0xFF84CC16), listOf(0.40f to 0.46f, 0.47f to 0.48f, 0.47f to 0.56f, 0.41f to 0.54f)), // West Africa
    MapPolygonSpec(Color(0xFFDC2626), listOf(0.47f to 0.48f, 0.54f to 0.49f, 0.53f to 0.59f, 0.47f to 0.58f)), // Central Africa
    MapPolygonSpec(Color(0xFF06B6D4), listOf(0.53f to 0.49f, 0.58f to 0.50f, 0.56f to 0.60f, 0.53f to 0.58f)), // Horn of Africa
    MapPolygonSpec(Color(0xFF15803D), listOf(0.47f to 0.58f, 0.55f to 0.59f, 0.53f to 0.72f, 0.48f to 0.70f)), // Southern Africa

    // Russia & Eurasia
    MapPolygonSpec(Color(0xFF991B1B), listOf(0.54f to 0.14f, 0.63f to 0.13f, 0.62f to 0.27f, 0.54f to 0.26f)), // West Russia
    MapPolygonSpec(Color(0xFF0D9488), listOf(0.62f to 0.12f, 0.70f to 0.12f, 0.69f to 0.26f, 0.62f to 0.26f)), // Central Siberia
    MapPolygonSpec(Color(0xFFEAB308), listOf(0.69f to 0.11f, 0.87f to 0.13f, 0.84f to 0.27f, 0.69f to 0.26f)), // East Siberia gold
    MapPolygonSpec(Color(0xFF0284C7), listOf(0.84f to 0.14f, 0.92f to 0.18f, 0.88f to 0.29f, 0.83f to 0.27f)), // Far East Russia

    // Middle East, Pakistan, India & Central Asia
    MapPolygonSpec(Color(0xFFDC2626), listOf(0.51f to 0.31f, 0.57f to 0.31f, 0.56f to 0.36f, 0.51f to 0.36f)), // Turkey
    MapPolygonSpec(Color(0xFF1E3A8A), listOf(0.53f to 0.43f, 0.59f to 0.44f, 0.58f to 0.52f, 0.53f to 0.50f)), // Saudi Arabia / UAE
    MapPolygonSpec(Color(0xFF0F766E), listOf(0.56f to 0.36f, 0.61f to 0.36f, 0.60f to 0.44f, 0.55f to 0.43f)), // Iran
    MapPolygonSpec(Color(0xFFA855F7), listOf(0.59f to 0.35f, 0.64f to 0.35f, 0.62f to 0.40f, 0.59f to 0.40f)), // Afghanistan / Central Asia
    MapPolygonSpec(Color(0xFF16A34A), listOf(0.59f to 0.39f, 0.66f to 0.39f, 0.64f to 0.53f, 0.59f to 0.51f)), // Pakistan Emerald Core
    MapPolygonSpec(Color(0xFFF97316), listOf(0.65f to 0.43f, 0.73f to 0.45f, 0.70f to 0.58f, 0.64f to 0.54f)), // India
    MapPolygonSpec(Color(0xFF84CC16), listOf(0.72f to 0.46f, 0.78f to 0.49f, 0.77f to 0.58f, 0.72f to 0.55f)), // Bangladesh & SE Asia

    // China, Japan & Oceania
    MapPolygonSpec(Color(0xFFF97316), listOf(0.67f to 0.28f, 0.83f to 0.29f, 0.82f to 0.44f, 0.69f to 0.43f)), // China Orange
    MapPolygonSpec(Color(0xFF1D4ED8), listOf(0.75f to 0.27f, 0.83f to 0.28f, 0.81f to 0.35f, 0.74f to 0.34f)), // North China / Manchuria
    MapPolygonSpec(Color(0xFFEF4444), listOf(0.84f to 0.34f, 0.89f to 0.33f, 0.87f to 0.41f, 0.84f to 0.40f)), // Japan
    MapPolygonSpec(Color(0xFF22C55E), listOf(0.76f to 0.58f, 0.84f to 0.61f, 0.82f to 0.65f, 0.75f to 0.62f)), // Indonesia
    MapPolygonSpec(Color(0xFFF5B7B1), listOf(0.81f to 0.70f, 0.91f to 0.69f, 0.90f to 0.83f, 0.80f to 0.81f)), // Australia Pink
    MapPolygonSpec(Color(0xFFEC4899), listOf(0.92f to 0.80f, 0.95f to 0.79f, 0.94f to 0.86f, 0.92f to 0.85f))  // New Zealand
)

@Composable
fun WorldPoliticalMapCanvas(
    regionsById: Map<String, RegionState>,
    selectedRegionId: String,
    currentRegionId: String,
    playerStateName: String,
    zoom: Float,
    pan: Offset,
    showLabels: Boolean,
    onTransform: (zoomDelta: Float, panDelta: Offset, viewportWidth: Float, viewportHeight: Float) -> Unit,
    onRegionTap: (String) -> Unit,
    onRegionDoubleTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapNodes = GameWorldSeed.mapNodes
    val textMeasurer = rememberTextMeasurer()

    fun findNearestRegionId(tap: Offset, width: Float, height: Float): String? {
        val cx = width / 2f
        val cy = height / 2f
        val worldX = (tap.x - cx - pan.x) / zoom + cx
        val worldY = (tap.y - cy - pan.y) / zoom + cy
        val nearest = mapNodes.minByOrNull { node ->
            hypot(worldX - node.xFraction * width, worldY - node.yFraction * height)
        } ?: return null
        val dist = hypot(worldX - nearest.xFraction * width, worldY - nearest.yFraction * height)
        return if (dist <= (95f / zoom.coerceAtLeast(1f)).coerceAtLeast(35f)) nearest.id else null
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(RrOceanBlue)
            .pointerInput(Unit) {
                detectTransformGestures { _, panChange, zoomChange, _ ->
                    onTransform(zoomChange, panChange, size.width.toFloat(), size.height.toFloat())
                }
            }
            .pointerInput(zoom, pan, regionsById) {
                detectTapGestures(
                    onTap = { tap ->
                        findNearestRegionId(tap, size.width.toFloat(), size.height.toFloat())?.let(onRegionTap)
                    },
                    onDoubleTap = { tap ->
                        findNearestRegionId(tap, size.width.toFloat(), size.height.toFloat())?.let(onRegionDoubleTap)
                    }
                )
            }
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        withTransform({
            translate(left = cx + pan.x, top = cy + pan.y)
            scale(scaleX = zoom, scaleY = zoom, pivot = Offset.Zero)
            translate(left = -cx, top = -cy)
        }) {
            // 1. Multi-colored political territories & borders
            politicalWorldPolygons.forEach { poly ->
                val path = Path().apply {
                    poly.points.forEachIndexed { idx, (xf, yf) ->
                        val px = xf * w
                        val py = yf * h
                        if (idx == 0) moveTo(px, py) else lineTo(px, py)
                    }
                    close()
                }
                drawPath(path = path, color = poly.fill)
                drawPath(
                    path = path,
                    color = MapBorderStroke,
                    style = Stroke(width = (1.1f / zoom).dp.toPx())
                )
            }

            // 2. Subtle travel / border links
            val nodeById = mapNodes.associateBy { it.id }
            mapNodes.forEach { node ->
                val start = Offset(node.xFraction * w, node.yFraction * h)
                node.neighbors.forEach { nbId ->
                    nodeById[nbId]?.let { nb ->
                        drawLine(
                            color = MapBorderStroke.copy(alpha = 0.25f),
                            start = start,
                            end = Offset(nb.xFraction * w, nb.yFraction * h),
                            strokeWidth = (0.9f / zoom),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )
                    }
                }
            }

            // 3. Clickable markers for all 40 Real Countries
            mapNodes.forEach { node ->
                val region = regionsById[node.id]
                val center = Offset(node.xFraction * w, node.yFraction * h)
                val isSelected = node.id == selectedRegionId
                val isCurrent = node.id == currentRegionId
                val isPlayerSide = region?.controller.equals(playerStateName, ignoreCase = true) ||
                    region?.country == "Pakistan"

                val markerRadius = when {
                    isSelected -> (6.5f / zoom.coerceAtMost(2.2f)).dp.toPx()
                    node.id == "punjab" -> (6.0f / zoom.coerceAtMost(2.2f)).dp.toPx()
                    else -> (3.8f / zoom.coerceAtMost(2.0f)).dp.toPx()
                }

                val fillColor = when {
                    isSelected -> HoverYellow
                    isCurrent -> CyanAccent
                    isPlayerSide -> PakistanBright
                    else -> Color(0xFF1F2937)
                }
                val ringColor = when {
                    isSelected -> Color.White
                    node.id == "punjab" -> KingGold
                    isCurrent -> CyanAccent
                    isPlayerSide -> PakistanBright
                    else -> Color.White.copy(alpha = 0.72f)
                }

                drawCircle(color = fillColor, radius = markerRadius, center = center)
                drawCircle(
                    color = ringColor,
                    radius = markerRadius,
                    center = center,
                    style = Stroke(width = (if (isSelected) 1.8f else 1.1f).dp.toPx() / zoom.coerceAtLeast(1f))
                )

                // Capital star on Punjab - Pakistan
                if (node.id == "punjab") {
                    drawGoldStar(center = center, starRadius = markerRadius * 0.72f)
                }

                // Region labels when zoomed in or selected
                if (showLabels && (zoom >= 1.55f || isSelected || isCurrent || node.id == "punjab")) {
                    drawRegionLabel(
                        textMeasurer = textMeasurer,
                        label = node.shortName,
                        anchor = Offset(center.x + markerRadius + 3f, center.y - markerRadius - 2f),
                        isHighlighted = isSelected || isCurrent,
                        zoom = zoom
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawGoldStar(center: Offset, starRadius: Float) {
    val starPath = Path()
    for (i in 0 until 10) {
        val ang = (-PI / 2.0) + i * (PI / 5.0)
        val r = if (i % 2 == 0) starRadius else starRadius * 0.42f
        val px = center.x + (r * cos(ang)).toFloat()
        val py = center.y + (r * sin(ang)).toFloat()
        if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
    }
    starPath.close()
    drawPath(starPath, color = KingGold)
}

private fun DrawScope.drawRegionLabel(
    textMeasurer: TextMeasurer,
    label: String,
    anchor: Offset,
    isHighlighted: Boolean,
    zoom: Float
) {
    val style = TextStyle(
        color = if (isHighlighted) HoverYellow else Color.White,
        fontSize = (9f / zoom.coerceAtLeast(1f).coerceAtMost(1.8f)).sp,
        fontWeight = FontWeight.Bold
    )
    val layout = textMeasurer.measure(text = label, style = style)
    if (anchor.x + layout.size.width <= size.width && anchor.y >= 0f) {
        drawText(textLayoutResult = layout, topLeft = anchor)
    }
}
