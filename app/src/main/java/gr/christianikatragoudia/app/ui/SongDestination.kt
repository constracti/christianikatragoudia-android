package gr.christianikatragoudia.app.ui

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.text.Html
import android.text.style.StyleSpan
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.core.net.toUri
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
import kotlin.math.pow

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
                change = { viewModel.setTonality(it) },
            )
            val immerse = uiState.immerse
            val immerseChange: (Boolean) -> Unit = { viewModel.setImmerse(it) }
            val starAction = PainterAction.getStarAction(
                starred = songMeta.starred,
                starredChange = { viewModel.setStarred(it) },
            )
            val infoAction = VectorAction.getInfoAction(song = song)
            val openAction = VectorAction.getOpenAction(song = song)
            val shareAction = VectorAction.getShareAction(song = song)
            val topActionList = listOf(starAction, infoAction, openAction, shareAction)
            BackHandler(enabled = immerse) {
                immerseChange(false)
            }
            if (tonalityControl.selected == null) {
                val zoom = songMeta.zoom
                var pan by remember { mutableStateOf(Offset.Zero) }
                var outerSize by remember { mutableStateOf(IntSize.Zero) }
                var innerSize by remember { mutableStateOf(IntSize.Zero) }
                val focusControl = FocusControl(
                    zoom = zoom,
                    pan = pan,
                    outerSize = outerSize,
                    innerSize = innerSize,
                    innerScale = false,
                    zoomChange = { viewModel.setSongZoom(it) },
                    panChange = { pan = it },
                )
                Scaffold(
                    topBar = {
                        TheTopBar(
                            song = song,
                            starAction = starAction,
                            infoAction = infoAction,
                            openAction = openAction,
                            shareAction = shareAction,
                            navigateBack = navigateBack,
                            visible = !immerse,
                        )
                    },
                    bottomBar = {
                        TheBottomBar(
                            tonalityControl = tonalityControl,
                            focusControl = focusControl,
                            immerseChange = immerseChange,
                            visible = !immerse,
                        )
                    },
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    containerColor = Color.Transparent,
                ) { paddingValues ->
                    Row(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .transformable(rememberTransformableState { gestureZoom, gesturePan, _ ->
                                focusControl.change(
                                    newZoom = zoom * gestureZoom,
                                    newPan = pan + gesturePan,
                                )
                            })
                    ) {
                        SongBox(
                            song = song,
                            boxModifier = Modifier
                                .clipToBounds()
                                .fillMaxHeight()
                                .onSizeChanged { outerSize = it; focusControl.change() }
                                .weight(1f),
                            textModifier = Modifier
                                .wrapContentHeight(align = Alignment.Top, unbounded = true)
                                .graphicsLayer(translationY = pan.y)
                                .onSizeChanged { innerSize = it; focusControl.change() }
                                .padding(8.dp),
                            zoom = zoom,
                        )
                        TheSideBar(
                            topActionList = topActionList,
                            tonalityControl = tonalityControl,
                            focusControl = focusControl,
                            immerseChange = immerseChange,
                            visible = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE && immerse,
                        )
                    }
                }
            } else {
                val zoom by rememberUpdatedState(chordMeta.zoom)
                var pan by remember { mutableStateOf(Offset.Zero) }
                var outerSize by remember { mutableStateOf(IntSize.Zero) }
                var innerSize by remember { mutableStateOf(IntSize.Zero) }
                val focusControl = FocusControl(
                    zoom = zoom,
                    pan = pan,
                    outerSize = outerSize,
                    innerSize = innerSize,
                    innerScale = true,
                    zoomChange = { viewModel.setChordZoom(it) },
                    panChange = { pan = it },
                )
                Scaffold(
                    topBar = {
                        TheTopBar(
                            song = song,
                            starAction = starAction,
                            infoAction = infoAction,
                            openAction = openAction,
                            shareAction = shareAction,
                            navigateBack = navigateBack,
                            visible = !immerse,
                        )
                    },
                    bottomBar = {
                        TheBottomBar(
                            tonalityControl = tonalityControl,
                            focusControl = focusControl,
                            immerseChange = immerseChange,
                            visible = !immerse,
                        )
                    },
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    containerColor = Color.Transparent,
                ) { paddingValues ->
                    Row(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { centroid, gesturePan, gestureZoom, _ ->
                                    val newZoom = FocusControl.limitZoom(zoom * gestureZoom)
                                    val effZoom = newZoom / zoom
                                    focusControl.change(
                                        newZoom = newZoom,
                                        newPan = (pan - centroid) * effZoom + centroid + gesturePan,
                                    )
                                }
                            },
                    ) {
                        ChordBox(
                            chord = chord,
                            tonality = tonalityControl.selected,
                            boxModifier = Modifier
                                .clipToBounds()
                                .fillMaxHeight()
                                .onSizeChanged { outerSize = it; focusControl.change() }
                                .weight(1f),
                            textModifier = Modifier
                                .wrapContentSize(align = Alignment.TopStart, unbounded = true)
                                .graphicsLayer {
                                    translationX = pan.x
                                    translationY = pan.y
                                    scaleX = zoom
                                    scaleY = zoom
                                    transformOrigin = TransformOrigin(0f, 0f)
                                }
                                .onSizeChanged { innerSize = it; focusControl.change() }
                                .padding(8.dp),
                        )
                        TheSideBar(
                            topActionList = topActionList,
                            tonalityControl = tonalityControl,
                            focusControl = focusControl,
                            immerseChange = immerseChange,
                            visible = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE && immerse,
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TheTopBar(
        song: Song,
        starAction: Action,
        infoAction: Action,
        openAction: Action,
        shareAction: Action,
        navigateBack: () -> Unit,
        visible: Boolean,
    ) {
        if (visible) {
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
    }

    @Composable
    private fun SongBox(
        song: Song,
        boxModifier: Modifier,
        textModifier: Modifier,
        zoom: Float,
    ) {
        Box(modifier = boxModifier) {
            Column(modifier = textModifier) {
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
        }
    }

    @Composable
    private fun ChordBox(
        chord: Chord,
        tonality: MusicNote,
        boxModifier: Modifier,
        textModifier: Modifier,
    ) {
        Box(modifier = boxModifier) {
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
                text = text,
                modifier = textModifier,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            )
        }
    }

    @Composable
    private fun TheBottomBar(
        tonalityControl: TonalityControl,
        focusControl: FocusControl,
        immerseChange: (Boolean) -> Unit,
        visible: Boolean,
    ) {
        if (visible) {
            BottomAppBar(containerColor = Color.Transparent) {
                Spacer(modifier = Modifier.size(8.dp))
                tonalityControl.DropdownMenu()
                Spacer(modifier = Modifier.size(8.dp))
                Spacer(modifier = Modifier.weight(1F))
                focusControl.ZoomIconButtonList()
                PainterAction(
                    stringResource(R.string.full_screen_text),
                    painterResource(R.drawable.baseline_fullscreen_24),
                    true,
                ) {
                    immerseChange(true)
                }.AsIconButton()
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    val collapse: () -> Unit = { expanded = false }
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
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.tonality_hide_text)) },
                            onClick = {
                                collapse()
                                tonalityControl.change(null)
                            },
                            enabled = tonalityControl.selected != null,
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.tonality_reset_text)) },
                            onClick = {
                                collapse()
                                tonalityControl.change(tonalityControl.default)
                            },
                            enabled = tonalityControl.selected != tonalityControl.default,
                        )
                        focusControl.ResetZoomDropdownMenuItem(collapse)
                    }
                }
            }
        }
    }

    @Composable
    private fun TheSideBar(
        topActionList: List<Action>,
        tonalityControl: TonalityControl,
        focusControl: FocusControl,
        immerseChange: (Boolean) -> Unit,
        visible: Boolean,
    ) {
        if (visible) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Row {
                    topActionList.forEach { it.AsIconButton() }
                }
                Spacer(modifier = Modifier.weight(1F))
                tonalityControl.DropdownMenu()
                Row {
                    focusControl.ZoomIconButtonList()
                    PainterAction(
                        stringResource(R.string.full_screen_exit_text),
                        painterResource(R.drawable.baseline_fullscreen_exit_24),
                        true,
                    ) {
                        immerseChange(false)
                    }.AsIconButton()
                }
            }
        }
    }
}

private interface Action {

    @Composable
    fun AsIconButton()

    @Composable
    fun AsDropdownMenuItem(collapse: () -> Unit)
}

private data class VectorAction(
    private val text: String,
    private val vector: ImageVector,
    private val enabled: Boolean,
    private val action: () -> Unit,
) : Action {

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
            return VectorAction(text, icon, true) {
                visible = true
            }
        }

        @Composable
        fun getOpenAction(song: Song): VectorAction {
            val context = LocalContext.current
            val text = stringResource(R.string.open_link)
            val icon = Icons.AutoMirrored.Default.ExitToApp
            return VectorAction(text, icon, true) {
                val intent = Intent(Intent.ACTION_VIEW, song.permalink.toUri())
                context.startActivity(intent)
            }
        }

        @Composable
        fun getShareAction(song: Song): VectorAction {
            val context = LocalContext.current
            val text = stringResource(R.string.send_link)
            val icon = Icons.Default.Share
            return VectorAction(text, icon, true) {
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
            IconButton(onClick = action, enabled = enabled) {
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
            enabled = enabled,
        )
    }
}

private data class PainterAction(
    private val text: String,
    private val icon: Painter,
    private val enabled: Boolean,
    private val action: () -> Unit,
) : Action {

    companion object {

        @Composable
        fun getStarAction(starred: Boolean, starredChange: (Boolean) -> Unit): PainterAction {
            return if (starred) {
                val text = stringResource(R.string.starred_remove)
                val icon = painterResource(R.drawable.baseline_star_24)
                PainterAction(text, icon, true) {
                    starredChange(false)
                }
            } else {
                val text = stringResource(R.string.starred_add)
                val icon = painterResource(R.drawable.baseline_star_outline_24)
                PainterAction(text, icon, true) {
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
            IconButton(onClick = action, enabled = enabled) {
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
            enabled = enabled,
        )
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

private class FocusControl(
    private val zoom: Float,
    private val pan: Offset,
    private val outerSize: IntSize,
    private val innerSize: IntSize,
    private val innerScale: Boolean,
    private val zoomChange: (Float) -> Unit,
    private val panChange: (Offset) -> Unit,
) {

    companion object {

        val ZOOM_MIN = 2f.pow(-2)
        val ZOOM_MAX = 2f.pow(+2)
        val ZOOM_STEP = 2f.pow(0.1f)
        const val ZOOM_INIT = 1f

        fun limitZoom(zoom: Float): Float {
            return zoom.coerceIn(ZOOM_MIN, ZOOM_MAX)
        }
    }

    private fun decreaseZoomEnabled(): Boolean {
        return zoom > ZOOM_MIN
    }

    private fun increaseZoomEnabled(): Boolean {
        return zoom < ZOOM_MAX
    }

    private fun resetZoomEnabled(): Boolean {
        return zoom != ZOOM_INIT
    }

    private fun decreaseZoom() {
        change(zoom / ZOOM_STEP)
    }

    private fun increaseZoom() {
        change(zoom * ZOOM_STEP)
    }

    private fun resetZoom() {
        change(ZOOM_INIT)
    }

    @Composable
    fun ZoomIconButtonList() {
        PainterAction(
            stringResource(R.string.font_size_decrease_text),
            painterResource(R.drawable.baseline_text_decrease_24),
            decreaseZoomEnabled(),
        ) {
            decreaseZoom()
        }.AsIconButton()
        PainterAction(
            stringResource(R.string.font_size_increase_text),
            painterResource(R.drawable.baseline_text_increase_24),
            increaseZoomEnabled(),
        ) {
            increaseZoom()
        }.AsIconButton()
    }

    @Composable
    fun ResetZoomDropdownMenuItem(collapse: () -> Unit) {
        DropdownMenuItem(
            text = {
                Text(text = stringResource(R.string.font_size_reset_text))
            },
            onClick = {
                collapse()
                resetZoom()
            },
            enabled = resetZoomEnabled(),
        )
    }

    fun change(newZoom: Float? = null, newPan: Offset? = null) {
        val effZoom = limitZoom(newZoom ?: zoom)
        val outerOffset = outerSize.toOffset()
        val innerOffset = innerSize.toOffset() * (if (innerScale) effZoom else 1f)
        val minPan = (outerOffset - innerOffset).coerceAtMost(Offset.Zero)
        val maxPan = Offset.Zero
        val effPan = (newPan ?: pan).coerceIn(minPan, maxPan)
        zoomChange(effZoom)
        panChange(effPan)
    }
}
