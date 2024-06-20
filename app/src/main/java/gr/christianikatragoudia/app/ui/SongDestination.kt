package gr.christianikatragoudia.app.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.Html
import android.text.style.StyleSpan
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.math.pow

object SongDestination : NavDestination {

    private val factory = viewModelFactory {
        initializer {
            SongViewModel(
                songId = this.createSavedStateHandle()[songIdArg] ?: 0,
                application = this[APPLICATION_KEY] as TheApplication,
            )
        }
    }

    override val route = "song"
    const val songIdArg = "songId"
    val routeWithArgs = "$route/{$songIdArg}"

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
            val hiddenTonalities = uiState.hiddenTonalities
            TheScaffold(
                song = song,
                chord = chord,
                starred = songMeta.starred,
                tonalities = MusicNote.TONALITIES.filter {
                    !hiddenTonalities.contains(it)
                }.toMutableList<MusicNote?>().also {
                    it.add(0, null)
                },
                tonality = chordMeta.tonality,
                songZoom = songMeta.zoom,
                chordZoom = chordMeta.zoom,
                navigateBack = navigateBack,
                onStarredToggle = if (songMeta.starred) ({
                    viewModel.setStarred(false)
                }) else ({
                    viewModel.setStarred(true)
                }),
                onTonalityHide = if (chordMeta.tonality != null) ({
                    viewModel.setTonality(null)
                }) else
                    null,
                onTonalityReset = if (chordMeta.tonality != chord.tonality) ({
                    viewModel.setTonality(chord.tonality)
                }) else
                    null,
                onTonalityChange = {
                    viewModel.setTonality(it)
                },
                onZoomReset = if (chordMeta.tonality == null) {
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
                onZoomIncrease = if (chordMeta.tonality == null) {
                    if (songMeta.zoom < 20) ({
                        viewModel.setSongZoom(songMeta.zoom + 1)
                    }) else
                        null
                } else {
                    if (chordMeta.zoom < 20) ({
                        viewModel.setChordZoom(chordMeta.zoom + 1)
                    }) else
                        null
                },
                onZoomDecrease = if (chordMeta.tonality == null) {
                    if (songMeta.zoom > -20) ({
                        viewModel.setSongZoom(songMeta.zoom - 1)
                    }) else
                        null
                } else {
                    if (chordMeta.zoom > -20) ({
                        viewModel.setChordZoom(chordMeta.zoom - 1)
                    }) else
                        null
                },
            )
        }
    }
}

@Composable
private fun TheScaffold(
    song: Song,
    chord: Chord,
    starred: Boolean,
    tonalities: List<MusicNote?>,
    tonality: MusicNote?,
    songZoom: Int,
    chordZoom: Int,
    navigateBack: () -> Unit,
    onStarredToggle: () -> Unit,
    onTonalityHide: (() -> Unit)?,
    onTonalityReset: (() -> Unit)?,
    onTonalityChange: (MusicNote?) -> Unit,
    onZoomReset: (() -> Unit)?,
    onZoomIncrease: (() -> Unit)?,
    onZoomDecrease: (() -> Unit)?,
) {
    Scaffold(
        topBar = {
            SongTopBar(
                song = song,
                starred = starred,
                onStarredToggle = onStarredToggle,
                navigateBack = navigateBack,
            )
        },
        bottomBar = {
            SongBottomBar(
                tonalities = tonalities,
                tonality = tonality,
                onTonalityHide = onTonalityHide,
                onTonalityReset = onTonalityReset,
                onTonalityChange = onTonalityChange,
                onZoomReset = onZoomReset,
                onZoomIncrease = onZoomIncrease,
                onZoomDecrease = onZoomDecrease,
            )
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) {
        Box(modifier = Modifier.padding(it)) {
            if (tonality == null)
                SongLyrics(song, songZoom)
            else
                SongChords(chord, tonality, chordZoom)
        }
    }
}

@Composable
private fun SongLyrics(song: Song, songZoom: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
        ,
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
                fontSize = 16.sp * 2F.pow(songZoom / 10F),
                lineHeight = 24.sp * 2F.pow(songZoom / 10F),
            )
        }
    }
}

@Composable
private fun SongChords(
    chord: Chord,
    tonality: MusicNote,
    chordZoom: Int,
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
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
        ,
        fontFamily = FontFamily.Monospace,
        fontSize = 16.sp * 2F.pow(chordZoom / 10F),
        lineHeight = 24.sp * 2F.pow(chordZoom / 10F),
    )
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
            Text(song.title)
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
            StarredIconButton(starred, onStarredToggle)
            SongTopBarMenu(song)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        )
    )
}

@Composable
private fun StarredIconButton(
    starred: Boolean,
    onStarredToggle: () -> Unit,
) {
    IconButton(onClick = onStarredToggle) {
        if (starred) {
            Icon(
                painter = painterResource(R.drawable.baseline_star_24),
                contentDescription = stringResource(R.string.starred_remove),
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.baseline_star_outline_24),
                contentDescription = stringResource(R.string.starred_add),
            )
        }
    }
}

@SuppressLint("ServiceCast")
@Composable
private fun SongTopBarMenu(song: Song) {
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
            val context = LocalContext.current
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.open_link)) },
                onClick = {
                    expanded = false
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.permalink))
                    context.startActivity(intent)
                },
            )
            val shareText = stringResource(R.string.send_link)
            DropdownMenuItem(
                text = { Text(text = shareText) },
                onClick = {
                    expanded = false
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, song.title)
                        putExtra(Intent.EXTRA_TEXT, song.permalink)
                    }
                    context.startActivity(Intent.createChooser(intent, shareText))
                    TheAnalytics.logShare("url", song.permalink)
                },
            )
        }
    }
}

@Composable
private fun SongBottomBar(
    tonalities: List<MusicNote?>,
    tonality: MusicNote?,
    onTonalityHide: (() -> Unit)?,
    onTonalityReset: (() -> Unit)?,
    onTonalityChange: (MusicNote?) -> Unit,
    onZoomReset: (() -> Unit)?,
    onZoomIncrease: (() -> Unit)?,
    onZoomDecrease: (() -> Unit)?,
) {
    BottomAppBar(containerColor = Color.Transparent) {
        Text(text = stringResource(R.string.tonality_label))
        Box {
            var expanded by remember { mutableStateOf(false) }
            BadgedBox(
                badge = {
                    Badge(modifier = Modifier.offset(x = (-12).dp, y = 12.dp)) {
                        Text(text = MusicNote.toNotationOrElse(tonality, MusicNote.NOTATION_NULL))
                    }
                },
            ) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_piano_24),
                        contentDescription = stringResource(R.string.tonality_select_text),
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                tonalities.forEach {
                    val lyricsNotation = stringResource(MusicNote.NOTATION_LYRICS)
                    DropdownMenuItem(
                        text = {
                            Text(text = MusicNote.toNotationOrElse(it, lyricsNotation))
                        },
                        onClick = {
                            expanded = false
                            onTonalityChange(it)
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1F))
        IconButton(onClick = onZoomIncrease ?: {}, enabled = onZoomIncrease != null) {
            Icon(
                painter = painterResource(R.drawable.baseline_text_increase_24),
                contentDescription = stringResource(R.string.font_size_increase_text),
            )
        }
        IconButton(onClick = onZoomDecrease ?: {}, enabled = onZoomDecrease != null) {
            Icon(
                painter = painterResource(R.drawable.baseline_text_decrease_24),
                contentDescription = stringResource(R.string.font_size_decrease_text),
            )
        }
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
                        if (onTonalityHide != null)
                            onTonalityHide()
                    },
                    enabled = onTonalityHide != null,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.tonality_reset_text)) },
                    onClick = {
                        expanded = false
                        if (onTonalityReset != null)
                            onTonalityReset()
                    },
                    enabled = onTonalityReset != null,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.font_size_reset_text)) },
                    onClick = {
                        expanded = false
                        if (onZoomReset != null)
                            onZoomReset()
                    },
                    enabled = onZoomReset != null,
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
            starred = false,
            tonalities = MusicNote.TONALITIES.filter {
                !MusicNote.ENHARMONIC_TONALITIES.contains(it)
            }.toMutableList<MusicNote?>().also {
                it.add(0, null)
            },
            tonality = null,
            songZoom = 0,
            chordZoom = 0,
            navigateBack = {},
            onStarredToggle = {},
            onTonalityHide = null,
            onTonalityReset = {},
            onTonalityChange = {},
            onZoomReset = {},
            onZoomIncrease = {},
            onZoomDecrease = {},
        )
    }
}
