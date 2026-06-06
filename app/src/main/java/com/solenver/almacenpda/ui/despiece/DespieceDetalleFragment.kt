package com.solenver.almacenpda.ui.despiece

import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.solenver.almacenpda.R
import com.solenver.almacenpda.data.api.RetrofitClient
import com.solenver.almacenpda.data.api.models.DespieceLinea
import com.solenver.almacenpda.databinding.FragmentDespieceDetalleBinding
import com.solenver.almacenpda.utils.fmt
import com.solenver.almacenpda.utils.toast
import kotlinx.coroutines.launch

class DespieceDetalleFragment : Fragment() {

    companion object {
        private const val ARG_ID = "despiece_id"
        fun newInstance(id: Int) = DespieceDetalleFragment().apply {
            arguments = Bundle().also { it.putInt(ARG_ID, id) }
        }
    }

    private var _binding: FragmentDespieceDetalleBinding? = null
    private val binding get() = _binding!!
    private var despieceId = 0
    private val lineas = mutableListOf<DespieceLinea>()
    private var adapter: DespieceLineaAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentDespieceDetalleBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        despieceId = arguments?.getInt(ARG_ID) ?: return
        adapter = DespieceLineaAdapter(lineas,
            onCheck = { pos -> toggleCheck(pos) },
            onIncidencia = { pos -> mostrarIncidencia(pos) }
        )
        binding.rvLineas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLineas.adapter = adapter
        binding.btnPreparado.setOnClickListener { confirmarPreparacion() }
        cargarDetalle()
    }

    private fun cargarDetalle() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val d = RetrofitClient.api.getDespieceDetalle(despieceId)
                binding.tvTitulo.text = d.obraNombre
                lineas.clear()
                lineas.addAll(d.lineas)
                adapter?.notifyDataSetChanged()
                updateProgress()
            } catch (e: Exception) {
                toast("Error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun toggleCheck(pos: Int) {
        if (lineas[pos].cantidadDisponible <= 0.0) return
        lineas[pos].checked = !lineas[pos].checked
        adapter?.notifyItemChanged(pos)
        updateProgress()
    }

    fun onScan(code: String) {
        val pos = lineas.indexOfFirst {
            it.codigoBarras == code && it.cantidadDisponible > 0.0
        }
        if (pos < 0) {
            toast("Artículo no encontrado en este despiece")
            return
        }
        if (lineas[pos].checked) {
            toast("Ya preparado: ${lineas[pos].nombre}")
            return
        }
        lineas[pos].checked = true
        adapter?.notifyItemChanged(pos)
        binding.rvLineas.scrollToPosition(pos)
        updateProgress()
        toast("✓ ${lineas[pos].nombre}")
    }

    private fun mostrarIncidencia(pos: Int) {
        val linea = lineas[pos]
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Incidencia: ${linea.nombre}")
            .setItems(arrayOf("Sin stock", "No sacar")) { _, _ ->
                lineas[pos].checked = false
                adapter?.notifyItemChanged(pos)
                updateProgress()
                toast("Incidencia registrada")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarPreparacion() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Confirmar preparación?")
            .setMessage("Se consumirá el stock reservado y el despiece quedará marcado como entregado.")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        RetrofitClient.api.confirmarEntregaDespiece(despieceId)
                        toast("Despiece confirmado ✓")
                        parentFragmentManager.popBackStack()
                    } catch (e: Exception) {
                        toast("Error: ${e.message}")
                    }
                }
            }
            .show()
    }

    private fun updateProgress() {
        val checkable = lineas.count { it.cantidadDisponible > 0.0 }
        val done = lineas.count { it.cantidadDisponible > 0.0 && it.checked }
        binding.tvProgress.text = "$done / $checkable preparados"
        binding.progressBar2.max = if (checkable > 0) checkable else 1
        binding.progressBar2.progress = done
        binding.btnPreparado.isEnabled = checkable > 0 && done == checkable
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

private class DespieceLineaAdapter(
    private val lineas: List<DespieceLinea>,
    private val onCheck: (Int) -> Unit,
    private val onIncidencia: (Int) -> Unit
) : RecyclerView.Adapter<DespieceLineaAdapter.VH>() {

    inner class VH(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_despiece_linea, parent, false))

    override fun getItemCount() = lineas.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val l = lineas[pos]
        val tvNombre   = h.view.findViewById<TextView>(R.id.tvNombre)
        val tvEstado   = h.view.findViewById<TextView>(R.id.tvEstado)
        val chk        = h.view.findViewById<CheckBox>(R.id.checkbox)
        val tvCantidad = h.view.findViewById<TextView>(R.id.tvCantidad)
        val tvUnidad   = h.view.findViewById<TextView>(R.id.tvUnidad)

        tvNombre.text   = l.nombre
        tvCantidad.text = l.cantidadDisponible.fmt()
        tvUnidad.text   = "${l.unidadMedida}\n(de ${l.cantidadSolicitada.fmt()})"

        val tieneStock = l.cantidadDisponible > 0.0
        chk.isEnabled  = tieneStock
        chk.isChecked  = l.checked
        h.view.alpha   = if (tieneStock) 1f else 0.5f

        when {
            !tieneStock -> {
                tvEstado.text = when (l.estadoPedido) {
                    "pedido"    -> "Pedido al proveedor"
                    "por_pedir" -> "Pendiente de pedir"
                    else        -> "Sin stock disponible"
                }
                tvEstado.visibility = View.VISIBLE
            }
            l.cantidadDisponible < l.cantidadSolicitada -> {
                tvEstado.text = "Stock parcial"
                tvEstado.visibility = View.VISIBLE
            }
            else -> tvEstado.visibility = View.GONE
        }

        if (l.checked) {
            tvNombre.paintFlags = tvNombre.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            tvNombre.paintFlags = tvNombre.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        chk.setOnClickListener { onCheck(pos) }
        h.view.setOnClickListener { if (tieneStock) onCheck(pos) }
        h.view.setOnLongClickListener { onIncidencia(pos); true }
    }
}
