package com.ben.firstaid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IconsTest {
    @Test
    fun every_icon_has_shapes_and_stays_inside_the_24_unit_grid() {
        for (icon in AppIcon.entries) {
            val ops = iconOps(icon)
            assertTrue(ops.isNotEmpty(), "${icon.name} has no shapes")
            for (op in ops) {
                val numbers: List<Float> = when (op) {
                    is LineOp -> listOf(op.x1, op.y1, op.x2, op.y2)
                    is CircleOp -> listOf(op.cx - op.r, op.cy - op.r, op.cx + op.r, op.cy + op.r)
                    is RectOp -> listOf(op.x, op.y, op.x + op.w, op.y + op.h)
                    is PolyOp -> {
                        assertEquals(0, op.points.size % 2, "${icon.name}: odd number of coordinates")
                        op.points
                    }
                    is PathOp -> {
                        assertEquals('M', op.cmds.first().op, "${icon.name}: path must start with M")
                        op.cmds.flatMap { it.v.toList() }
                    }
                }
                assertTrue(numbers.all { it >= 0f && it <= ICON_GRID }, "${icon.name} leaves the grid")
            }
        }
    }

    @Test
    fun path_commands_have_the_right_number_of_values() {
        val expected = mapOf('M' to 2, 'L' to 2, 'C' to 6, 'Z' to 0)
        for (icon in AppIcon.entries) {
            for (op in iconOps(icon)) {
                if (op is PathOp) {
                    for (cmd in op.cmds) assertEquals(expected[cmd.op], cmd.v.size, "${icon.name} ${cmd.op}")
                }
            }
        }
    }

    @Test
    fun every_screen_maps_to_an_icon_and_detail_screens_use_their_section_icon() {
        Screen.entries.forEach { screenIcon(it) }
        assertEquals(AppIcon.Home, screenIcon(Screen.Home))
        assertEquals(AppIcon.Guide, screenIcon(Screen.Guidance))
        assertEquals(AppIcon.Learn, screenIcon(Screen.Lesson))
        assertEquals(AppIcon.Kit, screenIcon(Screen.KitItem))
    }

    @Test
    fun unknown_ids_fall_back_to_a_generic_icon() {
        assertEquals(AppIcon.Guide, situationIcon("does_not_exist"))
        assertEquals(AppIcon.Learn, lessonIcon("does_not_exist"))
    }
}
