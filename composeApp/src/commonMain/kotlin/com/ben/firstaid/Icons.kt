package com.ben.firstaid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// Icons are drawn from simple shapes on a 24 x 24 grid, so they need no image files and no icon
// library, look the same on every platform and take whatever colour they are given.
// The shapes below were generated from one list of coordinates (also used for the preview picture).

internal enum class AppIcon {
    Home,
    Guide,
    Learn,
    Kit,
    Quiz,
    Search,
    Emergency,
    Sources,
    About,
    Burns,
    Bleeding,
    Choking,
    Fracture,
    HeartAttack,
    CardiacArrest,
    Fundamentals,
    SceneSafety,
}

internal sealed interface IconOp
internal class LineOp(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val width: Float = 2f) : IconOp
internal class CircleOp(val cx: Float, val cy: Float, val r: Float, val fill: Boolean) : IconOp
internal class RectOp(val x: Float, val y: Float, val w: Float, val h: Float, val r: Float, val fill: Boolean) : IconOp
/** [points] is x1, y1, x2, y2, ... */
internal class PolyOp(val points: List<Float>, val closed: Boolean, val fill: Boolean) : IconOp
/** [op] is 'M' (move), 'L' (line), 'C' (cubic curve) or 'Z' (close). */
internal class Cmd(val op: Char, vararg val v: Float)
internal class PathOp(val cmds: List<Cmd>, val fill: Boolean) : IconOp

internal const val ICON_GRID = 24f

internal fun iconOps(icon: AppIcon): List<IconOp> = when (icon) {
    AppIcon.Home -> listOf(
        PolyOp(listOf(3f, 11.5f, 12f, 3.5f, 21f, 11.5f), false, false),
        PolyOp(listOf(5.5f, 10f, 5.5f, 20.5f, 18.5f, 20.5f, 18.5f, 10f), false, false),
        PolyOp(listOf(10f, 20.5f, 10f, 14.5f, 14f, 14.5f, 14f, 20.5f), false, false),
    )
    AppIcon.Guide -> listOf(
        RectOp(3f, 3f, 18f, 18f, 4.5f, false),
        LineOp(12f, 7f, 12f, 17f, 2f),
        LineOp(7f, 12f, 17f, 12f, 2f),
    )
    AppIcon.Learn -> listOf(
        PathOp(listOf(Cmd('M', 12f, 6.5f), Cmd('C', 10f, 5f, 7f, 4.5f, 3.5f, 5f), Cmd('L', 3.5f, 19f), Cmd('C', 7f, 18.5f, 10f, 19f, 12f, 20.5f), Cmd('C', 14f, 19f, 17f, 18.5f, 20.5f, 19f), Cmd('L', 20.5f, 5f), Cmd('C', 17f, 4.5f, 14f, 5f, 12f, 6.5f), Cmd('Z')), false),
        LineOp(12f, 6.5f, 12f, 20.5f, 2f),
    )
    AppIcon.Kit -> listOf(
        RectOp(3f, 7.5f, 18f, 13f, 2.5f, false),
        PolyOp(listOf(9f, 7.5f, 9f, 4.5f, 15f, 4.5f, 15f, 7.5f), false, false),
        LineOp(12f, 11f, 12f, 17f, 2f),
        LineOp(8.5f, 14f, 15.5f, 14f, 2f),
    )
    AppIcon.Quiz -> listOf(
        CircleOp(12f, 12f, 9.5f, false),
        PolyOp(listOf(7.5f, 12.5f, 10.8f, 15.8f, 16.8f, 9f), false, false),
    )
    AppIcon.Search -> listOf(
        CircleOp(10.5f, 10.5f, 6.5f, false),
        LineOp(15.5f, 15.5f, 21f, 21f, 2f),
    )
    AppIcon.Emergency -> listOf(
        PolyOp(listOf(12f, 3.5f, 22f, 20.5f, 2f, 20.5f), true, false),
        LineOp(12f, 10f, 12f, 14.5f, 2f),
        CircleOp(12f, 17.4f, 1f, true),
    )
    AppIcon.Sources -> listOf(
        PolyOp(listOf(6f, 2.5f, 14f, 2.5f, 19f, 7.5f, 19f, 21.5f, 6f, 21.5f), true, false),
        PolyOp(listOf(14f, 2.5f, 14f, 7.5f, 19f, 7.5f), false, false),
        LineOp(9f, 12f, 16f, 12f, 2f),
        LineOp(9f, 15.5f, 16f, 15.5f, 2f),
        LineOp(9f, 19f, 13f, 19f, 2f),
    )
    AppIcon.About -> listOf(
        CircleOp(12f, 12f, 9.5f, false),
        LineOp(12f, 11f, 12f, 17f, 2f),
        CircleOp(12f, 7.6f, 1f, true),
    )
    AppIcon.Burns -> listOf(
        PathOp(listOf(Cmd('M', 12f, 2.5f), Cmd('C', 12.5f, 6.5f, 18.5f, 8.5f, 18.5f, 14.5f), Cmd('C', 18.5f, 18.5f, 15.5f, 21.5f, 12f, 21.5f), Cmd('C', 8.5f, 21.5f, 5.5f, 18.5f, 5.5f, 14.5f), Cmd('C', 5.5f, 12f, 6.5f, 10.5f, 8f, 9f), Cmd('C', 8.4f, 11f, 9.5f, 11.8f, 10.5f, 11.3f), Cmd('C', 11.5f, 8.5f, 11f, 5f, 12f, 2.5f), Cmd('Z')), false),
    )
    AppIcon.Bleeding -> listOf(
        PathOp(listOf(Cmd('M', 12f, 2.5f), Cmd('C', 12f, 2.5f, 5.5f, 10f, 5.5f, 14.5f), Cmd('C', 5.5f, 18.5f, 8.5f, 21.5f, 12f, 21.5f), Cmd('C', 15.5f, 21.5f, 18.5f, 18.5f, 18.5f, 14.5f), Cmd('C', 18.5f, 10f, 12f, 2.5f, 12f, 2.5f), Cmd('Z')), false),
    )
    AppIcon.Choking -> listOf(
        LineOp(12f, 2.5f, 12f, 12f, 2f),
        PathOp(listOf(Cmd('M', 11f, 10f), Cmd('C', 8f, 9f, 4f, 12f, 4f, 17f), Cmd('C', 4f, 19.5f, 5.5f, 20.5f, 7.5f, 20.5f), Cmd('C', 9.5f, 20.5f, 11f, 19.5f, 11f, 17f), Cmd('Z')), false),
        PathOp(listOf(Cmd('M', 13f, 10f), Cmd('C', 16f, 9f, 20f, 12f, 20f, 17f), Cmd('C', 20f, 19.5f, 18.5f, 20.5f, 16.5f, 20.5f), Cmd('C', 14.5f, 20.5f, 13f, 19.5f, 13f, 17f), Cmd('Z')), false),
    )
    AppIcon.Fracture -> listOf(
        LineOp(6.5f, 17.5f, 17.5f, 6.5f, 3.2f),
        CircleOp(17.6f, 4.6f, 2.3f, true),
        CircleOp(19.4f, 6.4f, 2.3f, true),
        CircleOp(4.6f, 17.6f, 2.3f, true),
        CircleOp(6.4f, 19.4f, 2.3f, true),
    )
    AppIcon.HeartAttack -> listOf(
        PathOp(listOf(Cmd('M', 12f, 21f), Cmd('C', 5f, 15f, 2f, 11f, 2f, 7.5f), Cmd('C', 2f, 4.5f, 4.5f, 3f, 7f, 3f), Cmd('C', 9f, 3f, 11f, 4f, 12f, 6f), Cmd('C', 13f, 4f, 15f, 3f, 17f, 3f), Cmd('C', 19.5f, 3f, 22f, 4.5f, 22f, 7.5f), Cmd('C', 22f, 11f, 19f, 15f, 12f, 21f), Cmd('Z')), false),
    )
    AppIcon.CardiacArrest -> listOf(
        PathOp(listOf(Cmd('M', 12f, 21f), Cmd('C', 5f, 15f, 2f, 11f, 2f, 7.5f), Cmd('C', 2f, 4.5f, 4.5f, 3f, 7f, 3f), Cmd('C', 9f, 3f, 11f, 4f, 12f, 6f), Cmd('C', 13f, 4f, 15f, 3f, 17f, 3f), Cmd('C', 19.5f, 3f, 22f, 4.5f, 22f, 7.5f), Cmd('C', 22f, 11f, 19f, 15f, 12f, 21f), Cmd('Z')), false),
        PolyOp(listOf(5f, 12f, 9f, 12f, 10.5f, 8.5f, 13f, 15.5f, 14.5f, 12f, 19f, 12f), false, false),
    )
    AppIcon.Fundamentals -> listOf(
        CircleOp(12f, 12f, 9.5f, false),
        LineOp(12f, 7.5f, 12f, 16.5f, 2f),
        LineOp(7.5f, 12f, 16.5f, 12f, 2f),
    )
    AppIcon.SceneSafety -> listOf(
        PathOp(listOf(Cmd('M', 12f, 2.5f), Cmd('L', 20f, 5.5f), Cmd('L', 20f, 11.5f), Cmd('C', 20f, 16.5f, 16.5f, 20.5f, 12f, 22f), Cmd('C', 7.5f, 20.5f, 4f, 16.5f, 4f, 11.5f), Cmd('L', 4f, 5.5f), Cmd('Z')), false),
        PolyOp(listOf(8.5f, 12f, 11f, 14.5f, 15.5f, 9.5f), false, false),
    )
}

@Composable
internal fun IconGlyph(
    icon: AppIcon,
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = LocalContentColor.current,
) {
    val ops = iconOps(icon)
    Canvas(modifier) {
        val u = size.minDimension / ICON_GRID
        for (op in ops) drawOp(op, tint, u)
    }
}

private fun DrawScope.drawOp(op: IconOp, tint: Color, u: Float) {
    fun stroke(width: Float = 2f) = Stroke(width = width * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
    when (op) {
        is LineOp -> drawLine(
            color = tint,
            start = Offset(op.x1 * u, op.y1 * u),
            end = Offset(op.x2 * u, op.y2 * u),
            strokeWidth = op.width * u,
            cap = StrokeCap.Round,
        )
        is CircleOp -> drawCircle(
            color = tint,
            radius = op.r * u,
            center = Offset(op.cx * u, op.cy * u),
            style = if (op.fill) Fill else stroke(),
        )
        is RectOp -> drawRoundRect(
            color = tint,
            topLeft = Offset(op.x * u, op.y * u),
            size = Size(op.w * u, op.h * u),
            cornerRadius = CornerRadius(op.r * u, op.r * u),
            style = if (op.fill) Fill else stroke(),
        )
        is PolyOp -> {
            val path = Path()
            for (i in 0 until op.points.size / 2) {
                val x = op.points[2 * i] * u
                val y = op.points[2 * i + 1] * u
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            if (op.closed) path.close()
            drawPath(path, tint, style = if (op.fill) Fill else stroke())
        }
        is PathOp -> {
            val path = Path()
            for (c in op.cmds) {
                when (c.op) {
                    'M' -> path.moveTo(c.v[0] * u, c.v[1] * u)
                    'L' -> path.lineTo(c.v[0] * u, c.v[1] * u)
                    'C' -> path.cubicTo(c.v[0] * u, c.v[1] * u, c.v[2] * u, c.v[3] * u, c.v[4] * u, c.v[5] * u)
                    'Z' -> path.close()
                }
            }
            drawPath(path, tint, style = if (op.fill) Fill else stroke())
        }
    }
}

// ---------------------------------------------------------------------------
// Which icon belongs to which screen, situation or lesson
// ---------------------------------------------------------------------------

internal fun screenIcon(screen: Screen): AppIcon = when (screen.section) {
    Screen.Home -> AppIcon.Home
    Screen.Guide -> AppIcon.Guide
    Screen.Learn -> AppIcon.Learn
    Screen.Kit -> AppIcon.Kit
    Screen.Quiz -> AppIcon.Quiz
    Screen.Search -> AppIcon.Search
    Screen.Emergency -> AppIcon.Emergency
    Screen.Sources -> AppIcon.Sources
    Screen.About -> AppIcon.About
    else -> AppIcon.Guide
}

internal val situationIcons: Map<String, AppIcon> = mapOf(
    "burns" to AppIcon.Burns,
    "bleeding" to AppIcon.Bleeding,
    "choking" to AppIcon.Choking,
    "fracture" to AppIcon.Fracture,
    "heart_attack" to AppIcon.HeartAttack,
    "cardiac_arrest" to AppIcon.CardiacArrest,
)

internal val lessonIcons: Map<String, AppIcon> = mapOf(
    "fundamentals" to AppIcon.Fundamentals,
    "scene_safety" to AppIcon.SceneSafety,
    "heart_awareness" to AppIcon.HeartAttack,
    "bleeding_awareness" to AppIcon.Bleeding,
    "burn_awareness" to AppIcon.Burns,
)

internal fun situationIcon(id: String): AppIcon = situationIcons[id] ?: AppIcon.Guide
internal fun lessonIcon(id: String): AppIcon = lessonIcons[id] ?: AppIcon.Learn
