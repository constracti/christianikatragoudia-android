package gr.christianikatragoudia.app

import gr.christianikatragoudia.app.data.Chord
import gr.christianikatragoudia.app.data.DateTimeConverter
import gr.christianikatragoudia.app.data.Song
import gr.christianikatragoudia.app.music.MusicNote


object Demo {

    val song = Song(
        id = 1,
        date = DateTimeConverter.byStr("2022-01-10 00:00:00"),
        content = """
            Θ' ανεβούμε μαζί στο βουνό,
            στο βουνό το ψηλό, το μεγάλο.
            Μπρος εσύ, πίσω εγώ κι αρχινώ
            της αυγής το τραγούδι να ψάλλω.
            
            <i>Μπρος εσύ, πίσω εγώ και γοργοί
            στου Θαβώρ τις κορφές θ' ανεβούμε
            και μακριά απ' την πολύβουη γη
            άλλων κόσμων το φως θα χαρούμε.</i>
            
            <hr />
            
            Πόσο λάμπει η θεϊκιά σου μορφή,
            πώς αστράφτει ο λευκός σου χιτώνας.
            Τρεις σκηνές να στηθούν στην κορφή
            κι ας τη δέρνει ο βοριάς κι ο χειμώνας.
    
            <i>Μπρος εσύ, πίσω εγώ και γοργοί…</i>
        """.trimIndent(),
        title = "Θαβώρ",
        excerpt = "Θ' ανεβούμε μαζί στο βουνό",
        modified =  DateTimeConverter.byStr("2022-01-10 00:00:00"),
        permalink = "https://christianikatragoudia.gr/songs/thavor-tha-anevoume-mazi/"
    )

    val chord = Chord(
        id = 7566,
        date = DateTimeConverter.byStr("2024-03-11 00:00:00"),
        modified = DateTimeConverter.byStr("2024-03-11 00:00:00"),
        parent = 1,
        content = """
            Gm    EbΔ7  Gm    EbΔ7  

                  Gm                Dm
            Θ' ανεβούμε μαζί στο βουνό,
                   Eb               Bb
            στο βουνό το ψηλό, το μεγάλο.
                   Gm                  Cm
            Μπρος εσύ, πίσω εγώ κι αρχινώ
                            Gm       D Gm
            της αυγής το τραγούδι να ψάλλω.

            G  D  

                   G                  Bm
            Μπρος εσύ, πίσω εγώ και γοργοί
                   C                    G
            στου Θαβώρ τις κορφές θ' ανεβούμε
                  C              D      G
            και μακριά απ' την πολύβουη γη
                  Em        C        D  G   D
            άλλων κόσμων το φως θα χαρούμε.

            Gm    EbΔ7  Gm    EbΔ7  

                 Gm                    Dm
            Πόσο λάμπει η θεϊκιά σου μορφή,
                 Eb                      Bb
            πώς αστράφτει ο λευκός σου χιτώνας.
                     Gm                    Cm
            Τρεις σκηνές να στηθούν στην κορφή
                                Gm           D Gm
            κι ας τη δέρνει ο βοριάς κι ο χειμώνας.

            G  D  


            Μπρος εσύ, πίσω εγώ και γοργοί…
        """.trimIndent(),
        tonality = MusicNote.byNotation("G")!!,
    )
}
