package gr.christianikatragoudia.app.ui

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.text.Html
import android.text.style.StyleSpan
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.math.MathUtils.clamp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.Chord
import gr.christianikatragoudia.app.data.Song
import gr.christianikatragoudia.app.music.MusicInterval
import gr.christianikatragoudia.app.music.MusicNote
import gr.christianikatragoudia.app.nav.NavDestination
import gr.christianikatragoudia.app.network.TheAnalytics
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

object SongDestination : NavDestination {

    private val factory = viewModelFactory {
        initializer {
            SongViewModel(
                songId = this.createSavedStateHandle()[SONG_ID_ARG] ?: 0,
                application = this[APPLICATION_KEY] as TheApplication,
            )
        }
    }

    override val route = "song"
    const val SONG_ID_ARG = "songId"
    val routeWithArgs = "$route/{$SONG_ID_ARG}"

    @Composable
    fun TheScreen(
        navigateBack: () -> Unit,
        viewModel: SongViewModel = viewModel(factory = factory),
    ) {
        val uiState by viewModel.uiState.collectAsState()
        if (uiState.loading) {
            LoadingScreen(navigateBack = navigateBack)
        } else if (!uiState.passed) {
            ErrorScreen(navigateBack = navigateBack)
        } else {
            val song = uiState.song!!
            val chord = uiState.chord!!
            val songMeta = uiState.songMeta!!
            val chordMeta = uiState.chordMeta!!
            val hiddenTonalities by viewModel.hiddenTonalities.collectAsState(
                initial = MusicNote.ENHARMONIC_TONALITIES,
            )
            val tonalityControl = TonalityControl(
                selected = chordMeta.tonality,
                list = MusicNote.TONALITIES.filter {
                    !hiddenTonalities.contains(it)
                }.toMutableList<MusicNote?>().also {
                    it.add(0, null)
                },
                default = chord.tonality,
                change = {
                    viewModel.setTonality(it)
                },
            )
            TheScaffold(
                song = song,
                chord = chord,
                navigateBack = navigateBack,
                starred = songMeta.starred,
                starredChange = { viewModel.setStarred(it) },
                tonalityControl = tonalityControl,
                songZoom = songMeta.zoom,
                songZoomChange = { viewModel.setSongZoom(it) },
                chordZoom = chordMeta.zoom,
                chordZoomChange = { viewModel.setChordZoom(it) },
                expanded = uiState.expanded,
                expandedChange = { viewModel.setExpanded(it) },
            )
        }
    }
}

@Composable
private fun TheScaffold(
    song: Song,
    chord: Chord,
    navigateBack: () -> Unit,
    starred: Boolean,
    starredChange: (Boolean) -> Unit,
    tonalityControl: TonalityControl,
    songZoom: Int,
    songZoomChange: (Int) -> Unit,
    chordZoom: Int,
    chordZoomChange: (Int) -> Unit,
    expanded: Boolean,
    expandedChange: (Boolean) -> Unit,
) {
    val starAction = PainterAction.getStarAction(starred = starred, starredChange = starredChange)
    val infoAction = VectorAction.getInfoAction(song = song)
    val openAction = VectorAction.getOpenAction(song = song)
    val shareAction = VectorAction.getShareAction(song = song)
    BackHandler(enabled = expanded) {
        expandedChange(false)
    }
    Scaffold(
        topBar = {
            if (!expanded) {
                SongTopBar(
                    song = song,
                    navigateBack = navigateBack,
                    starAction = starAction,
                    infoAction = infoAction,
                    openAction = openAction,
                    shareAction = shareAction,
                )
            }
        },
        bottomBar = {
            if (!expanded) {
                SongBottomBar(
                    tonalityControl = tonalityControl,
                    zoom = if (tonalityControl.selected == null) songZoom else chordZoom,
                    zoomChange = if (tonalityControl.selected == null) songZoomChange else chordZoomChange,
                    expandedChange = expandedChange,
                )
            }
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) {
        val actions = listOf(starAction, infoAction, openAction, shareAction)
        val modifier = Modifier.padding(it).fillMaxSize().clipToBounds()
        if (tonalityControl.selected == null) {
            SongLyrics(
                song = song,
                modifier = modifier,
                size = songZoom,
                sizeChange = songZoomChange,
                expanded = expanded,
                actions = actions,
                tonalityControl = tonalityControl,
            )
        } else {
            SongChords(
                chord = chord,
                tonality = tonalityControl.selected,
                modifier = modifier,
                size = chordZoom,
                sizeChange = chordZoomChange,
                expanded = expanded,
                actions = actions,
                tonalityControl = tonalityControl,
            )
        }
    }
}

@Composable
private fun SongLyrics(
    song: Song,
    modifier: Modifier,
    size: Int,
    sizeChange: (Int) -> Unit,
    expanded: Boolean,
    actions: List<Action>,
    tonalityControl: TonalityControl,
) {
    var zoom by remember { mutableFloatStateOf(2f.pow(size / 10f)) }
    if (abs(10f * log2(zoom) - size) > .5f)
        zoom = 2f.pow(size / 10f)
    var pan by remember { mutableFloatStateOf(0f) }
    var outerHeight by remember { mutableIntStateOf(0) }
    var innerHeight by remember { mutableIntStateOf(0) }
    val state = rememberTransformableState { zoomDiff, panDiff, _ ->
        val minZoom = 2f.pow(-2)
        val maxZoom = 2f.pow(+2)
        zoom = clamp(zoom * zoomDiff, minZoom, maxZoom)
        val newSize = (10f * log2(zoom)).roundToInt()
        if (newSize != size)
            sizeChange(newSize)
        val minPan = minOf((outerHeight - innerHeight), 0).toFloat()
        val maxPan = 0f
        pan = clamp(pan + panDiff.y, minPan, maxPan)
    }
    Row(
        modifier = modifier
            .transformable(state = state)
            .onSizeChanged { outerHeight = it.height },
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight(align = Alignment.Top, unbounded = true)
                .graphicsLayer(translationY = pan)
                .onSizeChanged { innerHeight = it.height }
                .weight(1f)
                .padding(8.dp),
        ) {
            song.content.split("<hr />").forEachIndexed { index, s ->
                val source = s.replace("\n", "<br>")
                val spanned = Html.fromHtml(source, 0)
                val builder = AnnotatedString.Builder(spanned.toString())
                val spanArray = spanned.getSpans(0, spanned.length, StyleSpan::class.java)
                spanArray.forEach {
                    when (it.style) {
                        Typeface.ITALIC -> builder.addStyle(
                            style = SpanStyle(fontStyle = FontStyle.Italic),
                            start = spanned.getSpanStart(it),
                            end = spanned.getSpanEnd(it),
                        )
                    }
                }
                if (index > 0)
                    HorizontalDivider()
                Text(
                    text = builder.toAnnotatedString(),
                    fontSize = 16.sp * zoom,
                    lineHeight = 24.sp * zoom,
                )
            }
        }
        SongSidebar(
            actions = actions,
            tonalityControl = tonalityControl,
            visible = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE && expanded,
        )
    }
}

@Composable
private fun SongChords(
    chord: Chord,
    tonality: MusicNote,
    modifier: Modifier = Modifier,
    size: Int = 0,
    sizeChange: (Int) -> Unit = {},
    expanded: Boolean,
    actions: List<Action>,
    tonalityControl: TonalityControl,
) {
    var zoom by remember { mutableFloatStateOf(2f.pow(size / 10f)) }
    if (abs(10f * log2(zoom) - size) > .5f)
        zoom = 2f.pow(size / 10f)
    var pan by remember { mutableStateOf(Offset.Zero) }
    var outerSize by remember { mutableStateOf(IntSize.Zero) }
    var innerSize by remember { mutableStateOf(IntSize.Zero) }
    val state = rememberTransformableState { zoomDiff, panDiff, _ ->
        val minZoom = 2f.pow(-2)
        val maxZoom = 2f.pow(+2)
        zoom = clamp(zoom * zoomDiff, minZoom, maxZoom)
        val newSize = (10f * log2(zoom)).roundToInt()
        if (newSize != size)
            sizeChange(newSize)
        val minPanX = minOf(outerSize.width - innerSize.width, 0).toFloat()
        val minPanY = minOf(outerSize.height - innerSize.height, 0).toFloat()
        val maxPanX = 0f
        val maxPanY = 0f
        pan = Offset(
            x = clamp(pan.x + panDiff.x, minPanX, maxPanX),
            y = clamp(pan.y + panDiff.y, minPanY, maxPanY),
        )
    }
    Row(
        modifier = modifier
            .transformable(state = state)
            .onSizeChanged { outerSize = it },
    ) {
        val interval = MusicInterval.getByNotes(chord.tonality, tonality)
        val text = buildAnnotatedString {
            chord.content.lines().forEachIndexed { index, s ->
                if (index > 0)
                    append("\n")
                val isChordLine = s.isNotEmpty() &&
                        s.filter { char -> char.isWhitespace() }.length * 1F / s.length >= .5F
                if (isChordLine) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(interval.transposeLine(s))
                    }
                } else {
                    append(s)
                }
            }
        }
        Text(
            text,
            modifier = Modifier
                .wrapContentSize(align = Alignment.TopStart, unbounded = true)
                .graphicsLayer(translationX = pan.x, translationY = pan.y)
                .onSizeChanged { innerSize = it }
                .weight(1f)
                .padding(8.dp)
            ,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp * zoom,
            lineHeight = 24.sp * zoom,
        )
        SongSidebar(
            actions = actions,
            tonalityControl = tonalityControl,
            visible = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE && expanded,
        )
    }
}

@Composable
private fun SongSidebar(actions: List<Action>, tonalityControl: TonalityControl, visible: Boolean) {
    if (visible) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End,
        ) {
            Row {
                actions.forEach { action ->
                    action.AsIconButton()
                }
            }
            tonalityControl.DropdownMenu()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongTopBar(
    song: Song,
    navigateBack: () -> Unit,
    starAction: Action,
    infoAction: Action,
    openAction: Action,
    shareAction: Action,
) {
    TopAppBar(
        title = {
            Text(
                text = song.title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
            )
        },
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back_button),
                )
            }
        },
        actions = {
            val configuration = LocalConfiguration.current
            starAction.AsIconButton()
            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    infoAction.AsIconButton()
                    openAction.AsIconButton()
                    shareAction.AsIconButton()
            } else {
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    val collapse = { expanded = false }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more),
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = collapse,
                    ) {
                        infoAction.AsDropdownMenuItem(collapse)
                        openAction.AsDropdownMenuItem(collapse)
                        shareAction.AsDropdownMenuItem(collapse)
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
    )
}

private interface Action {

    @Composable
    fun AsIconButton()

    @Composable
    fun AsDropdownMenuItem(collapse: () -> Unit)
}

private data class VectorAction(val text: String, val vector: ImageVector, val action: () -> Unit) : Action {

    companion object {

        @Composable
        fun getInfoAction(song: Song): VectorAction {
            val text = stringResource(R.string.information)
            val icon = Icons.Default.Info
            var visible by remember { mutableStateOf(false) }
            if (visible) {
                AlertDialog(
                    onDismissRequest = { visible = false },
                    confirmButton = {
                        TextButton(onClick = { visible = false }) {
                            Text(text = stringResource(R.string.close))
                        }
                    },
                    title = { Text(text = song.title, modifier = Modifier.fillMaxWidth()) },
                    text = { Text(text = song.excerpt) },
                )
            }
            return VectorAction(text, icon) {
                visible = true
            }
        }

        @Composable
        fun getOpenAction(song: Song): VectorAction {
            val context = LocalContext.current
            val text = stringResource(R.string.open_link)
            val icon = Icons.AutoMirrored.Default.ExitToApp
            return VectorAction(text, icon) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.permalink))
                context.startActivity(intent)
            }
        }

        @Composable
        fun getShareAction(song: Song): VectorAction {
            val context = LocalContext.current
            val text = stringResource(R.string.send_link)
            val icon = Icons.Default.Share
            return VectorAction(text, icon) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, song.title)
                    putExtra(Intent.EXTRA_TEXT, song.permalink)
                }
                context.startActivity(Intent.createChooser(intent, text))
                TheAnalytics.logShare("url", song.permalink)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun AsIconButton() {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(text = text, textAlign = TextAlign.Center) } },
            state = rememberTooltipState(),
        ) {
            IconButton(onClick = action) {
                Icon(imageVector = vector, contentDescription = text)
            }
        }
    }

    @Composable
    override fun AsDropdownMenuItem(collapse: () -> Unit) {
        DropdownMenuItem(
            text = {
                Text(text = text)
            },
            onClick = {
                collapse()
                action()
            },
            leadingIcon = {
                Icon(imageVector = vector, contentDescription = null)
            },
        )
    }
}

private data class PainterAction(val text: String, val icon: Painter, val action: () -> Unit) : Action {

    companion object {

        @Composable
        fun getStarAction(starred: Boolean, starredChange: (Boolean) -> Unit): PainterAction {
            return if (starred) {
                val text = stringResource(R.string.starred_remove)
                val icon = painterResource(R.drawable.baseline_star_24)
                PainterAction(text, icon) {
                    starredChange(false)
                }
            } else {
                val text = stringResource(R.string.starred_add)
                val icon = painterResource(R.drawable.baseline_star_outline_24)
                PainterAction(text, icon) {
                    starredChange(true)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun AsIconButton() {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(text = text, textAlign = TextAlign.Center) } },
            state = rememberTooltipState(),
        ) {
            IconButton(onClick = action) {
                Icon(painter = icon, contentDescription = text)
            }
        }
    }

    @Composable
    override fun AsDropdownMenuItem(collapse: () -> Unit) {
        DropdownMenuItem(
            text = {
                Text(text = text)
            },
            onClick = {
                collapse()
                action()
            },
            leadingIcon = {
                Icon(painter = icon, contentDescription = null)
            },
        )
    }
}

@Composable
private fun SongBottomBar(
    tonalityControl: TonalityControl,
    zoom: Int,
    zoomChange: (Int) -> Unit,
    expandedChange: (Boolean) -> Unit,
) {
    BottomAppBar(containerColor = Color.Transparent) {
        Spacer(modifier = Modifier.size(8.dp))
        tonalityControl.DropdownMenu()
        Spacer(modifier = Modifier.size(8.dp))
        Spacer(modifier = Modifier.weight(1F))
        PainterAction(
            stringResource(R.string.full_screen_text),
            painterResource(R.drawable.baseline_fullscreen_24),
        ) {
            expandedChange(true)
        }.AsIconButton()
        Box {
            var expanded by remember { mutableStateOf(false) }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.tonality_hide_text)) },
                    onClick = {
                        expanded = false
                        tonalityControl.change(null)
                    },
                    enabled = tonalityControl.selected != null,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.tonality_reset_text)) },
                    onClick = {
                        expanded = false
                        tonalityControl.change(tonalityControl.default)
                    },
                    enabled = tonalityControl.selected != tonalityControl.default,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.font_size_reset_text)) },
                    onClick = {
                        expanded = false
                        if (zoom != 0)
                            zoomChange(0)
                    },
                    enabled = zoom != 0,
                )
            }
        }
    }
}

private data class TonalityControl(
    val list: List<MusicNote?>,
    val selected: MusicNote?,
    val default: MusicNote,
    val change: (MusicNote?) -> Unit,
) {

    @Composable
    fun DropdownMenu() {
        Box {
            var expanded by remember { mutableStateOf(false) }
            FilterChip(
                selected = selected != null,
                onClick = { expanded = !expanded },
                label = {
                    val label = stringResource(R.string.tonality_label)
                    val chord = MusicNote.toNotationOrNull(selected)
                    Text(text = if (chord != null) "$label: $chord" else label)
                },
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                list.forEach {
                    val lyricsNotation = stringResource(MusicNote.NOTATION_LYRICS)
                    DropdownMenuItem(
                        text = {
                            val tonalityNotation = MusicNote.toNotationOrNull(it)
                            val defaultLabel = stringResource(R.string.tonality_default_text)
                            val text = if (tonalityNotation == null)
                                lyricsNotation
                            else if (it == default)
                                "$tonalityNotation $defaultLabel"
                            else
                                tonalityNotation
                            Text(text = text)
                        },
                        onClick = {
                            expanded = false
                            change(it)
                        },
                        trailingIcon = {
                            if (it == selected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }
    }
}
