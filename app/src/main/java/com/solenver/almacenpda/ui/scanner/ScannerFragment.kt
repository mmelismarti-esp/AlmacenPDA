package com.solenver.almacenpda.ui.scanner

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
        binding.etScan.requestFocus()
        binding.etScan.showSoftInputOnFocus = false

        binding.etScan.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NONE) {
                val code = binding.etScan.text.toString().trim()
                if (code.isNotEmpty()) {
                    processCode(code)
                }
                true
            } else false
        }
    }

    private fun setupListeners() {
        binding.btnToggleKeyboard.setOnClickListener {
            binding.etScan.showSoftInputOnFocus = true
            binding.etScan.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etScan, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.btnCloseResult.setOnClickListener {
            resetScreen()
        }

        binding.btnReservas.setOnClickListener {
            // Se asume que el objeto actual tiene el desglose de reservas
        }
    }

    fun onScan(code: String) {
        binding.etScan.setText(code)
        processCode(code)
    }

    private fun processCode(code: String) {
        if (isInternalOrderLabel(code)) {
            handleInternalOrder(code)
            return
        }
        lookupProduct(code)
    }

    private fun isInternalOrderLabel(code: String): Boolean {
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

        binding.etScan.setText("")
        binding.etScan.showSoftInputOnFocus = false
        binding.etScan.requestFocus()
    }

    private fun showReservasBreakdown(product: LookupResponse) {
        if (product.reservas.isNullOrEmpty()) {
            toast("No hay reservas activas")
            return
        }

        val unidad = product.unidadMedida ?: "ud"

        val items: Array<CharSequence> = product.reservas.map { reserva ->
            val sb = SpannableStringBuilder()

            // Número de obra en negrita (si existe) + nombre
            if (reserva.pedidoId.isNotEmpty()) {
                val numText = "[${reserva.pedidoId}] "
                sb.append(numText)
                sb.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, numText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            sb.append(reserva.cliente)

            // Nueva línea: cantidad + badge de estado con color de fondo
            sb.append("\n${reserva.cantidad.fmt()} $unidad  ")

            val bgColor = when (reserva.estado) {
                "En stock"       -> 0xFF43A047.toInt() // verde
                "En preparación" -> 0xFFEF6C00.toInt() // naranja
                "Preparado"      -> 0xFF1E88E5.toInt() // azul
                "Pedido"         -> 0xFF00897B.toInt() // teal
                "Por pedir"      -> 0xFFF9A825.toInt() // ámbar
                else             -> 0xFF757575.toInt() // gris
            }
            val estadoStart = sb.length
            sb.append(" ${reserva.estado} ")
            sb.setSpan(BackgroundColorSpan(bgColor),        estadoStart, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(ForegroundColorSpan(Color.WHITE),    estadoStart, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(StyleSpan(Typeface.BOLD),            estadoStart, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            sb as CharSequence
        }.toTypedArray()

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
