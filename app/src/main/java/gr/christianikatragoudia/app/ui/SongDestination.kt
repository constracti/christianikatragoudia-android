package gr.christianikatragoudia.app.ui

import android.content.Intent
import android.content.res.Configuration
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.text.style.TextOverflow
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
                zoomIncrease = if (chordMeta.tonality == null) {
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
                zoomDecrease = if (chordMeta.tonality == null) {
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
    navigateBack: () -> Unit,
    starred: Boolean,
    starredToggle: () -> Unit,
    tonalityList: List<MusicNote?>,
    tonalitySelected: MusicNote?,
    tonalityChange: (MusicNote?) -> Unit,
    songZoom: Int,
    chordZoom: Int,
    zoomReset: (() -> Unit)?,
    zoomIncrease: (() -> Unit)?,
    zoomDecrease: (() -> Unit)?,
) {
    Scaffold(
        topBar = {
            SongTopBar(
                song = song,
                starred = starred,
                onStarredToggle = starredToggle,
                navigateBack = navigateBack,
            )
        },
        bottomBar = {
            SongBottomBar(
                tonalityList = tonalityList,
                tonalitySelected = tonalitySelected,
                tonalityDefault = chord.tonality,
                tonalityChange = tonalityChange,
                zoomReset = zoomReset,
                zoomIncrease = zoomIncrease,
                zoomDecrease = zoomDecrease,
            )
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) {
        Box(modifier = Modifier.padding(it)) {
            if (tonalitySelected == null)
                SongLyrics(song, songZoom)
            else
                SongChords(chord, tonalitySelected, chordZoom)
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
                    MyIconButton(text = infoText, icon = infoIcon, action = infoAction)
                    MyIconButton(text = openText, icon = openIcon, action = openAction)
                    MyIconButton(text = shareText, icon = shareIcon, action = shareAction)
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

@Composable
private fun MyIconButton(
    text: String,
    icon: ImageVector,
    action: () -> Unit,
) {
    IconButton(onClick = action) {
        Icon(imageVector = icon, contentDescription = text)
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
    zoomIncrease: (() -> Unit)?,
    zoomDecrease: (() -> Unit)?,
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
        IconButton(onClick = zoomIncrease ?: {}, enabled = zoomIncrease != null) {
            Icon(
                painter = painterResource(R.drawable.baseline_text_increase_24),
                contentDescription = stringResource(R.string.font_size_increase_text),
            )
        }
        IconButton(onClick = zoomDecrease ?: {}, enabled = zoomDecrease != null) {
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
            zoomIncrease = {},
            zoomDecrease = {},
        )
    }
}
