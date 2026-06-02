package com.solenver.almacenpda.ui.scanner

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.solenver.almacenpda.data.api.RetrofitClient
import com.solenver.almacenpda.data.api.models.LookupResponse
import com.solenver.almacenpda.databinding.FragmentScannerBinding
import com.solenver.almacenpda.utils.fmt
import com.solenver.almacenpda.utils.toast
import kotlinx.coroutines.launch

class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInput()
        setupListeners()
        resetScreen()
    }

    private fun setupInput() {
        // FOCO AUTOMÁTICO pero SIN abrir el teclado
        binding.etScan.requestFocus()
        binding.etScan.showSoftInputOnFocus = false

        // Capturar ENTER (evento del lector de la PDA)
        binding.etScan.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                val code = binding.etScan.text.toString().trim()
                if (code.isNotEmpty()) {
                    processCode(code)
                }
                true
            } else false
        }
    }

    private fun setupListeners() {
        // Botón para abrir el teclado MANUALMENTE
        binding.btnToggleKeyboard.setOnClickListener {
            binding.etScan.showSoftInputOnFocus = true
            binding.etScan.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etScan, InputMethodManager.SHOW_IMPLICIT)
        }

        // Botón de reset (X)
        binding.btnCloseResult.setOnClickListener {
            resetScreen()
        }

        // Click en Unidades Reservadas
        binding.btnReservas.setOnClickListener {
            // Se asume que el objeto actual tiene el desglose de reservas
            // Aquí llamaríamos al modal de reservas
        }
    }

    fun onScan(code: String) {
        binding.etScan.setText(code)
        processCode(code)
    }

    private fun processCode(code: String) {
        // 1. Identificar si es una Etiqueta de Pedido Interno (Placeholder)
        if (isInternalOrderLabel(code)) {
            handleInternalOrder(code)
            return
        }

        // 2. Si no, buscar como Producto en PHP
        lookupProduct(code)
    }

    private fun isInternalOrderLabel(code: String): Boolean {
        // Patrón futuro: Ej. Códigos que empiezan por "ORD-" o tienen 15 dígitos
        return code.startsWith("ORD-", ignoreCase = true)
    }

    private fun handleInternalOrder(code: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Pedido Interno")
            .setMessage("Funcionalidad de pedidos internos ($code) disponible próximamente.")
            .setPositiveButton("Cerrar", null)
            .show()
        resetScreen()
    }

    private fun lookupProduct(code: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.lookupProducto(code)
                if (resp.encontrado && resp.id != null) {
                    showProductResult(resp)
                } else {
                    showUnknownCodeDialog(code)
                }
            } catch (e: Exception) {
                toast("Error de conexión")
                resetScreen()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun showProductResult(product: LookupResponse) {
        binding.resultContainer.visibility = View.VISIBLE
        binding.tvFabricante.text = product.marca ?: "Genérico"
        binding.tvNombre.text = product.nombre
        binding.tvReferencia.text = "REF: ${product.referencia ?: "N/A"}"
        binding.tvDescripcion.text = product.descripcion ?: "Sin descripción técnica."
        
        binding.tvStockLibre.text = "${(product.stockLibre ?: 0.0).fmt()} ${product.unidadMedida}"
        binding.tvStockReservado.text = "${(product.stockReservado ?: 0.0).fmt()} ${product.unidadMedida}"
        binding.tvStockTotal.text = "${(product.stockTotal ?: 0.0).fmt()} ${product.unidadMedida}"

        binding.btnReservas.setOnClickListener {
            showReservasBreakdown(product)
        }

        // Tras mostrar resultado, volvemos a enfocar el campo ocultando el teclado para el siguiente escaneo
        binding.etScan.setText("")
        binding.etScan.showSoftInputOnFocus = false
        binding.etScan.requestFocus()
    }

    private fun showReservasBreakdown(product: LookupResponse) {
        if (product.reservas.isNullOrEmpty()) {
            toast("No hay reservas activas")
            return
        }

        val items = product.reservas.map { "${it.cliente}: ${it.cantidad.fmt()} (${it.estado})" }.toTypedArray()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Desglose de Reservas")
            .setItems(items, null)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun showUnknownCodeDialog(code: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Código desconocido")
            .setMessage("El código '$code' no coincide con ningún producto o pedido.")
            .setPositiveButton("Aceptar") { _, _ -> resetScreen() }
            .setCancelable(false)
            .show()
    }

    private fun resetScreen() {
        binding.resultContainer.visibility = View.GONE
        binding.etScan.setText("")
        binding.etScan.showSoftInputOnFocus = false
        binding.etScan.requestFocus()
        
        // Esconder teclado por si estaba abierto
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etScan.windowToken, 0)
    }

    private fun setLoading(loading: Boolean) {
        binding.loadingBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.tilScan.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
