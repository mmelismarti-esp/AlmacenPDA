package com.solenver.almacenpda.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.PopupMenu
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.solenver.almacenpda.R
import com.solenver.almacenpda.data.local.PreferencesManager
import com.solenver.almacenpda.databinding.ActivityMainBinding
import com.solenver.almacenpda.ui.inventory.InventoryFragment
import com.solenver.almacenpda.ui.login.LoginActivity
import com.solenver.almacenpda.ui.picking.PickingListFragment
import com.solenver.almacenpda.ui.reception.ReceptionFragment
import com.solenver.almacenpda.ui.scanner.ScannerFragment
import com.solenver.almacenpda.ui.settings.SettingsFragment
import com.solenver.almacenpda.ui.taller.TallerMenuFragment
import com.solenver.almacenpda.utils.ScannerHelper
import com.solenver.almacenpda.utils.ThemeHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val scannerHelper = ScannerHelper { code -> forwardScanToFragment(code) }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvUserName.text = PreferencesManager.getUserName(this)

        binding.bottomNav.setOnItemSelectedListener { item -> onNavSelected(item) }
        binding.bottomNav.selectedItemId = R.id.nav_scanner

        binding.userProfileContainer.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.user_profile_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_settings -> {
                        loadFragment(SettingsFragment(), addToBackStack = true)
                        true
                    }
                    R.id.action_logout -> {
                        confirmLogout()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Confirmar") { _, _ -> logout() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        PreferencesManager.clear(this)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun onNavSelected(item: MenuItem): Boolean {
        val fragment = when (item.itemId) {
            R.id.nav_scanner   -> ScannerFragment()
            R.id.nav_reception -> ReceptionFragment()
            R.id.nav_picking   -> PickingListFragment()
            R.id.nav_inventory -> InventoryFragment()
            R.id.nav_taller    -> TallerMenuFragment()
            else -> return false
        }
        loadFragment(fragment)
        return true
    }

    fun loadFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val tx = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
        if (addToBackStack) tx.addToBackStack(null)
        tx.commit()
    }

    // Intercept ALL key events and route scanner input to the current fragment
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (scannerHelper.onKeyDown(event.keyCode, event)) return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun forwardScanToFragment(code: String) {
        val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        when (current) {
            is ScannerFragment   -> current.onScan(code)
            is ReceptionFragment -> current.onScan(code)
            is InventoryFragment -> current.onScan(code)
        }
    }
}
