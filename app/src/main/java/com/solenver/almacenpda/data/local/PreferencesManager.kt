package com.solenver.almacenpda.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PreferencesManager {

    private const val FILE_NAME = "almacen_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_ROL = "user_rol"
    private const val KEY_APP_THEME = "app_theme"
    private const val KEY_PRINTER_URL = "printer_url"
    private const val DEFAULT_PRINTER_URL = "http://192.168.1.100:5000"

    private var sharedPreferences: SharedPreferences? = null

    private fun getPrefs(context: Context): SharedPreferences {
        if (sharedPreferences == null) {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            sharedPreferences = EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        return sharedPreferences!!
    }

    fun saveSession(context: Context, token: String, nombre: String, rol: String) {
        getPrefs(context).edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_NAME, nombre)
            .putString(KEY_USER_ROL, rol)
            .apply()
    }

    fun getToken(context: Context): String? = getPrefs(context).getString(KEY_TOKEN, null)
    fun getUserName(context: Context): String = getPrefs(context).getString(KEY_USER_NAME, "") ?: ""
    fun getUserRol(context: Context): String = getPrefs(context).getString(KEY_USER_ROL, "") ?: ""

    fun saveTheme(context: Context, themeName: String) {
        getPrefs(context).edit().putString(KEY_APP_THEME, themeName).apply()
    }

    fun getTheme(context: Context): String = getPrefs(context).getString(KEY_APP_THEME, "Azul") ?: "Azul"

    fun savePrinterUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_PRINTER_URL, url).apply()
    }

    fun getPrinterUrl(context: Context): String =
        getPrefs(context).getString(KEY_PRINTER_URL, DEFAULT_PRINTER_URL) ?: DEFAULT_PRINTER_URL

    fun clear(context: Context) {
        val theme = getTheme(context)
        getPrefs(context).edit().clear().apply()
        saveTheme(context, theme)
    }
}
