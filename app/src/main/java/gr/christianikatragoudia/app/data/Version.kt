package gr.christianikatragoudia.app.data

import androidx.annotation.ArrayRes
import gr.christianikatragoudia.app.R
import java.time.LocalDate


enum class Version(val tag: String, @param:ArrayRes val changes: Int, val date: LocalDate) {
    V_1_0(tag = "1.0", changes = R.array.version_1_0, date = LocalDate.of(2023, 10, 22)),
    V_1_1(tag = "1.1", changes = R.array.version_1_1, date = LocalDate.of(2023, 10, 30)),
    V_1_2(tag = "1.2", changes = R.array.version_1_2, date = LocalDate.of(2023, 11, 16)),
    V_1_3(tag = "1.3", changes = R.array.version_1_3, date = LocalDate.of(2023, 12, 22)),
    V_1_4(tag = "1.4", changes = R.array.version_1_4, date = LocalDate.of(2025, 4, 21)),
    V_1_5(tag = "1.5", changes = R.array.version_1_5, date = LocalDate.of(2025, 7, 1)),
    V_1_6(tag = "1.6", changes = R.array.version_1_6, date = LocalDate.of(2025, 11, 8));

    companion object {

        const val CURRENT = "1.6.0"
    }
}
