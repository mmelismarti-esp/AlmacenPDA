package com.solenver.almacenpda.utils

import android.view.KeyEvent

/**
 * Handles barcode scanner input for generic PDAs (ALPS Z1L, etc.)
 * These devices inject characters as rapid keyboard events + Enter.
 * Buffer chars arriving within SCAN_TIMEOUT_MS and fire callback on Enter.
 */
class ScannerHelper(private val onScan: (String) -> Unit) {

    companion object {
        private const val SCAN_TIMEOUT_MS = 100L
        private const val MIN_BARCODE_LENGTH = 4
    }

    private val buffer = StringBuilder()
    private var lastKeyTime = 0L

    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.source and android.view.InputDevice.SOURCE_KEYBOARD == 0 &&
            event.source and android.view.InputDevice.SOURCE_GAMEPAD == 0) {
            return false
        }

        val now = System.currentTimeMillis()

        // Reset buffer if gap too large (manual typing)
        if (now - lastKeyTime > SCAN_TIMEOUT_MS && buffer.isNotEmpty()) {
            buffer.clear()
        }
        lastKeyTime = now

        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                val code = buffer.toString().trim()
                buffer.clear()
                if (code.length >= MIN_BARCODE_LENGTH) {
                    onScan(code)
                    true
                } else false
            }
            KeyEvent.KEYCODE_DEL -> {
                if (buffer.isNotEmpty()) buffer.deleteCharAt(buffer.length - 1)
                false
            }
            else -> {
                val ch = event.unicodeChar.toChar()
                if (ch.code > 0 && !ch.isISOControl()) buffer.append(ch)
                false
            }
        }
    }

    fun reset() { buffer.clear() }
}
