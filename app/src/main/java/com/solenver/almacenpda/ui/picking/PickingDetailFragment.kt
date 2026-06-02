package com.solenver.almacenpda.ui.picking

import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.solenver.almacenpda.R
import com.solenver.almacenpda.data.api.RetrofitClient
import com.solenver.almacenpda.data.api.models.*
import com.solenver.almacenpda.databinding.FragmentPickingDetailBinding
import com.solenver.almacenpda.utils.fmt
import com.solenver.almacenpda.utils.toast
import kotlinx.coroutines.launch

class PickingDetailFragment : Fragment() {

    companion object {
        private const val ARG_ID = "albaran_id"
        fun newInstance(id: Int) = PickingDetailFragment().apply {
            arguments = Bundle().also { it.putInt(ARG_ID, id) }
        }
    }

    private var _binding: FragmentPickingDetailBinding? = null
    private val binding get() = _binding!!
    private var albaranId = 0
    private var lineas = mutableListOf<LineaAlbaran>()
    private var adapter: LineaAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentPickingDetailBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        albaranId = arguments?.getInt(ARG_ID) ?: return
        adapter = LineaAdapter(lineas,
            onCheck = { pos -> toggleCheck(pos) },
            onIncidencia = { pos -> mostrarIncidencia(pos) }
        )
        binding.rvLineas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLineas.adapter = adapter
        binding.btnPreparado.setOnClickListener { confirmarPreparado() }
        cargarDetalle()
    }

    private fun cargarDetalle() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val alb = RetrofitClient.api.getAlbaran(albaranId)
                binding.tvTitulo.text = "#${alb.numero.toString().padStart(4, '0')} · ${alb.obraNombre ?: "Sin obra"}"
                lineas.clear()
                lineas.addAll(alb.lineas ?: emptyList())
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
        lineas[pos].checked = !lineas[pos].checked
        adapter?.notifyItemChanged(pos)
        updateProgress()
    }

    private fun mostrarIncidencia(pos: Int) {
        val linea = lineas[pos]
        val razones = arrayOf("Sin stock", "No sacar", "Devolver al stock")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Incidencia: ${linea.productoNombre}")
            .setItems(razones) { _, which ->
                val razon = when (which) { 0 -> "sin_stock"; 1 -> "no_sacar"; else -> "devolver_stock" }
                aplicarIncidencia(pos, razon)
            }
            .show()
    }

    private fun aplicarIncidencia(pos: Int, razon: String) {
        lifecycleScope.launch {
            try {
                val linea = lineas[pos]
                RetrofitClient.api.aplicarIncidencias(
                    albaranId,
                    IncidenciaRequest(listOf(IncidenciaLinea(linea.id, 0.0, razon)))
                )
                lineas.removeAt(pos)
                adapter?.notifyDataSetChanged()
                updateProgress()
                toast("Incidencia registrada")
            } catch (e: Exception) { toast("Error: ${e.message}") }
        }
    }

    private fun confirmarPreparado() {
        val pendientes = lineas.count { !it.checked }
        if (pendientes > 0) {
            toast("Quedan $pendientes artículo(s) sin marcar")
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Marcar como preparado?")
            .setMessage("Se confirma que todo el material está listo.")
            .setPositiveButton("Confirmar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        RetrofitClient.api.prepararAlbaran(albaranId)
                        toast("Albarán marcado como preparado")
                        parentFragmentManager.popBackStack()
                    } catch (e: Exception) { toast("Error: ${e.message}") }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateProgress() {
        val total = lineas.size
        val done = lineas.count { it.checked }
        binding.tvProgress.text = "$done / $total preparados"
        binding.progressBar2.max = total
        binding.progressBar2.progress = done
        binding.btnPreparado.isEnabled = total > 0 && done == total
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

private class LineaAdapter(
    private val lineas: List<LineaAlbaran>,
    private val onCheck: (Int) -> Unit,
    private val onIncidencia: (Int) -> Unit
) : RecyclerView.Adapter<LineaAdapter.VH>() {

    inner class VH(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_picking_line, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val l = lineas[position]
        val tvNombre = holder.view.findViewById<TextView>(R.id.tvNombre)
        tvNombre.text = l.productoNombre
        holder.view.findViewById<TextView>(R.id.tvRef).text = l.referencia ?: "—"
        holder.view.findViewById<TextView>(R.id.tvCantidad).text =
            "${l.cantidad.fmt()} ${l.unidadMedida}"
        val chk = holder.view.findViewById<CheckBox>(R.id.checkbox)
        chk.isChecked = l.checked
        if (l.checked) {
            tvNombre.paintFlags = tvNombre.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.view.alpha = 0.5f
        } else {
            tvNombre.paintFlags = tvNombre.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.view.alpha = 1f
        }
        chk.setOnClickListener { onCheck(position) }
        holder.view.setOnClickListener { onCheck(position) }
        holder.view.setOnLongClickListener { onIncidencia(position); true }
    }

    override fun getItemCount() = lineas.size
}
