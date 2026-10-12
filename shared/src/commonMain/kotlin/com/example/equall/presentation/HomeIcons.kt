package com.example.equall.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Small shared vector set; avoids bundling an entire extended icon library. */
internal object HomeIcons {
    private fun outline(name: String, draw: PathBuilder.() -> Unit) = ImageVector.Builder(
        name, 24.dp, 24.dp, 24f, 24f
    ).apply {
        path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.7f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathBuilder = draw)
    }.build()

    val Home = outline("Home") {
        moveTo(3f, 10f); lineTo(12f, 3f); lineTo(21f, 10f); verticalLineTo(21f)
        horizontalLineTo(15f); verticalLineTo(14f); horizontalLineTo(9f); verticalLineTo(21f)
        horizontalLineTo(3f); close()
    }
    val CreditCard = outline("Payments") {
        moveTo(4f, 5f); horizontalLineTo(20f); quadTo(22f, 5f, 22f, 7f)
        verticalLineTo(18f); quadTo(22f, 20f, 20f, 20f); horizontalLineTo(4f)
        quadTo(2f, 20f, 2f, 18f); verticalLineTo(7f); quadTo(2f, 5f, 4f, 5f); close()
        moveTo(2f, 10f); horizontalLineTo(22f)
    }
    val Payments = outline("Loans") {
        moveTo(3f, 6f); horizontalLineTo(21f); verticalLineTo(18f); horizontalLineTo(3f); close()
        moveTo(15f, 12f); curveTo(15f, 16f, 9f, 16f, 9f, 12f)
        curveTo(9f, 8f, 15f, 8f, 15f, 12f); close()
    }
    val PersonOutline = outline("Profile") {
        moveTo(16f, 7f); curveTo(16f, 12.3f, 8f, 12.3f, 8f, 7f)
        curveTo(8f, 1.7f, 16f, 1.7f, 16f, 7f); close()
        moveTo(4f, 21f); curveTo(4f, 11f, 20f, 11f, 20f, 21f)
    }
    val Notifications = outline("Notifications") {
        moveTo(5f, 17f); horizontalLineTo(19f); lineTo(17f, 14f); verticalLineTo(9f)
        curveTo(17f, 2f, 7f, 2f, 7f, 9f); verticalLineTo(14f); close()
        moveTo(10f, 20f); quadTo(12f, 23f, 14f, 20f)
    }
    val Check = outline("Completed") {
        moveTo(5f, 12f); lineTo(10f, 17f); lineTo(19f, 7f)
    }
    val Schedule = outline("Due date") {
        moveTo(21f, 12f); curveTo(21f, 24f, 3f, 24f, 3f, 12f)
        curveTo(3f, 0f, 21f, 0f, 21f, 12f); close()
        moveTo(12f, 7f); verticalLineTo(12f); lineTo(15f, 15f)
    }
}
