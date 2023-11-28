package gr.christianikatragoudia.app.music

class MusicInterval(val diatonic: Int, val chromatic: Int) {

    fun transposeLine(originalLine: String): String {
        val acc = MusicNote.REGEX.findAll(originalLine).fold(Pair("", 0)) { acc, matchResult ->
            val currFirst = matchResult.range.first
            val block = originalLine.substring(acc.second, currFirst).
                replace(Regex("\\s{2,}$"), " ")
            val spacesLength = currFirst - (acc.first.length + block.length)
            val spaces = " ".repeat(if (block.lastOrNull() != '/' && spacesLength >= 0) spacesLength else 0)
            val originalNotation = matchResult.value
            val originalNote = MusicNote.byNotation(originalNotation)!!
            val transposedNote = originalNote.transpose(this)
            val transposedNotation = MusicNote.toNotationOrElse(transposedNote, MusicNote.NOTATION_ERROR)
            Pair(acc.first + block + spaces + transposedNotation, matchResult.range.last + 1)
        }
        return acc.first + originalLine.substring(acc.second)
    }

    companion object {

        fun getByNotes(src: MusicNote, dst: MusicNote): MusicInterval {
            val diatonic = dst.step.diatonic - src.step.diatonic
            val chromatic = (
                dst.step.chromatic + dst.alter.semitones
                - src.step.chromatic - src.alter.semitones
            )
            return MusicInterval(diatonic, chromatic)
        }
    }
}
