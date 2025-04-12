package gr.christianikatragoudia.app.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

fun IntSize.toOffset(): Offset {
    return Offset(width.toFloat(), height.toFloat())
}

fun Offset.coerceIn(min: Offset, max: Offset): Offset {
    return Offset(x.coerceIn(min.x, max.x), y.coerceIn(min.y, max.y))
}

fun Offset.coerceAtMost(max: Offset): Offset {
    return coerceIn(-Offset.Infinite, max)
}
