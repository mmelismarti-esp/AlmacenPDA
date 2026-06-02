package com.solenver.almacenpda.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object PreferencesManager {

    private const val FILE_NAME = "almacen_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_ROL = "user_rol"

    private fun prefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSession(context: Context, token: String, nombre: String, rol: String) {
        prefs(context).edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_NAME, nombre)
            .putString(KEY_USER_ROL, rol)
            .apply()
    }

    fun getToken(context: Context): String? = prefs(context).getString(KEY_TOKEN, null)
    fun getUserName(context: Context): String = prefs(context).getString(KEY_USER_NAME, "") ?: ""
    fun getUserRol(context: Context): String = prefs(context).getString(KEY_USER_ROL, "") ?: ""

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
