package gr.christianikatragoudia.app.music

enum class MusicAlter(val symbol: String, val semitones: Int) {
    DOUBLE_FLAT(symbol = "bb", semitones = -2),
    FLAT(symbol = "b", semitones = -1),
    NATURAL(symbol = "", semitones = 0),
    SHARP(symbol = "#", semitones = 1),
    DOUBLE_SHARP(symbol = "x", semitones = 2);

    companion object {

        private val symbol2alter = entries.associateBy { it.symbol }
        private val semitones2alter = entries.associateBy { it.semitones }

        fun getBySymbol(symbol: String): MusicAlter? {
            return symbol2alter[symbol]
        }

        fun getBySemitones(semitones: Int): MusicAlter? {
            return semitones2alter[semitones]
        }
    }
}
