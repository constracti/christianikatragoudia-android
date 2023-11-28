package gr.christianikatragoudia.app.music

enum class MusicStep(val diatonic: Int, val chromatic: Int) {
    C(diatonic = 0, chromatic = 0),
    D(diatonic = 1, chromatic = 2),
    E(diatonic = 2, chromatic = 4),
    F(diatonic = 3, chromatic = 5),
    G(diatonic = 4, chromatic = 7),
    A(diatonic = 5, chromatic = 9),
    B(diatonic = 6, chromatic = 11);

    companion object {

        private val name2step = entries.associateBy { it.name }
        private val diatonic2step = entries.associateBy { it.diatonic }

        fun getByName(name: String): MusicStep? {
            return name2step[name]
        }

        fun getByDiatonic(diatonic: Int): MusicStep {
            return diatonic2step[diatonic]!!
        }
    }
}
