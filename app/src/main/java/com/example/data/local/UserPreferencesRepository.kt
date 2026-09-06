package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppThemeOption
import com.example.ui.theme.AppFontOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ultinotePrefs by preferencesDataStore(name = "ultinote_prefs")

/**
 * Local-first settings store. Survives rotation, reboot, updates.
 * No account, no cloud — just DataStore on device.
 */
class UserPreferencesRepository(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null
        fun get(context: Context): UserPreferencesRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }

        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_FONT = stringPreferencesKey("font")
        private val KEY_STYLUS_ONLY = booleanPreferencesKey("stylus_only")
        private val KEY_SMOOTHING = booleanPreferencesKey("smoothing")
        private val KEY_AUTO_SNAP = booleanPreferencesKey("auto_snap")
        private val KEY_PRESSURE = floatPreferencesKey("pressure_mult")
        private val KEY_SEEN_ONBOARDING = booleanPreferencesKey("seen_onboarding")
    }

    val theme: Flow<AppThemeOption> = context.ultinotePrefs.data.map { p ->
        try {
            AppThemeOption.valueOf(p[KEY_THEME] ?: AppThemeOption.MATCHA_CREAM.name)
        } catch (_: Exception) { AppThemeOption.MATCHA_CREAM }
    }

    val font: Flow<AppFontOption> = context.ultinotePrefs.data.map { p ->
        try {
            AppFontOption.valueOf(p[KEY_FONT] ?: AppFontOption.MODERN_NEO_GROTESQUE.name)
        } catch (_: Exception) { AppFontOption.MODERN_NEO_GROTESQUE }
    }

    val stylusOnly: Flow<Boolean> = context.ultinotePrefs.data.map { it[KEY_STYLUS_ONLY] ?: false }
    val smoothing: Flow<Boolean> = context.ultinotePrefs.data.map { it[KEY_SMOOTHING] ?: true }
    val autoSnap: Flow<Boolean> = context.ultinotePrefs.data.map { it[KEY_AUTO_SNAP] ?: true }
    val pressureMult: Flow<Float> = context.ultinotePrefs.data.map { it[KEY_PRESSURE] ?: 1.0f }
    val seenOnboarding: Flow<Boolean> = context.ultinotePrefs.data.map { it[KEY_SEEN_ONBOARDING] ?: false }

    suspend fun setTheme(t: AppThemeOption) = context.ultinotePrefs.edit { it[KEY_THEME] = t.name }
    suspend fun setFont(f: AppFontOption) = context.ultinotePrefs.edit { it[KEY_FONT] = f.name }
    suspend fun setStylusOnly(v: Boolean) = context.ultinotePrefs.edit { it[KEY_STYLUS_ONLY] = v }
    suspend fun setSmoothing(v: Boolean) = context.ultinotePrefs.edit { it[KEY_SMOOTHING] = v }
    suspend fun setAutoSnap(v: Boolean) = context.ultinotePrefs.edit { it[KEY_AUTO_SNAP] = v }
    suspend fun setPressureMult(v: Float) = context.ultinotePrefs.edit { it[KEY_PRESSURE] = v.coerceIn(0.4f, 2.0f) }
    suspend fun setSeenOnboarding(v: Boolean) = context.ultinotePrefs.edit { it[KEY_SEEN_ONBOARDING] = v }
}
