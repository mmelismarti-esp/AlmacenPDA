package com.solenver.almacenpda.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.solenver.almacenpda.data.api.RetrofitClient
import com.solenver.almacenpda.data.api.models.LoginRequest
import com.solenver.almacenpda.data.local.PreferencesManager
import com.solenver.almacenpda.databinding.ActivityLoginBinding
import com.solenver.almacenpda.ui.main.MainActivity
import com.solenver.almacenpda.utils.ThemeHelper
import com.solenver.almacenpda.utils.toast
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si ya hay token, ir directamente al main
        PreferencesManager.getToken(this)?.let {
            RetrofitClient.setToken(it)
            goMain()
            return
        }

        binding.btnLogin.setOnClickListener { doLogin() }
        binding.etPassword.setOnEditorActionListener { _, _, _ -> doLogin(); true }
    }

    private fun doLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString()
        if (email.isEmpty() || pass.isEmpty()) { toast("Introduce email y contraseña"); return }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.login(LoginRequest(email, pass))
                PreferencesManager.saveSession(this@LoginActivity, resp.token, resp.nombre, resp.rol)
                RetrofitClient.setToken(resp.token)
                goMain()
            } catch (e: Exception) {
                toast("Error: ${e.message ?: "Credenciales incorrectas"}")
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
