package gr.christianikatragoudia.app.ui

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.text.Html
import android.text.style.StyleSpan
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.tooling.preview.Preview
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
import gr.christianikatragoudia.app.data.DateTimeConverter
import gr.christianikatragoudia.app.data.Song
import gr.christianikatragoudia.app.music.MusicInterval
import gr.christianikatragoudia.app.music.MusicNote
import gr.christianikatragoudia.app.nav.NavDestination
import gr.christianikatragoudia.app.network.TheAnalytics
import gr.christianikatragoudia.app.ui.theme.ChristianikaTragoudiaTheme
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
            TheScaffold(
                song = song,
                chord = chord,
                navigateBack = navigateBack,
                starred = songMeta.starred,
                starredToggle = if (songMeta.starred) ({
                    viewModel.setStarred(false)
                }) else ({
                    viewModel.setStarred(true)
                }),
                tonalityList = MusicNote.TONALITIES.filter {
                    !hiddenTonalities.contains(it)
                }.toMutableList<MusicNote?>().also {
                    it.add(0, null)
                },
                tonalitySelected = chordMeta.tonality,
                tonalityChange = {
                    viewModel.setTonality(it)
                },
                songZoom = songMeta.zoom,
                chordZoom = chordMeta.zoom,
                zoomReset = if (chordMeta.tonality == null) {
                    if (songMeta.zoom != 0) ({
                        viewModel.setSongZoom(0)
                    }) else
                        null
                } else {
                    if (chordMeta.zoom != 0) ({
                        viewModel.setChordZoom(0)
                    }) else
                        null
                },
                zoomChange = if (chordMeta.tonality == null) ({
                    viewModel.setSongZoom(it)
                }) else ({
                    viewModel.setChordZoom(it)
                }),
                expanded = uiState.expanded,
                expandedSet = { viewModel.setExpanded(it) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TheScaffold(
    song: Song,
    chord: Chord,
    navigateBack: () -> Unit,
    starred: Boolean,
    starredToggle: () -> Unit,
    tonalityList: List<MusicNote?>,
    tonalitySelected: MusicNote?,
    tonalityChange: (MusicNote?) -> Unit,
    songZoom: Int,
    chordZoom: Int,
    zoomReset: (() -> Unit)?,
    zoomChange: (Int) -> Unit,
    expanded: Boolean,
    expandedSet: (Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            // TODO hide on fullscreen
            SongTopBar(
                song = song,
                starred = starred,
                onStarredToggle = starredToggle,
                navigateBack = navigateBack,
            )
        },
        bottomBar = {
            if (!expanded) {
                SongBottomBar(
                    tonalityList = tonalityList,
                    tonalitySelected = tonalitySelected,
                    tonalityDefault = chord.tonality,
                    tonalityChange = tonalityChange,
                    zoomReset = zoomReset,
                    expandedSet = expandedSet,
                )
            }
        },
        floatingActionButton = {
            if (expanded) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip {
                        Text(
                            text = stringResource(id = R.string.full_screen_disable_text),
                            textAlign = TextAlign.Center,
                        )
                    } },
                    state = rememberTooltipState(),
                ) {
                    FloatingActionButton(onClick = { expandedSet(false) }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_fullscreen_exit_24),
                            contentDescription = stringResource(R.string.full_screen_disable_text),
                        )
                    }
                }
            }
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) {
        val modifier = Modifier
            .padding(it)
            .fillMaxSize()
            .clipToBounds()
        if (tonalitySelected == null)
            SongLyrics(song, modifier, songZoom, zoomChange)
        else
            SongChords(chord, tonalitySelected, modifier, chordZoom, zoomChange)
    }
}

@Composable
private fun SongLyrics(
    song: Song,
    modifier: Modifier = Modifier,
    size: Int = 0,
    sizeChange: (Int) -> Unit = {},
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
    Box(
        modifier = modifier
            .transformable(state = state)
            .onSizeChanged { outerHeight = it.height },
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight(align = Alignment.Top, unbounded = true)
                .graphicsLayer(translationY = pan)
                .onSizeChanged { innerHeight = it.height }
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
    }
}

@Composable
private fun SongChords(
    chord: Chord,
    tonality: MusicNote,
    modifier: Modifier = Modifier,
    size: Int = 0,
    sizeChange: (Int) -> Unit = {},
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
    Box(
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
                .padding(8.dp)
            ,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp * zoom,
            lineHeight = 24.sp * zoom,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SongTopBar(
    song: Song,
    starred: Boolean,
    onStarredToggle: () -> Unit,
    navigateBack: () -> Unit,
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
            val context = LocalContext.current
            var infoVisible by remember { mutableStateOf(false) }
            val infoText = stringResource(R.string.information)
            val infoIcon = Icons.Default.Info
            val infoAction = {
                infoVisible = true
            }
            val openText = stringResource(R.string.open_link)
            val openIcon = Icons.AutoMirrored.Default.ExitToApp
            val openAction = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.permalink))
                context.startActivity(intent)
            }
            val shareText = stringResource(R.string.send_link)
            val shareIcon = Icons.Default.Share
            val shareAction = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, song.title)
                    putExtra(Intent.EXTRA_TEXT, song.permalink)
                }
                context.startActivity(Intent.createChooser(intent, shareText))
                TheAnalytics.logShare("url", song.permalink)
            }
            StarredIconButton(starred, onStarredToggle)
            when (configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    VectorIconButton(text = infoText, icon = infoIcon, action = infoAction)
                    VectorIconButton(text = openText, icon = openIcon, action = openAction)
                    VectorIconButton(text = shareText, icon = shareIcon, action = shareAction)
                }
                else -> {
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
                            MyDropdownMenuItem(infoText, infoIcon, infoAction, collapse)
                            MyDropdownMenuItem(openText, openIcon, openAction, collapse)
                            MyDropdownMenuItem(shareText, shareIcon, shareAction, collapse)
                        }
                    }
                }
            }
            if (infoVisible) {
                AlertDialog(
                    onDismissRequest = { infoVisible = false },
                    confirmButton = {
                        TextButton(onClick = { infoVisible = false }) {
                            Text(text = stringResource(R.string.close))
                        }
                    },
                    title = { Text(text = song.title, modifier = Modifier.fillMaxWidth()) },
                    text = { Text(text = song.excerpt) },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
    )
}

@Composable
private fun StarredIconButton(
    starred: Boolean,
    onStarredToggle: () -> Unit,
) {
    if (starred) {
        PainterIconButton(
            text = stringResource(R.string.starred_remove),
            icon = painterResource(R.drawable.baseline_star_24),
            action = onStarredToggle,
        )
    } else {
        PainterIconButton(
            text = stringResource(R.string.starred_add),
            icon = painterResource(R.drawable.baseline_star_outline_24),
            action = onStarredToggle,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VectorIconButton(
    text: String,
    icon: ImageVector,
    action: () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(text = text, textAlign = TextAlign.Center) } },
        state = rememberTooltipState(),
    ) {
        IconButton(onClick = action) {
            Icon(imageVector = icon, contentDescription = text)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PainterIconButton(
    text: String,
    icon: Painter,
    action: () -> Unit,
    enabled: Boolean = true,
) {
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
private fun MyDropdownMenuItem(
    text: String,
    icon: ImageVector,
    action: () -> Unit,
    collapse: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(text = text)
        },
        onClick = {
            collapse()
            action()
        },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null)
        },
    )
}

@Composable
private fun SongBottomBar(
    tonalityList: List<MusicNote?>,
    tonalitySelected: MusicNote?,
    tonalityDefault: MusicNote,
    tonalityChange: (MusicNote?) -> Unit,
    zoomReset: (() -> Unit)?,
    expandedSet: (Boolean) -> Unit,
) {
    BottomAppBar(containerColor = Color.Transparent) {
        Spacer(modifier = Modifier.size(8.dp))
        Box {
            var expanded by remember { mutableStateOf(false) }
            FilterChip(
                selected = tonalitySelected != null,
                onClick = { expanded = !expanded },
                label = {
                    val label = stringResource(R.string.tonality_label)
                    val chord = MusicNote.toNotationOrNull(tonalitySelected)
                    Text(text = if (chord != null) "$label: $chord" else label)
                },
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                tonalityList.forEach {
                    val lyricsNotation = stringResource(MusicNote.NOTATION_LYRICS)
                    DropdownMenuItem(
                        text = {
                            val tonalityNotation = MusicNote.toNotationOrNull(it)
                            val defaultLabel = stringResource(R.string.tonality_default_text)
                            val text = if (tonalityNotation == null)
                                lyricsNotation
                            else if (it == tonalityDefault)
                                "$tonalityNotation $defaultLabel"
                            else
                                tonalityNotation
                            Text(text = text)
                        },
                        onClick = {
                            expanded = false
                            tonalityChange(it)
                        },
                        trailingIcon = {
                            if (it == tonalitySelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Spacer(modifier = Modifier.weight(1F))
        PainterIconButton(
            text = stringResource(R.string.full_screen_enable_text),
            icon = painterResource(R.drawable.baseline_fullscreen_24),
            action = { expandedSet(true) },
        )
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
                        tonalityChange(null)
                    },
                    enabled = tonalitySelected != null,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.tonality_reset_text)) },
                    onClick = {
                        expanded = false
                        tonalityChange(tonalityDefault)
                    },
                    enabled = tonalitySelected != tonalityDefault,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.font_size_reset_text)) },
                    onClick = {
                        expanded = false
                        if (zoomReset != null)
                            zoomReset()
                    },
                    enabled = zoomReset != null,
                )
            }
        }
    }
}

private val thavorSong = Song(
    id = 1,
    date = DateTimeConverter.byStr("2022-01-10 00:00:00"),
    content = "Θ' ανεβούμε μαζί στο βουνό,\n" +
            "στο βουνό το ψηλό, το μεγάλο.\n" +
            "Μπρoς εσύ, πίσω εγώ κι αρχινώ\n" +
            "της αυγής το τραγούδι να ψάλλω.\n" +
            "\n" +
            "<i>Μπρος εσύ, πίσω εγώ και γοργοί\n" +
            "στου Θαβώρ τις κορφές θ' ανεβούμε\n" +
            "και μακριά απ' την πολύβουη γη\n" +
            "άλλων κόσμων το φως θα χαρούμε.</i>\n" +
            "\n" +
            "Πόσο λάμπει η θεϊκιά σου μορφή,\n" +
            "πώς αστράφτει ο λευκός σου χιτώνας.\n" +
            "Τρεις σκηνές να στηθούν στην κορφή\n" +
            "κι ας τη δέρνει ο βοριάς κι ο χειμώνας.\n" +
            "\n" +
            "<i>Μπρος εσύ, πίσω εγώ και γοργοί…</i>",
    title = "Θαβώρ",
    excerpt = "Θ' ανεβούμε μαζί στο βουνό",
    modified = DateTimeConverter.byStr("2022-01-10 00:00:00"),
    permalink = "https://christianikatragoudia.gr/songs/thavor-tha-anevoume-mazi/",
)

private val thavorChord = Chord(
    id = 4957,
    date = DateTimeConverter.byStr("2022-01-10 00:00:00"),
    modified = DateTimeConverter.byStr("2022-01-10 00:00:00"),
    parent = 1,
    content = "Gm    EbΔ7  Gm    EbΔ7  \n" +
            "\n" +
            "      Gm                Dm\n" +
            "Θ' ανεβούμε μαζί στο βουνό,\n" +
            "       Eb               Bb\n" +
            "στο βουνό το ψηλό, το μεγάλο.\n" +
            "       Gm                  Cm\n" +
            "Μπρoς εσύ, πίσω εγώ κι αρχινώ\n" +
            "                Gm       D Gm\n" +
            "της αυγής το τραγούδι να ψάλλω.\n" +
            "\n" +
            "G  D  \n" +
            "\n" +
            "       G                  Bm\n" +
            "Μπρος εσύ, πίσω εγώ και γοργοί\n" +
            "       C                    G\n" +
            "στου Θαβώρ τις κορφές θ' ανεβούμε\n" +
            "      C              D      G\n" +
            "και μακριά απ' την πολύβουη γη\n" +
            "      Em        C        D  G   D\n" +
            "άλλων κόσμων το φως θα χαρούμε.\n" +
            "\n" +
            "Gm    EbΔ7  Gm    EbΔ7  \n" +
            "\n" +
            "     Gm                    Dm\n" +
            "Πόσο λάμπει η θεϊκιά σου μορφή,\n" +
            "     Eb                      Bb\n" +
            "πώς αστράφτει ο λευκός σου χιτώνας.\n" +
            "         Gm                    Cm\n" +
            "Τρεις σκηνές να στηθούν στην κορφή\n" +
            "                    Gm           D Gm\n" +
            "κι ας τη δέρνει ο βοριάς κι ο χειμώνας.\n" +
            "\n" +
            "G  D  \n" +
            "\n" +
            "       G                  Bm\n" +
            "Μπρος εσύ, πίσω εγώ και γοργοί\n" +
            "       C                    G\n" +
            "στου Θαβώρ τις κορφές θ' ανεβούμε\n" +
            "      C              D      G\n" +
            "και μακριά απ' την πολύβουη γη\n" +
            "      Em        C        D  G\n" +
            "άλλων κόσμων το φως θα χαρούμε,\n" +
            "      C              D      G\n" +
            "και μακριά απ' την πολύβουη γη\n" +
            "      Em        C        D  G   D\n" +
            "άλλων κόσμων το φως θα χαρούμε.",
    tonality = MusicNote.byNotation("G")!!,
)

@Preview
@Composable
private fun SongLyricsPreview() {
    ChristianikaTragoudiaTheme {
        TheScaffold(
            song = thavorSong,
            chord = thavorChord,
            navigateBack = {},
            starred = false,
            starredToggle = {},
            tonalityList = MusicNote.TONALITIES.filter {
                !MusicNote.ENHARMONIC_TONALITIES.contains(it)
            }.toMutableList<MusicNote?>().also {
                it.add(0, null)
            },
            tonalitySelected = null,
            tonalityChange = {},
            songZoom = 0,
            chordZoom = 0,
            zoomReset = {},
            zoomChange = {},
            expanded = false,
            expandedSet = {},
        )
    }
}
