package gr.christianikatragoudia.app.ui

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.data.Song
import gr.christianikatragoudia.app.music.MusicNote
import gr.christianikatragoudia.app.network.TheAnalytics


class SongControl(
    @param:StringRes
    private val text: Int,
    private val graphic: Graphic,
    private val enabled: Boolean = true,
    private val onClick: () -> Unit
) {

    interface Graphic {

        @Composable
        fun Content(@StringRes text: Int)

        class PainterGraphic(@param:DrawableRes val icon: Int) : Graphic {

            @Composable
            override fun Content(@StringRes text: Int) {
                Icon(painter = painterResource(icon), contentDescription = stringResource(text))
            }
        }

        class VectorGraphic(val icon: ImageVector) : Graphic {

            @Composable
            override fun Content(@StringRes text: Int) {
                Icon(imageVector = icon, contentDescription = stringResource(text))
            }
        }
    }

    companion object {

        fun navigateBack(onClick: () -> Unit) = SongControl(
            text = R.string.back_button,
            graphic = Graphic.VectorGraphic(Icons.AutoMirrored.Default.ArrowBack),
            onClick = onClick,
        )

        fun star(currentStarred: Boolean, changeStarred: (Boolean) -> Unit) = SongControl(
            text = if (currentStarred) R.string.starred_remove else R.string.starred_add,
            graphic = if (currentStarred)
                Graphic.PainterGraphic(R.drawable.baseline_star_24)
            else
                Graphic.PainterGraphic(R.drawable.baseline_star_outline_24),
        ) { changeStarred(currentStarred.not()) }

        @Composable
        fun showInfo(song: Song): SongControl {
            var visible by remember { mutableStateOf(false) }
            if (visible) {
                AlertDialog(
                    onDismissRequest = { visible = false },
                    confirmButton = {
                        TextButton(onClick = { visible = false }) {
                            Text(text = stringResource(R.string.close))
                        }
                    },
                    title = { Text(text = song.title) },
                    text = { Text(text = song.excerpt) },
                )
            }
            return SongControl(
                text = R.string.information,
                graphic = Graphic.VectorGraphic(Icons.Default.Info),
            ) { visible = true }
        }

        @Composable
        fun openLink(song: Song): SongControl {
            val context = LocalContext.current
            return SongControl(
                text = R.string.link_open,
                graphic = Graphic.PainterGraphic(R.drawable.baseline_open_in_browser_24),
            ) {
                val intent = Intent(Intent.ACTION_VIEW, song.permalink.toUri())
                context.startActivity(intent)
            }
        }

        @Composable
        fun sendLink(song: Song): SongControl {
            val context = LocalContext.current
            return SongControl(
                text = R.string.link_send,
                graphic = Graphic.VectorGraphic(Icons.Default.Share),
            ) {
                // TODO enrich, see https://developer.android.com/training/sharing/send
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, song.title)
                    putExtra(Intent.EXTRA_TEXT, song.permalink)
                }
                context.startActivity(Intent.createChooser(intent, null))
                TheAnalytics.logShare("url", song.permalink)
            }
        }

        fun hideTonality(
            currentTonality: MusicNote?,
            changeTonality: (MusicNote?) -> Unit,
        ) = SongControl(
            text = R.string.tonality_hide,
            graphic = Graphic.PainterGraphic(R.drawable.baseline_notes_24),
            enabled = currentTonality != null,
        ) { changeTonality(null) }

        fun resetTonality(
            defaultTonality: MusicNote,
            currentTonality: MusicNote?,
            changeTonality: (MusicNote?) -> Unit,
        ) = SongControl(
            text = R.string.tonality_reset,
            graphic = Graphic.PainterGraphic(R.drawable.baseline_queue_music_24),
            enabled = currentTonality != defaultTonality,
        ) { changeTonality(defaultTonality) }

        fun decreaseScale(
            minimum: Float,
            factor: Float,
            current: Float,
            change: (Float) -> Unit,
        ) = SongControl(
            text = R.string.scale_decrease,
            graphic = Graphic.PainterGraphic(R.drawable.baseline_zoom_out_24),
            enabled = current > minimum,
        ) { change(current.div(factor).coerceAtLeast(minimum)) }

        fun increaseScale(
            maximum: Float,
            factor: Float,
            current: Float,
            change: (Float) -> Unit,
        ) = SongControl(
            text = R.string.scale_increase,
            graphic = Graphic.PainterGraphic(R.drawable.baseline_zoom_in_24),
            enabled = current < maximum,
        ) { change(current.times(factor).coerceAtMost(maximum)) }

        fun resetScale(
            default: Float,
            current: Float,
            change: (Float) -> Unit,
        ) = SongControl(
            text = R.string.scale_reset,
            graphic = Graphic.PainterGraphic(R.drawable.baseline_search_off_24),
            enabled = current != default,
        ) { change(default) }

        fun optimizeScale(
            optimal: Float, // coerced value
            current: Float,
            change: (Float) -> Unit,
        ): SongControl {
            return SongControl(
                text = R.string.scale_optimize,
                graphic = Graphic.PainterGraphic(R.drawable.baseline_saved_search_24),
                enabled = current != optimal,
            ) { change(optimal) }
        }

        fun scroll(isRunning: Boolean, isFinished: Boolean, onClick: () -> Unit) = SongControl(
            text = if (isRunning) R.string.pause else R.string.scroll,
            graphic = if (isRunning)
                Graphic.PainterGraphic(R.drawable.baseline_pause_24)
            else
                Graphic.PainterGraphic(R.drawable.baseline_play_arrow_24),
            enabled = isFinished.not(),
            onClick = onClick,
        )

        fun greatlyDecreaseSpeed(enabled: Boolean, onClick: () -> Unit) = SongControl(
            text = R.string.speed_decrease_greatly,
            graphic = Graphic.PainterGraphic(R.drawable.baseline_keyboard_double_arrow_down_24),
            enabled = enabled,
            onClick = onClick,
        )

        fun decreaseSpeed(enabled: Boolean, onClick: () -> Unit) = SongControl(
            text = R.string.speed_decrease,
            graphic = Graphic.PainterGraphic(R.drawable.baseline_keyboard_arrow_down_24),
            enabled = enabled,
            onClick = onClick,
        )

        fun increaseSpeed(enabled: Boolean, onClick: () -> Unit) = SongControl(
            text = R.string.speed_increase,
            graphic = Graphic.PainterGraphic(R.drawable.baseline_keyboard_arrow_up_24),
            enabled = enabled,
            onClick = onClick,
        )

        fun greatlyIncreaseSpeed(enabled: Boolean, onClick: () -> Unit) = SongControl(
            text = R.string.speed_increase_greatly,
            graphic = Graphic.PainterGraphic(R.drawable.baseline_keyboard_double_arrow_up_24),
            enabled = enabled,
            onClick = onClick,
        )

        fun resetSpeed(enabled: Boolean, onClick: () -> Unit) = SongControl(
            text = R.string.speed_reset,
            graphic = Graphic.PainterGraphic(R.drawable.baseline_1x_mobiledata_24),
            enabled = enabled,
            onClick = onClick,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class) // TooltipBox
    @Composable
    fun AsIconButton() {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Text(text = stringResource(text), textAlign = TextAlign.Center)
                }
            },
            state = rememberTooltipState(),
        ) {
            IconButton(onClick = onClick, enabled = enabled) { graphic.Content(text = text) }
        }
    }

    @Composable
    fun AsDropdownMenuItem(collapse: () -> Unit) {
        DropdownMenuItem(
            text = { Text(text = stringResource(text)) },
            onClick = {
                onClick()
                collapse()
            },
            leadingIcon = { graphic.Content(text = text) },
            enabled = enabled,
        )
    }
}
