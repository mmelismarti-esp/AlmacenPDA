package com.solenver.almacenpda.utils

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import com.solenver.almacenpda.R
import com.solenver.almacenpda.data.local.PreferencesManager

object ThemeHelper {
    fun applyTheme(activity: Activity) {
        val themeName = PreferencesManager.getTheme(activity)
        val themeResId = getThemeResId(themeName)
        activity.setTheme(themeResId)
    }

    fun getThemeResId(themeName: String): Int {
        return when (themeName) {
            "Azul" -> R.style.Theme_AlmacenPDA_Azul
            "Cielo" -> R.style.Theme_AlmacenPDA_Cielo
            "Verde azulado" -> R.style.Theme_AlmacenPDA_VerdeAzulado
            "Verde" -> R.style.Theme_AlmacenPDA_Verde
            "Lima" -> R.style.Theme_AlmacenPDA_Lima
            "Ámbar" -> R.style.Theme_AlmacenPDA_Ambar
            "Naranja" -> R.style.Theme_AlmacenPDA_Naranja
            "Rojo" -> R.style.Theme_AlmacenPDA_Rojo
            "Rosa" -> R.style.Theme_AlmacenPDA_Rosa
            "Morado" -> R.style.Theme_AlmacenPDA_Morado
            else -> R.style.Theme_AlmacenPDA_Azul
        }
    }

    fun getColorFromAttr(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}