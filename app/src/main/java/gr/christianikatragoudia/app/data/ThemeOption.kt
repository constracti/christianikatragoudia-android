package gr.christianikatragoudia.app.data

import androidx.annotation.StringRes
import gr.christianikatragoudia.app.R


enum class ThemeOption(@param:StringRes val text: Int) {
    SYSTEM(text = R.string.theme_system),
    LIGHT(text = R.string.theme_light),
    DARK(text = R.string.theme_dark),
}
