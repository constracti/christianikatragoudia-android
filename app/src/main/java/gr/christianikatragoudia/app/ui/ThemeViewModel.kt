package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.SettingsRepo
import gr.christianikatragoudia.app.data.ThemeOption
import gr.christianikatragoudia.app.network.TheAnalytics
import kotlinx.coroutines.launch

class ThemeViewModel(private val application: TheApplication) : ViewModel() {

    private val analyticsClass = "/options/theme/"
    private val analyticsName =
        application.getString(R.string.theme) + " – " + application.getString(R.string.app_name)

    init {
        viewModelScope.launch {
            TheAnalytics.logScreenView(analyticsClass, analyticsName)
        }
    }

    val themeOption = SettingsRepo(application).themeOption

    fun setThemeOption(option: ThemeOption) {
        viewModelScope.launch {
            SettingsRepo(application).setThemeOption(option)
        }
    }
}
