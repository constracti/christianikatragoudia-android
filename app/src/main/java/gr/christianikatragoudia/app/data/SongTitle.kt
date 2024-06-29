package gr.christianikatragoudia.app.data

data class SongTitle(
    val id: Int,
    val title: String,
    val excerpt: String,
) {

    constructor(song: Song) : this(song.id, song.title, song.excerpt)

    fun simplifyExcerpt(): SongTitle {
        return copy(excerpt = excerpt.split(Regex("\r\n|\r|\n")).first())
    }
}
