package com.markduenas.android.apigen.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of SettingsRepository using NSUserDefaults
 */
actual class SettingsRepository {

    private val userDefaults = NSUserDefaults.standardUserDefaults

    private val _preferencesFlow = MutableStateFlow(loadPreferences())

    actual val preferencesFlow: Flow<UserPreferences> = _preferencesFlow.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        return UserPreferences(
            adsRemoved = userDefaults.boolForKey(KEY_ADS_REMOVED),
            purchaseToken = userDefaults.stringForKey(KEY_PURCHASE_TOKEN)
        )
    }

    private fun updateFlow() {
        _preferencesFlow.value = loadPreferences()
    }

    actual suspend fun getAdsRemoved(): Boolean {
        return userDefaults.boolForKey(KEY_ADS_REMOVED)
    }

    actual suspend fun setAdsRemoved(removed: Boolean) {
        userDefaults.setBool(removed, KEY_ADS_REMOVED)
        userDefaults.synchronize()
        updateFlow()
    }

    actual suspend fun setPurchaseToken(token: String?) {
        if (token != null) {
            userDefaults.setObject(token, KEY_PURCHASE_TOKEN)
        } else {
            userDefaults.removeObjectForKey(KEY_PURCHASE_TOKEN)
        }
        userDefaults.synchronize()
        updateFlow()
    }

    actual suspend fun getPurchaseToken(): String? {
        return userDefaults.stringForKey(KEY_PURCHASE_TOKEN)
    }

    actual suspend fun clearAll() {
        userDefaults.removeObjectForKey(KEY_ADS_REMOVED)
        userDefaults.removeObjectForKey(KEY_PURCHASE_TOKEN)
        userDefaults.synchronize()
        updateFlow()
    }

    companion object {
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
