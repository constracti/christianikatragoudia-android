package gr.christianikatragoudia.app.ui

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    @param:DrawableRes
    private val icon: Int,
    private val enabled: Boolean = true,
    private val onClick: () -> Unit
) {

    companion object {

        fun navigateBack(onClick: () -> Unit) = SongControl(
            text = R.string.back_button,
            icon = R.drawable.arrow_back,
            onClick = onClick,
        )

        fun star(currentStarred: Boolean, changeStarred: (Boolean) -> Unit) = SongControl(
            text = if (currentStarred) R.string.starred_remove else R.string.starred_add,
            icon = if (currentStarred)
                R.drawable.star_fill
            else
                R.drawable.star,
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
                icon = R.drawable.info,
            ) { visible = true }
        }

        @Composable
        fun openLink(song: Song): SongControl {
            val context = LocalContext.current
            return SongControl(
                text = R.string.link_open,
                icon = R.drawable.open_in_browser,
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
                icon = R.drawable.share,
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
            icon = R.drawable.playlist_remove,
            enabled = currentTonality != null,
        ) { changeTonality(null) }

        fun resetTonality(
            defaultTonality: MusicNote,
            currentTonality: MusicNote?,
            changeTonality: (MusicNote?) -> Unit,
        ) = SongControl(
            text = R.string.tonality_reset,
            icon = R.drawable.queue_music,
            enabled = currentTonality != defaultTonality,
        ) { changeTonality(defaultTonality) }

        fun decreaseScale(
            minimum: Float,
            factor: Float,
            current: Float,
            change: (Float) -> Unit,
        ) = SongControl(
            text = R.string.scale_decrease,
            icon = R.drawable.zoom_out,
            enabled = current > minimum,
        ) { change(current.div(factor).coerceAtLeast(minimum)) }

        fun increaseScale(
            maximum: Float,
            factor: Float,
            current: Float,
            change: (Float) -> Unit,
        ) = SongControl(
            text = R.string.scale_increase,
            icon = R.drawable.zoom_in,
            enabled = current < maximum,
        ) { change(current.times(factor).coerceAtMost(maximum)) }

        fun resetScale(
            default: Float,
            current: Float,
            change: (Float) -> Unit,
        ) = SongControl(
            text = R.string.scale_reset,
            icon = R.drawable.search_off,
            enabled = current != default,
        ) { change(default) }

        fun optimizeScale(
            optimal: Float, // coerced value
            current: Float,
            change: (Float) -> Unit,
        ): SongControl {
            return SongControl(
                text = R.string.scale_optimize,
                icon = R.drawable.saved_search,
                enabled = current != optimal,
            ) { change(optimal) }
        }

        fun scroll(isRunning: Boolean, isFinished: Boolean, onClick: () -> Unit) = SongControl(
            text = if (isRunning) R.string.pause else R.string.scroll,
            icon = if (isRunning)
                R.drawable.pause
            else
                R.drawable.play_arrow,
            enabled = isFinished.not(),
            onClick = onClick,
        )

        fun greatlyDecreaseSpeed(enabled: Boolean, onClick: () -> Unit) = SongControl(
            text = R.string.speed_decrease_greatly,
            icon = R.drawable.keyboard_double_arrow_down,
            enabled = enabled,
            onClick = onClick,
        )

        fun decreaseSpeed(enabled: Boolean, onClick: () -> Unit) = SongControl(
            text = R.string.speed_decrease,
            icon = R.drawable.keyboard_arrow_down,
            enabled = enabled,
            onClick = onClick,
        )

        fun increaseSpeed(enabled: Boolean, onClick: () -> Unit) = SongControl(
            text = R.string.speed_increase,
            icon = R.drawable.keyboard_arrow_up,
            enabled = enabled,
            onClick = onClick,
        )

        fun greatlyIncreaseSpeed(enabled: Boolean, onClick: () -> Unit) = SongControl(
            text = R.string.speed_increase_greatly,
            icon = R.drawable.keyboard_double_arrow_up,
            enabled = enabled,
            onClick = onClick,
        )

        fun resetSpeed(enabled: Boolean, onClick: () -> Unit) = SongControl(
            text = R.string.speed_reset,
            icon = R.drawable.speed_1x,
            enabled = enabled,
            onClick = onClick,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class) // TooltipBox
    @Composable
    fun AsIconButton(tooltipPositioning: TooltipAnchorPosition = TooltipAnchorPosition.Above) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = tooltipPositioning),
            tooltip = {
                PlainTooltip {
                    Text(text = stringResource(text), textAlign = TextAlign.Center)
                }
            },
            state = rememberTooltipState(),
        ) {
            IconButton(onClick = onClick, enabled = enabled) {
                Icon(painter = painterResource(icon), contentDescription = stringResource(text))
            }
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
            leadingIcon = {
                Icon(painter = painterResource(icon), contentDescription = stringResource(text))
            },
            enabled = enabled,
        )
    }
}
