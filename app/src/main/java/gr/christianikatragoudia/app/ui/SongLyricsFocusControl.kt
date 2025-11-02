package gr.christianikatragoudia.app.ui

import androidx.compose.ui.unit.IntSize


sealed interface SongLyricsFocusControl {

    companion object {

        const val MAX_OFFSET = 0f
    }

    val offset: Float

    fun reconfigure(
        initialOffset: Float? = null,
        outerSize: IntSize? = null,
        innerSize: IntSize? = null,
    ): SongLyricsFocusControl

    data class Start(
        private val initialOffset: Float,
        private val outerSize: IntSize? = null,
        private val innerSize: IntSize? = null,
    ) : SongLyricsFocusControl {

        override val offset = MAX_OFFSET

        override fun reconfigure(
            initialOffset: Float?,
            outerSize: IntSize?,
            innerSize: IntSize?
        ): SongLyricsFocusControl {
            return copy(
                initialOffset = initialOffset ?: this.initialOffset,
                outerSize = outerSize ?: this.outerSize,
                innerSize = innerSize ?: this.innerSize,
            ).promote()
        }

        private fun promote(): SongLyricsFocusControl {
            return if (outerSize != null && innerSize != null)
                Ready(
                    initialOffset = initialOffset,
                    outerSize = outerSize,
                    innerSize = innerSize,
                )
            else
                this
        }
    }

    private data class Ready(
        private val initialOffset: Float,
        private val outerSize: IntSize,
        private val innerSize: IntSize,
    ) : SongLyricsFocusControl {

        private val minOffset = (outerSize.height.toFloat() - innerSize.height.toFloat())
            .minus(MAX_OFFSET).coerceAtMost(MAX_OFFSET)

        override val offset = initialOffset.coerceIn(minOffset, MAX_OFFSET)

        override fun reconfigure(
            initialOffset: Float?,
            outerSize: IntSize?,
            innerSize: IntSize?
        ): SongLyricsFocusControl {
            return copy(
                initialOffset = initialOffset ?: this.initialOffset,
                outerSize = outerSize ?: this.outerSize,
                innerSize = innerSize ?: this.innerSize,
            )
        }
    }
}
