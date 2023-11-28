package gr.christianikatragoudia.app

import androidx.lifecycle.ViewModel

class MainViewModel(application: TheApplication) : ViewModel() {

    val themeOption = application.getSettings().themeOption
}
