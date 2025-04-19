package gr.christianikatragoudia.app.data

import gr.christianikatragoudia.app.toUInt

data class SongMatch(
    val id: Int,
    val title: String,
    val excerpt: String,
    val matchInfo: ByteArray,
) {

    private fun decodeMatchInfo(): List<UInt> {
        return matchInfo.toList().windowed(4, 4).map {
            it.toByteArray().toUInt()
        }
    }

    fun getScore(): Float {
        val info = decodeMatchInfo()
        val columnCount = info[1].toInt()
        val columnSize = 3
        val phraseCount = info[0].toInt()
        val phraseSize = columnSize * columnCount
        var score = 0f
        for (phraseIndex in 0..<phraseCount) {
            for (columnIndex in 0..<columnCount) {
                val infoStart = 2 + phraseIndex * phraseSize + columnIndex * columnSize
                val infoStop = infoStart + columnSize
                val infoList = info.slice(infoStart..<infoStop)
                val columnWeight = SongFts.getColumnWeight(columnIndex)
                val phraseFrequency = infoList[1]
                if (phraseFrequency == 0u)
                    continue
                val phraseMatches = infoList[0]
                score += phraseMatches.toFloat() / phraseFrequency.toFloat() * columnWeight
            }
        }
        return score
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SongMatch

        if (id != other.id) return false
        if (title != other.title) return false
        if (excerpt != other.excerpt) return false
        if (!matchInfo.contentEquals(other.matchInfo)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + excerpt.hashCode()
        result = 31 * result + matchInfo.contentHashCode()
        return result
    }
}
