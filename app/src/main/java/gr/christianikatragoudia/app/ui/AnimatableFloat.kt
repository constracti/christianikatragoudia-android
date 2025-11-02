package gr.christianikatragoudia.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


class AnimatableFloat(
    initialValue: Float,
    private var targetValue: Float,
    private val onValueUpdate: (Float) -> Unit,
    private val onToggle: (Boolean) -> Unit,
    private var speed: Float, // pixels per second
) {

    private val animatable = Animatable(initialValue = initialValue)

    val currentValue get() = animatable.value
    val isRunning get() = animatable.isRunning
    val isFinished get() = animatable.value == targetValue

    private suspend fun start() {
        onToggle(true)
        val duration = (targetValue - currentValue).absoluteValue.div(speed)
        val durationMillis = duration.times(1000).roundToInt()
        animatable.animateTo(
            targetValue = targetValue,
            animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing),
            block = { onValueUpdate(value) },
        )
        onToggle(false)
    }

    private suspend fun stop() {
        animatable.stop()
        onToggle(false)
    }

    suspend fun toggle() {
        if (isRunning)
            stop()
        else
            start()
    }

    suspend fun reconfigure(
        currentValue: Float,
        targetValue: Float,
        speed: Float,
    ) {
        val wasRunning = isRunning
        animatable.snapTo(currentValue)
        this.targetValue = targetValue
        this.speed = speed
        if (wasRunning)
            start()
    }
}
