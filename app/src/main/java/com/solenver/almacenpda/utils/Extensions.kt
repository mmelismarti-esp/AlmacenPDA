package com.solenver.almacenpda.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Fragment.toast(msg: String) = requireContext().toast(msg)

fun Double.fmt(): String {
    return if (this == kotlin.math.floor(this)) this.toInt().toString()
    else String.format(Locale.getDefault(), "%.2f", this)
}

fun todayIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
