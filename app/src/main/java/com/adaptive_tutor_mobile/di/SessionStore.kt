package com.adaptive_tutor_mobile.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.adaptive_tutor_mobile.domain.model.User
import com.adaptive_tutor_mobile.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class SessionStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefsLock = Any()
    @Volatile
    private var encryptedPrefs: SharedPreferences? = null

    // ── Encrypted SharedPreferences for token ─────────────────────────────────

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun getEncryptedPrefs(): SharedPreferences? {
        encryptedPrefs?.let { return it }

        synchronized(prefsLock) {
            encryptedPrefs?.let { return it }

            return runCatching { createEncryptedPrefs() }
                .recoverCatching {
                    Log.w(TAG, "Encrypted prefs unavailable, clearing secure storage", it)
                    resetEncryptedStorage()
                    createEncryptedPrefs()
                }
                .onSuccess { encryptedPrefs = it }
                .onFailure { Log.e(TAG, "Failed to initialize encrypted prefs", it) }
                .getOrNull()
        }
    }

    private fun resetEncryptedStorage() {
        encryptedPrefs = null
        context.deleteSharedPreferences(SECURE_PREFS_NAME)
    }

    fun saveAccessToken(token: String) {
        runCatching {
            getEncryptedPrefs()?.edit()?.putString(KEY_ACCESS_TOKEN, token)?.apply()
        }.onFailure {
            Log.e(TAG, "Failed to save access token", it)
            resetEncryptedStorage()
        }
    }

    fun getAccessToken(): String? = runCatching {
        getEncryptedPrefs()?.getString(KEY_ACCESS_TOKEN, null)
    }.onFailure {
        Log.e(TAG, "Failed to read access token", it)
        resetEncryptedStorage()
    }.getOrNull()

    fun clearTokens() {
        runCatching {
            getEncryptedPrefs()?.edit()?.remove(KEY_ACCESS_TOKEN)?.apply()
        }.onFailure {
            Log.e(TAG, "Failed to clear access token", it)
            resetEncryptedStorage()
        }
    }

    // ── DataStore for user data ────────────────────────────────────────────────

    suspend fun saveUser(user: User) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID]    = user.id
            prefs[USER_FIRST] = user.firstName
            prefs[USER_LAST]  = user.lastName
            prefs[USER_EMAIL] = user.email
            prefs[USER_ROLE]  = user.role.name
            prefs[USER_STATUS] = user.status
            prefs[USER_ORG_ID]   = user.organizationId   ?: ""
            prefs[USER_ORG_NAME] = user.organizationName ?: ""
        }
    }

    suspend fun getUser(): User? {
        val prefs = context.dataStore.data.firstOrNull() ?: return null
        val id = prefs[USER_ID] ?: return null
        return User(
            id            = id,
            firstName     = prefs[USER_FIRST]  ?: "",
            lastName      = prefs[USER_LAST]   ?: "",
            email         = prefs[USER_EMAIL]  ?: "",
            role          = UserRole.entries.find { it.name == prefs[USER_ROLE] } ?: UserRole.UNKNOWN,
            status        = prefs[USER_STATUS] ?: "",
            organizationId   = prefs[USER_ORG_ID].takeIf { it?.isNotEmpty() == true },
            organizationName = prefs[USER_ORG_NAME].takeIf { it?.isNotEmpty() == true }
        )
    }

    suspend fun clearAll() {
        clearTokens()
        context.dataStore.edit { it.clear() }
    }

    // ── Force logout event ────────────────────────────────────────────────────

    private val _forceLogoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceLogoutEvent: SharedFlow<Unit> = _forceLogoutEvent.asSharedFlow()

    fun emitForceLogout() {
        _forceLogoutEvent.tryEmit(Unit)
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[THEME_MODE] = mode }
    }

    fun getThemeModeFlow() = context.dataStore.data.map { prefs -> prefs[THEME_MODE] ?: "system" }

    // ── Keys ─────────────────────────────────────────────────────────────────

    companion object {
        private const val TAG = "SessionStore"
        private const val SECURE_PREFS_NAME = "secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private val USER_ID      = stringPreferencesKey("user_id")
        private val USER_FIRST   = stringPreferencesKey("user_first_name")
        private val USER_LAST    = stringPreferencesKey("user_last_name")
        private val USER_EMAIL   = stringPreferencesKey("user_email")
        private val USER_ROLE    = stringPreferencesKey("user_role")
        private val USER_STATUS  = stringPreferencesKey("user_status")
        private val USER_ORG_ID  = stringPreferencesKey("user_org_id")
        private val USER_ORG_NAME = stringPreferencesKey("user_org_name")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
