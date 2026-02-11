package com.markduenas.android.apigen.settings

import android.content.Context
import android.content.SharedPreferences
import com.markduenas.android.apigen.config.getAndroidContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of SettingsRepository using SharedPreferences
 */
actual class SettingsRepository {

    private val prefs: SharedPreferences by lazy {
        getAndroidContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _preferencesFlow = MutableStateFlow(loadPreferences())

    actual val preferencesFlow: Flow<UserPreferences> = _preferencesFlow.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        return UserPreferences(
            adsRemoved = prefs.getBoolean(KEY_ADS_REMOVED, false),
            purchaseToken = prefs.getString(KEY_PURCHASE_TOKEN, null)
        )
    }

    private fun updateFlow() {
        _preferencesFlow.value = loadPreferences()
    }

    actual suspend fun getAdsRemoved(): Boolean {
        return prefs.getBoolean(KEY_ADS_REMOVED, false)
    }

    actual suspend fun setAdsRemoved(removed: Boolean) {
        prefs.edit().putBoolean(KEY_ADS_REMOVED, removed).apply()
        updateFlow()
    }

    actual suspend fun setPurchaseToken(token: String?) {
        if (token != null) {
            prefs.edit().putString(KEY_PURCHASE_TOKEN, token).apply()
        } else {
            prefs.edit().remove(KEY_PURCHASE_TOKEN).apply()
        }
        updateFlow()
    }

    actual suspend fun getPurchaseToken(): String? {
        return prefs.getString(KEY_PURCHASE_TOKEN, null)
    }

    actual suspend fun clearAll() {
        prefs.edit().clear().apply()
        updateFlow()
    }

    companion object {
        private const val PREFS_NAME = "pi_generator_prefs"
        private const val KEY_ADS_REMOVED = "ads_removed"
        private const val KEY_PURCHASE_TOKEN = "purchase_token"
    }
}

private var settingsRepositoryInstance: SettingsRepository? = null

actual fun getSettingsRepository(): SettingsRepository {
    if (settingsRepositoryInstance == null) {
        settingsRepositoryInstance = SettingsRepository()
    }
    return settingsRepositoryInstance!!
}
