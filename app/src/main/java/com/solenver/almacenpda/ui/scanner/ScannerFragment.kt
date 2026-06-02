package com.solenver.almacenpda.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.solenver.almacenpda.data.api.RetrofitClient
import com.solenver.almacenpda.databinding.FragmentScannerBinding
import com.solenver.almacenpda.utils.fmt
import com.solenver.almacenpda.utils.toast
import kotlinx.coroutines.launch

class ScannerFragment : Fragment() {

    private var _binding: FragmentScannerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBuscar.setOnClickListener {
            val code = binding.etCodigo.text.toString().trim()
            if (code.isNotEmpty()) buscarProducto(code)
        }
        binding.etCodigo.setOnEditorActionListener { _, _, _ ->
            val code = binding.etCodigo.text.toString().trim()
            if (code.isNotEmpty()) buscarProducto(code)
            true
        }
        clearResult()
    }

    fun onScan(code: String) {
        binding.etCodigo.setText(code)
        buscarProducto(code)
    }

    private fun buscarProducto(codigo: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.cardResult.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val r = RetrofitClient.api.lookupProducto(codigo)
                binding.progressBar.visibility = View.GONE
                if (!r.encontrado || r.id == null) {
                    toast("Producto no encontrado")
                    clearResult()
                    return@launch
                }
                binding.tvNombre.text = r.nombre ?: "—"
                binding.tvReferencia.text = r.referencia ?: "—"
                binding.tvMarca.text = r.marca ?: "—"
                binding.tvCodigo.text = r.codigoBarras ?: codigo
                binding.tvUnidad.text = r.unidadMedida ?: "ud"
                binding.tvStockTotal.text = (r.stockTotal ?: 0.0).fmt()
                binding.tvStockLibre.text = (r.stockLibre ?: 0.0).fmt()
                val libre = r.stockLibre ?: 0.0
                binding.tvStockLibre.setTextColor(
                    requireContext().getColor(if (libre > 0) android.R.color.holo_green_light else android.R.color.holo_red_light)
                )
                binding.cardResult.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                toast("Error: ${e.message}")
            }
        }
    }

    private fun clearResult() {
        binding.cardResult.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
