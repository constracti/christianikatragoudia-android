package gr.christianikatragoudia.app

import androidx.lifecycle.ViewModel
import gr.christianikatragoudia.app.data.SettingsRepo

class MainViewModel(application: TheApplication) : ViewModel() {

    val themeOption = SettingsRepo(application).themeOption
}
