package com.solenver.almacenpda.ui.inventory

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
import com.solenver.almacenpda.data.api.models.InventarioLinea
import com.solenver.almacenpda.data.api.models.InventarioRequest
import com.solenver.almacenpda.databinding.FragmentInventoryBinding
import com.solenver.almacenpda.utils.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private val lineas = mutableListOf<InventarioLinea>()
    private val nombresProductos = mutableMapOf<Int, String>()
    private val unidadesProductos = mutableMapOf<Int, String>()
    private var adapter: InvAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentInventoryBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        adapter = InvAdapter(lineas, nombresProductos, unidadesProductos) { pos -> eliminar(pos) }
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
        binding.btnConfirmar.setOnClickListener { confirmarInventario() }
        binding.btnLimpiar.setOnClickListener { confirmarLimpiar() }
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
                pedirCantidad(r.id, r.nombre ?: "Producto ${r.id}", r.unidadMedida ?: "ud", r.stockTotal ?: 0.0)
            } catch (e: Exception) { toast("Error: ${e.message}") }
        }
    }

    private fun pedirCantidad(productoId: Int, nombre: String, unidad: String, stockSistema: Double) {
        val existing = lineas.find { it.productoId == productoId }
        val input = TextInputEditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(if (existing != null) existing.stockReal.fmt() else "")
            hint = "Stock sistema: ${stockSistema.fmt()} $unidad"
            selectAll()
        }
        AlertDialog.Builder(requireContext())
            .setTitle(nombre)
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val qty = input.text.toString().toDoubleOrNull()
                if (qty == null || qty < 0) { toast("Cantidad no válida"); return@setPositiveButton }
                if (existing != null) {
                    val idx = lineas.indexOf(existing)
                    lineas[idx] = existing.copy(stockReal = qty)
                } else {
                    lineas.add(InventarioLinea(productoId, qty))
                    nombresProductos[productoId] = nombre
                    unidadesProductos[productoId] = unidad
                }
                adapter?.notifyDataSetChanged()
                binding.etScan.setText("")
                updateCounter()
            }
            .setNegativeButton("Cancelar", null)
            .show()
        input.requestFocus()
    }

    private fun eliminar(pos: Int) { lineas.removeAt(pos); adapter?.notifyDataSetChanged(); updateCounter() }

    private fun confirmarLimpiar() {
        AlertDialog.Builder(requireContext())
            .setTitle("¿Borrar conteo?")
            .setMessage("Se eliminarán los ${lineas.size} artículo(s) contados.")
            .setPositiveButton("Borrar") { _, _ -> lineas.clear(); adapter?.notifyDataSetChanged(); updateCounter() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarInventario() {
        if (lineas.isEmpty()) { toast("No hay artículos contados"); return }
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar inventario")
            .setMessage("Se ajustará el stock de ${lineas.size} producto(s). Esta operación no se puede deshacer.")
            .setPositiveButton("Confirmar") { _, _ -> enviarInventario() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun enviarInventario() {
        binding.btnConfirmar.isEnabled = false
        lifecycleScope.launch {
            try {
                val nombre = "Inventario ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}"
                RetrofitClient.api.crearInventario(InventarioRequest(nombre, todayIso(), lineas.toList()))
                toast("Inventario registrado correctamente")
                lineas.clear(); nombresProductos.clear(); unidadesProductos.clear()
                adapter?.notifyDataSetChanged()
                updateCounter()
            } catch (e: Exception) {
                toast("Error: ${e.message}")
            } finally {
                binding.btnConfirmar.isEnabled = true
            }
        }
    }

    private fun updateCounter() {
        binding.tvCounter.text = if (lineas.isEmpty()) "Sin artículos contados" else "${lineas.size} producto(s) en conteo"
        binding.btnConfirmar.isEnabled = lineas.isNotEmpty()
        binding.btnLimpiar.isEnabled = lineas.isNotEmpty()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

private class InvAdapter(
    private val lineas: List<InventarioLinea>,
    private val nombres: Map<Int, String>,
    private val unidades: Map<Int, String>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<InvAdapter.VH>() {

    inner class VH(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory_line, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val l = lineas[position]
        holder.view.findViewById<TextView>(R.id.tvNombre).text = nombres[l.productoId] ?: "Producto ${l.productoId}"
        holder.view.findViewById<TextView>(R.id.tvCantidad).text = "${l.stockReal.fmt()} ${unidades[l.productoId] ?: ""}"
        holder.view.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener { onDelete(position) }
    }

    override fun getItemCount() = lineas.size
}
