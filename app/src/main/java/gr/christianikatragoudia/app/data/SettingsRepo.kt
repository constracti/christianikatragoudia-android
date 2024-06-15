package gr.christianikatragoudia.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import gr.christianikatragoudia.app.music.MusicNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val SETTINGS_NAME = "settings"

private val Context.dataStore by preferencesDataStore(name = SETTINGS_NAME)

class SettingsRepo(context: Context) {

    private val dataStore = context.dataStore

    private companion object {

        val HIDDEN_TONALITIES_KEY = stringSetPreferencesKey("hidden_tonalities")
        val THEME_OPTION_KEY = booleanPreferencesKey("theme_option")
        val UPDATE_TIMESTAMP_KEY = intPreferencesKey("update_timestamp")
        val UPDATE_CHECK_KEY = booleanPreferencesKey("update_check")
    }

    val hiddenTonalities: Flow<Set<MusicNote>> = dataStore.data
        .catch {
            if (it is IOException)
                emit(emptyPreferences())
            else
                throw it
        }
        .map {
            val notations = it[HIDDEN_TONALITIES_KEY]
            if (notations == null) {
                MusicNote.ENHARMONIC_TONALITIES
            } else {
                notations.mapNotNull { notation ->
                    MusicNote.byNotation(notation)
                }.toSet()
            }
        }

    suspend fun getHiddenTonalities(): Set<MusicNote> = hiddenTonalities.first()

    suspend fun setHiddenTonalities(tonalities: Set<MusicNote>?) {
        dataStore.edit {
            if (tonalities != null) {
                it[HIDDEN_TONALITIES_KEY] = tonalities.map { note ->
                    MusicNote.toNotation(note)
                }.toSet()
            } else {
                it.remove(HIDDEN_TONALITIES_KEY)
            }
        }
    }

    val themeOption: Flow<ThemeOption> = dataStore.data
        .catch {
            if (it is IOException)
                emit(emptyPreferences())
            else
                throw it
        }
        .map {
            when (it[THEME_OPTION_KEY]) {
                null -> ThemeOption.SYSTEM
                false -> ThemeOption.LIGHT
                true -> ThemeOption.DARK
            }
        }

    suspend fun setThemeOption(option: ThemeOption) {
        dataStore.edit {
            when (option) {
                ThemeOption.SYSTEM -> it.remove(THEME_OPTION_KEY)
                ThemeOption.LIGHT -> it[THEME_OPTION_KEY] = false
                ThemeOption.DARK -> it[THEME_OPTION_KEY] = true
            }
        }
    }

    suspend fun getUpdateTimestamp(): Int? {
        return dataStore.data.firstOrNull()?.get(UPDATE_TIMESTAMP_KEY)
    }

    suspend fun setUpdateTimestamp(timestamp: Int) {
        dataStore.edit {
            it[UPDATE_TIMESTAMP_KEY] = timestamp
        }
    }

    val updateCheck: Flow<Boolean> = dataStore.data
        .catch {
            if (it is IOException)
                emit(emptyPreferences())
            else
                throw it
        }
        .map {
            it[UPDATE_CHECK_KEY] ?: false
        }

    suspend fun hasUpdateCheck(): Boolean = updateCheck.first()

    suspend fun setUpdateCheck(check: Boolean) {
        dataStore.edit {
            it[UPDATE_CHECK_KEY] = check
        }
    }
}
