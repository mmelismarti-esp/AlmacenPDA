package com.solenver.almacenpda.ui.reception

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.solenver.almacenpda.R
import com.solenver.almacenpda.data.api.RetrofitClient
import com.solenver.almacenpda.data.api.models.*
import com.solenver.almacenpda.databinding.FragmentReceptionBinding
import com.solenver.almacenpda.utils.*
import kotlinx.coroutines.launch

class ReceptionFragment : Fragment() {

    private var _binding: FragmentReceptionBinding? = null
    private val binding get() = _binding!!

    private val lineas = mutableListOf<EntradaLinea>()
    private val nombresProductos = mutableMapOf<Int, String>() // id → nombre
    private var adapter: LineasAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentReceptionBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        adapter = LineasAdapter(lineas, nombresProductos) { pos -> eliminarLinea(pos) }
        binding.rvLineas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLineas.adapter = adapter

        binding.btnScan.setOnClickListener {
            val code = binding.etScan.text.toString().trim()
            if (code.isNotEmpty()) buscarYAgregar(code)
        }
        binding.etScan.setOnEditorActionListener { _, _, _ ->
            val code = binding.etScan.text.toString().trim()
            if (code.isNotEmpty()) buscarYAgregar(code)
            true
        }
        binding.btnConfirmar.setOnClickListener { confirmarEntrada() }
        updateCounter()
    }

    fun onScan(code: String) {
        binding.etScan.setText(code)
        buscarYAgregar(code)
    }

    private fun buscarYAgregar(codigo: String) {
        lifecycleScope.launch {
            try {
                val r = RetrofitClient.api.lookupProducto(codigo)
                if (!r.encontrado || r.id == null) { toast("Producto no encontrado"); return@launch }
                // Pedir cantidad
                pedirCantidad(r.id, r.nombre ?: "Producto ${r.id}", r.unidadMedida ?: "ud")
            } catch (e: Exception) { toast("Error: ${e.message}") }
        }
    }

    private fun pedirCantidad(productoId: Int, nombre: String, unidad: String) {
        val input = TextInputEditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText("1")
            selectAll()
        }
        AlertDialog.Builder(requireContext())
            .setTitle(nombre)
            .setMessage("Cantidad ($unidad):")
            .setView(input)
            .setPositiveButton("Añadir") { _, _ ->
                val qty = input.text.toString().toDoubleOrNull() ?: 1.0
                if (qty <= 0) { toast("Cantidad no válida"); return@setPositiveButton }
                // Si ya existe el producto, sumar cantidad
                val existing = lineas.indexOfFirst { it.productoId == productoId }
                if (existing >= 0) {
                    lineas[existing] = lineas[existing].copy(cantidad = lineas[existing].cantidad + qty)
                } else {
                    lineas.add(EntradaLinea(productoId, qty, null))
                    nombresProductos[productoId] = nombre
                }
                adapter?.notifyDataSetChanged()
                binding.etScan.setText("")
                updateCounter()
            }
            .setNegativeButton("Cancelar", null)
            .show()
        input.requestFocus()
    }

    private fun eliminarLinea(pos: Int) {
        lineas.removeAt(pos)
        adapter?.notifyDataSetChanged()
        updateCounter()
    }

    private fun confirmarEntrada() {
        if (lineas.isEmpty()) { toast("No hay artículos"); return }
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar entrada")
            .setMessage("¿Registrar entrada de ${lineas.size} artículo(s)?")
            .setPositiveButton("Confirmar") { _, _ -> enviarEntrada() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun enviarEntrada() {
        binding.btnConfirmar.isEnabled = false
        lifecycleScope.launch {
            try {
                val alb = binding.etAlbaran.text.toString().trim().ifEmpty { null }
                RetrofitClient.api.crearEntrada(EntradaRequest(null, alb, todayIso(), null, lineas.toList()))
                toast("Entrada registrada correctamente")
                lineas.clear()
                nombresProductos.clear()
                adapter?.notifyDataSetChanged()
                binding.etAlbaran.setText("")
                binding.etScan.setText("")
                updateCounter()
            } catch (e: Exception) {
                toast("Error: ${e.message}")
            } finally {
                binding.btnConfirmar.isEnabled = true
            }
        }
    }

    private fun updateCounter() {
        binding.tvCounter.text = if (lineas.isEmpty()) "Sin artículos" else "${lineas.size} artículo(s)"
        binding.btnConfirmar.isEnabled = lineas.isNotEmpty()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

private class LineasAdapter(
    private val lineas: List<EntradaLinea>,
    private val nombres: Map<Int, String>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<LineasAdapter.VH>() {

    inner class VH(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_reception_line, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val l = lineas[position]
        holder.view.findViewById<TextView>(R.id.tvNombre).text = nombres[l.productoId] ?: "Producto ${l.productoId}"
        holder.view.findViewById<TextView>(R.id.tvCantidad).text = l.cantidad.fmt()
        holder.view.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener { onDelete(position) }
    }

    override fun getItemCount() = lineas.size
}
