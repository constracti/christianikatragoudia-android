package gr.christianikatragoudia.app.data

data class SongTitle(
    val id: Int,
    val title: String,
    val excerpt: String,
) {

    constructor(song: Song) : this(
        id = song.id,
        title = song.title,
        excerpt = firstLine(song.excerpt),
    )

    constructor(songMatch: SongMatch): this(
        id = songMatch.id,
        title = songMatch.title,
        excerpt = firstLine(songMatch.excerpt),
    )

    fun simplifyExcerpt(): SongTitle {
        return copy(excerpt = firstLine(excerpt))
    }

    companion object {

        private fun firstLine(excerpt: String): String {
            return excerpt.split(Regex("\r\n|\r|\n")).first()
        }
    }
}
