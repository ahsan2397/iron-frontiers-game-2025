package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AutonomyTeal
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.HoverYellow
import com.example.ui.theme.PakistanEmerald
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarCrimson
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PakistanFlagBadge(
    isPakistan: Boolean = true,
    isAutonomy: Boolean = false,
    width: Dp = 32.dp,
    height: Dp = 20.dp
) {
    Canvas(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(3.dp))
    ) {
        val w = size.width
        val h = size.height
        if (!isPakistan) {
            drawRect(color = WarCrimson, size = size)
            drawRect(color = Color(0xFF1F2937), size = Size(w * 0.35f, h))
            return@Canvas
        }
        val green = if (isAutonomy) AutonomyTeal else PakistanEmerald
        drawRect(color = Color.White, size = Size(w * 0.28f, h))
        drawRect(color = green, topLeft = Offset(w * 0.28f, 0f), size = Size(w * 0.72f, h))

        val cx = w * 0.64f
        val cy = h * 0.50f
        val rOuter = h * 0.30f
        drawCircle(color = Color.White, radius = rOuter, center = Offset(cx, cy))
        drawCircle(color = green, radius = rOuter * 0.84f, center = Offset(cx + rOuter * 0.26f, cy - rOuter * 0.14f))

        // Crisp 5-point star
        val starCx = cx + rOuter * 0.42f
        val starCy = cy - rOuter * 0.24f
        val starR = rOuter * 0.34f
        val starPath = Path()
        for (i in 0 until 10) {
            val angle = (-PI / 2.0) + i * (PI / 5.0)
            val rad = if (i % 2 == 0) starR else starR * 0.42f
            val px = starCx + (rad * cos(angle)).toFloat()
            val py = starCy + (rad * sin(angle)).toFloat()
            if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
        }
        starPath.close()
        drawPath(starPath, color = Color.White)
    }
}

@Composable
fun RrActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = SurfaceElevated,
    contentColor: Color = TextPrimary,
    accentBorder: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val borderColor = when {
        isHovered || isPressed -> HoverYellow
        accentBorder != null -> accentBorder
        else -> HairlineBorder
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = SurfaceElevated.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.5.dp, borderColor),
        shape = RoundedCornerShape(5.dp),
        contentPadding = contentPadding,
        modifier = modifier,
        content = content
    )
}
