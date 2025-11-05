package gr.christianikatragoudia.app.ui

import android.content.res.Configuration
import android.graphics.Typeface
import android.text.Html
import android.text.style.StyleSpan
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gr.christianikatragoudia.app.Demo
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.Chord
import gr.christianikatragoudia.app.data.ChordMeta
import gr.christianikatragoudia.app.data.Song
import gr.christianikatragoudia.app.data.SongMeta
import gr.christianikatragoudia.app.music.MusicInterval
import gr.christianikatragoudia.app.music.MusicNote
import gr.christianikatragoudia.app.nav.NavDestination
import gr.christianikatragoudia.app.ui.theme.ChristianikaTragoudiaTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
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
        val destState by viewModel.state.collectAsState()
        val hiddenTonalities by viewModel.hiddenTonalities.collectAsState(
            initial = MusicNote.ENHARMONIC_TONALITIES,
        )
        when (destState) {
            is SongViewModel.State.StartState -> StartScreen(navigateBack = navigateBack)
            is SongViewModel.State.ReadyState -> (destState as SongViewModel.State.ReadyState).let {
                readyState ->
                if (readyState.chordMeta.tonality == null) {
                    LyricsScreen(
                        song = readyState.song,
                        currentStarred = readyState.songMeta.starred,
                        changeStarred = { viewModel.setStarred(it) },
                        hiddenTonalities = hiddenTonalities,
                        defaultTonality = readyState.chord.tonality,
                        changeTonality = { viewModel.setTonality(it) },
                        initialScale = readyState.songMeta.scale,
                        changeScale = { viewModel.setSongScale(it) },
                        initialOffset = readyState.lyricsOffset,
                        changeOffset = { viewModel.setLyricsOffset(it) },
                        navigateBack = navigateBack,
                    )
                } else {
                    ChordsScreen(
                        song = readyState.song,
                        chord = readyState.chord,
                        currentStarred = readyState.songMeta.starred,
                        changeStarred = { viewModel.setStarred(it) },
                        hiddenTonalities = hiddenTonalities,
                        currentTonality = readyState.chordMeta.tonality,
                        changeTonality = { viewModel.setTonality(it) },
                        initialScale = readyState.chordMeta.scale,
                        changeScale = { viewModel.setChordScale(it) },
                        initialOffset = readyState.chordsOffset,
                        changeOffset = { viewModel.setChordsOffset(it) },
                        initialScrolling = readyState.chordsScrolling,
                        changeScrolling = { viewModel.setChordsScrolling(it) },
                        currentSpeeding = readyState.chordsSpeeding,
                        changeSpeeding = { viewModel.setChordsSpeeding(it) },
                        initialSpeed = readyState.chordMeta.speed ?: readyState.chord.speed ?:
                            SongViewModel.defaultSpeed,
                        changeSpeed = { viewModel.setSpeed(it) },
                        navigateBack = navigateBack,
                    )
                }
            }
        }
    }
}

@Composable
private fun StartScreen(navigateBack: () -> Unit) {
    LoadingScreen(navigateBack = navigateBack)
}

@Composable
private fun LyricsScreen(
    song: Song,
    currentStarred: Boolean,
    changeStarred: (Boolean) -> Unit,
    hiddenTonalities: Set<MusicNote>,
    defaultTonality: MusicNote,
    changeTonality: (MusicNote?) -> Unit,
    initialScale: Float,
    changeScale: (Float) -> Unit,
    initialOffset: Float,
    changeOffset: (Float) -> Unit,
    navigateBack: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val navigateBackControl = SongControl.navigateBack(onClick = navigateBack)
    val starControl = SongControl.star(
        currentStarred = currentStarred,
        changeStarred = changeStarred,
    )
    Scaffold(
        topBar = {
            if (configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                TopBar(
                    song = song,
                    starControl = starControl,
                    navigateBackControl = navigateBackControl,
                )
            }
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) { paddingValues ->
        var scale by remember { mutableFloatStateOf(initialScale) }
        var focusControl by remember {
            mutableStateOf(SongLyricsFocusControl.Start(
                initialOffset = initialOffset,
            ) as SongLyricsFocusControl)
        }
        val containerModifier = Modifier
            .padding(paddingValues = paddingValues)
            .padding(all = dimensionResource(R.dimen.spacing))
        val boxModifier = Modifier
            .onSizeChanged { focusControl = focusControl.reconfigure(outerSize = it) }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    val oldScale = scale
                    val newScale = oldScale.times(zoom)
                        .coerceIn(SongMeta.minScale, SongMeta.maxScale)
                    val effectiveZoom = newScale / oldScale
                    val oldOffset = focusControl.offset
                    val newOffset = (oldOffset - centroid.y) * effectiveZoom + centroid.y + pan.y
                    changeScale(newScale)
                    changeOffset(newOffset)
                    scale = newScale
                    focusControl = focusControl.reconfigure(initialOffset = newOffset)
                }
            }
            .clipToBounds()
        val textModifier = Modifier
            .wrapContentHeight(align = Alignment.Top, unbounded = true)
            .onSizeChanged { focusControl = focusControl.reconfigure(innerSize = it) }
            .graphicsLayer(translationY = focusControl.offset)
        val showInfoControl = SongControl.showInfo(song = song)
        val openLinkControl = SongControl.openLink(song = song)
        val sendLinkControl = SongControl.sendLink(song = song)
        val hideTonalityControl = SongControl.hideTonality(
            currentTonality = null,
            changeTonality = changeTonality,
        )
        val resetTonalityControl = SongControl.resetTonality(
            defaultTonality = defaultTonality,
            currentTonality = null,
            changeTonality = changeTonality,
        )
        val onScaleButtonClick: (Float) -> Unit = { newScale ->
            // new scale is already coerced
            val oldScale = scale
            val zoom = newScale / oldScale
            val oldOffset = focusControl.offset
            val newOffset = oldOffset * zoom
            // new offset is coerced
            changeScale(newScale)
            changeOffset(newOffset)
            scale = newScale
            focusControl = focusControl.reconfigure(initialOffset = newOffset)
        }
        val decreaseScaleControl = SongControl.decreaseScale(
            minimum = SongMeta.minScale,
            factor = SongMeta.scaleStep,
            current = scale,
            change = onScaleButtonClick,
        )
        val increaseScaleControl = SongControl.increaseScale(
            maximum = SongMeta.maxScale,
            factor = SongMeta.scaleStep,
            current = scale,
            change = onScaleButtonClick,
        )
        val resetScaleControl = SongControl.resetScale(
            default = SongMeta.DEFAULT_SCALE,
            current = scale,
            change = onScaleButtonClick,
        )
        if (configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            Column(
                modifier = containerModifier,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing)),
            ) {
                Box(modifier = boxModifier.fillMaxWidth().weight(1f)) {
                    LyricsContent(content = song.content, scale = scale, modifier = textModifier)
                }
                Row {
                    TonalityMenu(
                        hiddenTonalities = hiddenTonalities,
                        defaultTonality = defaultTonality,
                        currentTonality = null,
                        changeTonality = changeTonality,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    decreaseScaleControl.AsIconButton()
                    increaseScaleControl.AsIconButton()
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        val collapse = { expanded = false }
                        IconButton(onClick = { expanded = expanded.not() }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more),
                            )
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = collapse) {
                            showInfoControl.AsDropdownMenuItem(collapse = collapse)
                            openLinkControl.AsDropdownMenuItem(collapse = collapse)
                            sendLinkControl.AsDropdownMenuItem(collapse = collapse)
                            hideTonalityControl.AsDropdownMenuItem(collapse = collapse)
                            resetTonalityControl.AsDropdownMenuItem(collapse = collapse)
                            resetScaleControl.AsDropdownMenuItem(collapse = collapse)
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = containerModifier,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing))
            ) {
                Box(modifier = boxModifier.fillMaxHeight().weight(1f)) {
                    LyricsContent(content = song.content, scale = scale, modifier = textModifier)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        navigateBackControl.AsIconButton()
                        starControl.AsIconButton()
                        showInfoControl.AsIconButton()
                        openLinkControl.AsIconButton()
                        sendLinkControl.AsIconButton()
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row {
                        decreaseScaleControl.AsIconButton()
                        increaseScaleControl.AsIconButton()
                        resetScaleControl.AsIconButton()
                    }
                    Row {
                        TonalityMenu(
                            hiddenTonalities = hiddenTonalities,
                            defaultTonality = defaultTonality,
                            currentTonality = null,
                            changeTonality = changeTonality,
                        )
                        hideTonalityControl.AsIconButton()
                        resetTonalityControl.AsIconButton()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChordsScreen(
    song: Song,
    chord: Chord,
    currentStarred: Boolean,
    changeStarred: (Boolean) -> Unit,
    hiddenTonalities: Set<MusicNote>,
    currentTonality: MusicNote,
    changeTonality: (MusicNote?) -> Unit,
    initialScale: Float,
    changeScale: (Float) -> Unit,
    initialOffset: Offset,
    changeOffset: (Offset) -> Unit,
    initialScrolling: Boolean,
    changeScrolling: (Boolean) -> Unit,
    currentSpeeding: Boolean,
    changeSpeeding: (Boolean) -> Unit,
    initialSpeed: Float,
    changeSpeed: (Float) -> Unit,
    navigateBack: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val navigateBackControl = SongControl.navigateBack(onClick = navigateBack)
    val starControl = SongControl.star(
        currentStarred = currentStarred,
        changeStarred = changeStarred,
    )
    Scaffold(
        topBar = {
            if (configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                TopBar(
                    song = song,
                    starControl = starControl,
                    navigateBackControl = navigateBackControl,
                )
            }
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) { paddingValues ->
        val coroutineScope = rememberCoroutineScope()
        var scale by remember { mutableFloatStateOf(initialScale) }
        var focusControl by remember {
            mutableStateOf(SongChordsFocusControl.Start(
                scale = initialScale,
                initialOffset = initialOffset,
                changeOffset = { changeOffset(it) },
                changeScrolling = { changeScrolling(it) },
                linesPerMinute = initialSpeed,
            ) as SongChordsFocusControl)
        }
        val interval = MusicInterval.getByNotes(chord.tonality, currentTonality)
        val text = buildAnnotatedString {
            append("\n".repeat(2)) // TODO how many lines?
            chord.content.lines().forEach { line ->
                val isChordLine = line.isNotEmpty() &&
                        line.count { it.isWhitespace() }.toFloat() / line.count() >= 0.5f
                if (isChordLine) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(interval.transposeLine(line))
                    }
                } else {
                    append(line)
                }
                append("\n")
            }
        }
        val containerModifier = Modifier
            .padding(paddingValues = paddingValues)
            .padding(all = dimensionResource(R.dimen.spacing))
        val boxModifier = Modifier
            .onSizeChanged {
                coroutineScope.launch {
                    val oldControl = focusControl
                    val newControl = focusControl.reconfigure(outerSize = it)
                    focusControl = newControl
                    if (initialScrolling && newControl.isReady != oldControl.isReady)
                        newControl.animatable?.toggle()
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    val oldScale = scale
                    val newScale = oldScale.times(zoom)
                        .coerceIn(ChordMeta.minScale, ChordMeta.maxScale)
                    val effectiveZoom = newScale / oldScale
                    val oldOffset = focusControl.offset
                    val newOffset = (oldOffset - centroid) * effectiveZoom + centroid + pan
                    changeScale(newScale)
                    changeOffset(newOffset)
                    scale = newScale
                    coroutineScope.launch {
                        focusControl = focusControl.reconfigure(
                            scale = newScale,
                            initialOffset = newOffset,
                        )
                    }
                }
            }
            .clipToBounds()
        val textModifier = Modifier
            .wrapContentSize(align = Alignment.TopStart, unbounded = true)
            .onSizeChanged {
                coroutineScope.launch {
                    val oldControl = focusControl
                    val newControl = focusControl.reconfigure(
                        innerSize = it,
                        lineCount = text.lines().size,
                    )
                    focusControl = newControl
                    if (initialScrolling && newControl.isReady != oldControl.isReady)
                        newControl.animatable?.toggle()
                }
            }
            .graphicsLayer {
                translationX = focusControl.offset.x
                translationY = focusControl.offset.y
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        val showInfoControl = SongControl.showInfo(song = song)
        val openLinkControl = SongControl.openLink(song = song)
        val sendLinkControl = SongControl.sendLink(song = song)
        val hideTonalityControl = SongControl.hideTonality(
            currentTonality = currentTonality,
            changeTonality = changeTonality,
        )
        val resetTonalityControl = SongControl.resetTonality(
            defaultTonality = chord.tonality,
            currentTonality = currentTonality,
            changeTonality = changeTonality,
        )
        val onScaleButtonClick: (Float) -> Unit = { newScale ->
            // new scale is already coerced
            val oldScale = scale
            val zoom = newScale / oldScale
            val oldOffset = focusControl.offset
            val newOffset = oldOffset * zoom
            // new offset is coerced
            changeScale(newScale)
            changeOffset(newOffset)
            scale = newScale
            coroutineScope.launch {
                focusControl = focusControl.reconfigure(
                    scale = newScale,
                    initialOffset = newOffset,
                )
            }
        }
        val decreaseScaleControl = SongControl.decreaseScale(
            minimum = ChordMeta.minScale,
            factor = ChordMeta.scaleStep,
            current = scale,
            change = onScaleButtonClick,
        )
        val increaseScaleControl = SongControl.increaseScale(
            maximum = ChordMeta.maxScale,
            factor = ChordMeta.scaleStep,
            current = scale,
            change = onScaleButtonClick,
        )
        val resetScaleControl = SongControl.resetScale(
            default = ChordMeta.DEFAULT_SCALE,
            current = scale,
            change = onScaleButtonClick,
        )
        val optimizeScaleControl = focusControl.optimalScale?.let { optimalScale ->
            SongControl.optimizeScale(
                optimal = optimalScale.coerceIn(ChordMeta.minScale, ChordMeta.maxScale),
                current = scale,
                change = onScaleButtonClick,
            )
        }
        val scrollControl = focusControl.animatable?.let { animatable ->
            SongControl.scroll(
                isRunning = animatable.isRunning,
                isFinished = animatable.isFinished,
            ) {
                coroutineScope.launch {
                    animatable.toggle()
                }
            }
        }
        if (configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            Column(
                modifier = containerModifier,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = boxModifier.fillMaxWidth().weight(1f)) {
                    Text(
                        text = text,
                        modifier = textModifier,
                        fontSize = integerResource(R.integer.song_font_size).sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = integerResource(R.integer.song_line_height).sp,
                    )
                }
                if (currentSpeeding) {
                    SpeedRow(
                        initialSpeed = initialSpeed,
                        changeSpeed = {
                            changeSpeed(it)
                            coroutineScope.launch {
                                focusControl.reconfigure(linesPerMinute = it)
                            }
                        },
                    )
                }
                Row {
                    TonalityMenu(
                        hiddenTonalities = hiddenTonalities,
                        defaultTonality = chord.tonality,
                        currentTonality = currentTonality,
                        changeTonality = changeTonality,
                    )
                    scrollControl?.AsIconButton()
                    AdjustSpeedToggleButton(
                        currentSpeeding = currentSpeeding,
                        changeSpeeding = changeSpeeding,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    decreaseScaleControl.AsIconButton()
                    increaseScaleControl.AsIconButton()
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        val collapse = { expanded = false }
                        IconButton(onClick = { expanded = expanded.not() }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more),
                            )
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = collapse) {
                            showInfoControl.AsDropdownMenuItem(collapse = collapse)
                            openLinkControl.AsDropdownMenuItem(collapse = collapse)
                            sendLinkControl.AsDropdownMenuItem(collapse = collapse)
                            hideTonalityControl.AsDropdownMenuItem(collapse = collapse)
                            resetTonalityControl.AsDropdownMenuItem(collapse = collapse)
                            resetScaleControl.AsDropdownMenuItem(collapse = collapse)
                            optimizeScaleControl?.AsDropdownMenuItem(collapse = collapse)
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = containerModifier,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing)),
            ) {
                Box(modifier = boxModifier.fillMaxHeight().weight(1f)) {
                    Text(
                        text = text,
                        modifier = textModifier,
                        fontSize = integerResource(R.integer.song_font_size).sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = integerResource(R.integer.song_line_height).sp,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        navigateBackControl.AsIconButton()
                        starControl.AsIconButton()
                        showInfoControl.AsIconButton()
                        openLinkControl.AsIconButton()
                        sendLinkControl.AsIconButton()
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    SpeedRow(
                        initialSpeed = initialSpeed,
                        changeSpeed = {
                            changeSpeed(it)
                            coroutineScope.launch {
                                focusControl.reconfigure(linesPerMinute = it)
                            }
                        },
                    )
                    Row {
                        scrollControl?.AsIconButton()
                        decreaseScaleControl.AsIconButton()
                        increaseScaleControl.AsIconButton()
                        resetScaleControl.AsIconButton()
                        optimizeScaleControl?.AsIconButton()
                    }
                    Row {
                        TonalityMenu(
                            hiddenTonalities = hiddenTonalities,
                            defaultTonality = chord.tonality,
                            currentTonality = currentTonality,
                            changeTonality = changeTonality,
                        )
                        hideTonalityControl.AsIconButton()
                        resetTonalityControl.AsIconButton()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // TopAppBar
@Composable
private fun TopBar(
    song: Song,
    starControl: SongControl,
    navigateBackControl: SongControl,
) {
    TopAppBar(
        title = { Text(text = song.title) },
        navigationIcon = { navigateBackControl.AsIconButton() },
        actions = { starControl.AsIconButton() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
    )
}

@Composable
private fun LyricsContent(content: String, scale: Float, modifier: Modifier) {
    Column(modifier = modifier) {
        content.split(Regex("(?:\r\n|\r|\n)<hr />(?:\r\n|\r|\n)")).forEachIndexed { index, content ->
            val source = content.replace(Regex("\r\n|\r|\n"), "<br>")
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
                fontSize = integerResource(R.integer.song_font_size).sp * scale,
                lineHeight = integerResource(R.integer.song_line_height).sp * scale,
            )
        }
    }
}

@Composable
private fun TonalityMenu(
    hiddenTonalities: Set<MusicNote>,
    defaultTonality: MusicNote,
    currentTonality: MusicNote?,
    changeTonality: (MusicNote?) -> Unit,
) {
    Box {
        var expanded by remember { mutableStateOf(false) }
        FilterChip(
            selected = currentTonality != null,
            onClick = { expanded = expanded.not() },
            label = {
                val label = stringResource(R.string.tonality_label)
                val chord = MusicNote.toNotationOrNull(currentTonality)
                Text(text = if (chord != null) "$label: $chord" else label)
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            MusicNote.TONALITIES.filter {
                hiddenTonalities.contains(it).not()
            }.toMutableList<MusicNote?>().also {
                it.add(0, null)
            }.forEach { tonality ->
                val lyricsNotation = stringResource(MusicNote.NOTATION_LYRICS)
                DropdownMenuItem(
                    text = {
                        val tonalityNotation = MusicNote.toNotationOrNull(tonality)
                        val defaultLabel = stringResource(R.string.tonality_default)
                        val text = if (tonalityNotation == null)
                            lyricsNotation
                        else if (tonality == defaultTonality)
                            "$tonalityNotation $defaultLabel"
                        else
                            tonalityNotation
                        Text(text = text)
                    },
                    onClick = {
                        changeTonality(tonality)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // TooltipBox
@Composable
private fun AdjustSpeedToggleButton(
    currentSpeeding: Boolean,
    changeSpeeding: (Boolean) -> Unit,
) {
    val text = R.string.speed_adjust
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(text = stringResource(text), textAlign = TextAlign.Center)
            }
        },
        state = rememberTooltipState(),
    ) {
        IconToggleButton(
            checked = currentSpeeding,
            onCheckedChange = changeSpeeding,
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_speed_24),
                contentDescription = stringResource(text),
            )
        }
    }
}

@Composable
private fun SpeedRow(
    initialSpeed: Float,
    changeSpeed: (Float) -> Unit,
) {
    val speedList = SongViewModel.speedList
    val minIndex = 0
    val maxIndex = speedList.size - 1
    val initialIndex = speedList.map {
        it.minus(initialSpeed).absoluteValue
    }.withIndex().minBy { it.value }.index
    var currentIndex by remember { mutableIntStateOf(initialIndex) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        val currentSpeed = speedList[currentIndex]
        val currentText = if (currentSpeed % 1f == 0f)
            currentSpeed.roundToInt().toString()
        else
            currentSpeed.toString()
        SongControl.greatlyDecreaseSpeed(enabled = currentIndex > minIndex) {
            currentIndex = currentIndex.minus(4).coerceIn(minIndex, maxIndex)
            changeSpeed(speedList[currentIndex])
        }.AsIconButton()
        SongControl.decreaseSpeed(enabled = currentIndex > minIndex) {
            currentIndex = currentIndex.minus(1).coerceIn(minIndex, maxIndex)
            changeSpeed(speedList[currentIndex])
        }.AsIconButton()
        Text(
            text = currentText,
            modifier = Modifier.width(dimensionResource(R.dimen.speed_width)),
            textAlign = TextAlign.Center,
        )
        SongControl.increaseSpeed(enabled = currentIndex < maxIndex) {
            currentIndex = currentIndex.plus(1).coerceIn(minIndex, maxIndex)
            changeSpeed(speedList[currentIndex])
        }.AsIconButton()
        SongControl.greatlyIncreaseSpeed(enabled = currentIndex < maxIndex) {
            currentIndex = currentIndex.plus(4).coerceIn(minIndex, maxIndex)
            changeSpeed(speedList[currentIndex])
        }.AsIconButton()
    }
}

@Preview
@Composable
private fun StartPreview() {
    ChristianikaTragoudiaTheme {
        LoadingScreen(navigateBack = {})
    }
}

@Preview
@Composable
private fun LyricsPreview() {
    ChristianikaTragoudiaTheme {
        LyricsScreen(
            song = Demo.song,
            currentStarred = false,
            changeStarred = {},
            hiddenTonalities = MusicNote.ENHARMONIC_TONALITIES,
            defaultTonality = Demo.chord.tonality,
            changeTonality = {},
            initialScale = 1f,
            changeScale = {},
            initialOffset = SongLyricsFocusControl.MAX_OFFSET,
            changeOffset = {},
            navigateBack = {},
        )
    }
}

@Preview
@Composable
private fun ChordsPreview() {
    ChristianikaTragoudiaTheme {
        ChordsScreen(
            song = Demo.song,
            chord = Demo.chord,
            currentStarred = true,
            changeStarred = {},
            hiddenTonalities = MusicNote.ENHARMONIC_TONALITIES,
            currentTonality = Demo.chord.tonality,
            changeTonality = {},
            initialScale = 1f,
            changeScale = {},
            initialOffset = SongChordsFocusControl.maxOffset,
            changeOffset = {},
            initialScrolling = false,
            changeScrolling = {},
            currentSpeeding = true,
            changeSpeeding = {},
            initialSpeed = SongViewModel.defaultSpeed,
            changeSpeed = {},
            navigateBack = {},
        )
    }
}
