package gr.christianikatragoudia.app.music

import androidx.annotation.StringRes
import gr.christianikatragoudia.app.R

class MusicNote(val step: MusicStep, val alter: MusicAlter) {

    override fun equals(other: Any?): Boolean {
        if (other !is MusicNote)
            return false
        return (other.step == step) && (other.alter == alter)
    }

    override fun hashCode(): Int {
        var result = step.hashCode()
        result = 31 * result + alter.hashCode()
        return result
    }

    fun transpose(interval: MusicInterval): MusicNote? {
        var octaves = 0
        var newDiatonic = step.diatonic + interval.diatonic
        while (newDiatonic < 0) {
            newDiatonic += 7
            octaves -= 1
        }
        while (newDiatonic >= 7) {
            newDiatonic -= 7
            octaves += 1
        }
        val newStep = MusicStep.getByDiatonic(newDiatonic)
        var newSemitones = step.chromatic + alter.semitones + interval.chromatic - newStep.chromatic
        newSemitones -= 12 * octaves
        val newAlter = MusicAlter.getBySemitones(newSemitones)
        if (newAlter == null)
            return null
        return MusicNote(newStep, newAlter)
    }

    companion object {

        val REGEX = Regex("[A-G](bb?|#|x)?")

        val TONALITIES = MusicStep.entries.fold(mutableListOf()) { acc: MutableList<MusicNote>, musicStep ->
            acc.add(MusicNote(musicStep, MusicAlter.FLAT))
            acc.add(MusicNote(musicStep, MusicAlter.NATURAL))
            acc.add(MusicNote(musicStep, MusicAlter.SHARP))
            acc
        }.toList()

        val ENHARMONIC_TONALITIES = listOf("Cb", "Db", "D#", "E#", "Fb", "Gb", "G#", "A#", "B#")
            .mapNotNull { byNotation(it) }
            .toSet()

        fun byNotation(notation: String): MusicNote? {
            val step = MusicStep.getByName(notation.substring(0, 1))
            if (step == null)
                return null
            val alter = MusicAlter.getBySymbol(notation.substring(1))
            if (alter == null)
                return null
            return MusicNote(step, alter)
        }

        fun toNotation(note: MusicNote): String {
            return "${note.step.name}${note.alter.symbol}"
        }

        fun toNotationOrNull(note: MusicNote?): String? {
            if (note == null)
                return null
            return toNotation(note)
        }

        const val NOTATION_ERROR = "?"
        const val NOTATION_NULL = "-"
        @StringRes val NOTATION_LYRICS = R.string.tonality_none_text

        fun toNotationOrElse(note: MusicNote?, nullNotation: String): String {
            return toNotationOrNull(note) ?: nullNotation
        }
    }
}
