package gr.christianikatragoudia.app.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize


sealed interface SongChordsFocusControl {

    companion object {

        val maxOffset = Offset.Zero
    }

    val isReady: Boolean

    val optimalScale: Float?

    val animatable: AnimatableFloat?

    val offset: Offset

    suspend fun reconfigure(
        scale: Float? = null,
        initialOffset: Offset? = null,
        linesPerMinute: Float? = null,
        outerSize: IntSize? = null,
        innerSize: IntSize? = null,
        lineCount: Int? = null,
    ): SongChordsFocusControl

    data class Start(
        private val scale: Float,
        private val initialOffset: Offset,
        private val changeOffset: (Offset) -> Unit,
        private val changeScrolling: (Boolean) -> Unit,
        private val linesPerMinute: Float,
        private val outerSize: IntSize? = null,
        private val innerSize: IntSize? = null,
        private val lineCount: Int? = null,
    ) : SongChordsFocusControl {

        override val isReady = false

        override val optimalScale = null

        override val animatable = null

        override val offset = maxOffset

        override suspend fun reconfigure(
            scale: Float?,
            initialOffset: Offset?,
            linesPerMinute: Float?,
            outerSize: IntSize?,
            innerSize: IntSize?,
            lineCount: Int?,
        ): SongChordsFocusControl {
            return copy(
                scale = scale ?: this.scale,
                initialOffset = initialOffset ?: this.initialOffset,
                linesPerMinute = linesPerMinute ?: this.linesPerMinute,
                outerSize = outerSize ?: this.outerSize,
                innerSize = innerSize ?: this.innerSize,
                lineCount = lineCount ?: this.lineCount,
            ).promote()
        }

        private fun promote(): SongChordsFocusControl {
            return if (outerSize != null && innerSize != null && lineCount != null)
                ReadyWrapper(ReadyKernel(
                    scale = scale,
                    initialOffset = initialOffset,
                    changeOffset = changeOffset,
                    changeScrolling = changeScrolling,
                    linesPerMinute = linesPerMinute,
                    outerSize = outerSize,
                    innerSize = innerSize,
                    lineCount = lineCount,
                ))
            else
                this
        }
    }

    private class ReadyKernel(
        var scale: Float,
        var initialOffset: Offset,
        changeOffset: (Offset) -> Unit,
        changeScrolling: (Boolean) -> Unit,
        var linesPerMinute: Float,
        var outerSize: IntSize,
        var innerSize: IntSize,
        var lineCount: Int,
    ) {

        private val minOffset get() = Offset(
            x = outerSize.width.toFloat() - innerSize.width.toFloat() * scale,
            y = outerSize.height.toFloat() - innerSize.height.toFloat() * scale,
        ).minus(maxOffset).coerceAtMost(maxOffset)

        val optimalScale get() = outerSize.width.toFloat() / innerSize.width.toFloat()

        private val coercedOffset get() = initialOffset.coerceIn(minOffset, maxOffset)

        private val pixelsPerLine get() = innerSize.height * scale / lineCount
        private val pixelsPerSecond get() = linesPerMinute * pixelsPerLine / 60

        val animatable = AnimatableFloat(
            initialValue = coercedOffset.y,
            targetValue = minOffset.y,
            onValueUpdate = { changeOffset(coercedOffset.copy(y = it)) },
            onToggle = changeScrolling,
            speed = pixelsPerSecond,
        )

        val offset get() = coercedOffset.copy(y = animatable.currentValue)

        suspend fun reconfigure(
            scale: Float?,
            initialOffset: Offset?,
            linesPerMinute: Float?,
            outerSize: IntSize?,
            innerSize: IntSize?,
            lineCount: Int?,
        ) {
            if (scale != null)
                this.scale = scale
            if (linesPerMinute != null)
                this.linesPerMinute = linesPerMinute
            if (outerSize != null)
                this.outerSize = outerSize
            if (innerSize != null)
                this.innerSize = innerSize
            if (lineCount != null)
                this.lineCount = lineCount
            this.initialOffset = (initialOffset ?: offset).coerceIn(minOffset, maxOffset)
            animatable.reconfigure(
                currentValue = this.initialOffset.y,
                targetValue = minOffset.y,
                speed = pixelsPerSecond,
            )
        }
    }

    private data class ReadyWrapper(
        private val kernel: ReadyKernel,
        private val tag: Boolean = false,
    ) : SongChordsFocusControl {

        override val isReady = true

        override val optimalScale get() = kernel.optimalScale

        override val animatable = kernel.animatable

        override val offset get() = kernel.offset

        override suspend fun reconfigure(
            scale: Float?,
            initialOffset: Offset?,
            linesPerMinute: Float?,
            outerSize: IntSize?,
            innerSize: IntSize?,
            lineCount: Int?,
        ): SongChordsFocusControl {
            kernel.reconfigure(
                scale = scale,
                initialOffset = initialOffset,
                linesPerMinute = linesPerMinute,
                outerSize = outerSize,
                innerSize = innerSize,
                lineCount = lineCount,
            )
            return copy(kernel = kernel, tag = tag.not())
        }
    }
}


private fun Offset.coerceAtMost(maximum: Offset): Offset {
    return Offset(x.coerceAtMost(maximum.x), y.coerceAtMost(maximum.y))
}

private fun Offset.coerceIn(minimum: Offset, maximum: Offset): Offset {
    return Offset(x.coerceIn(minimum.x, maximum.x), y.coerceIn(minimum.y, maximum.y))
}
