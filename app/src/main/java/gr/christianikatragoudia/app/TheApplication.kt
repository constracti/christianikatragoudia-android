package gr.christianikatragoudia.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import gr.christianikatragoudia.app.data.TheDatabase
import gr.christianikatragoudia.app.data.TheSettings

class TheApplication : Application() {

    private lateinit var database: TheDatabase

    private val settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private lateinit var settings: TheSettings

    override fun onCreate() {
        super.onCreate()
        database = TheDatabase.getDatabase(this)
        settings = TheSettings(settingsDataStore)
    }

    fun getDatabase(): TheDatabase {
        return database
    }

    fun getSettings(): TheSettings {
        return settings
    }
}
