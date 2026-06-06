package com.solenver.almacenpda.ui.picking

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.solenver.almacenpda.R
import com.solenver.almacenpda.data.api.RetrofitClient
import com.solenver.almacenpda.data.api.models.AlbaranSalida
import com.solenver.almacenpda.data.api.models.Despiece
import com.solenver.almacenpda.databinding.FragmentPickingListBinding
import com.solenver.almacenpda.ui.despiece.DespieceDetalleFragment
import com.solenver.almacenpda.ui.main.MainActivity
import com.solenver.almacenpda.utils.toast
import kotlinx.coroutines.launch

class PickingListFragment : Fragment() {

    private var _binding: FragmentPickingListBinding? = null
    private val binding get() = _binding!!

    private val albaranes = mutableListOf<AlbaranSalida>()
    private val despieces = mutableListOf<Despiece>()
    private var albaranAdapter: AlbaranAdapter? = null
    private var despieceAdapter: DespieceAdapter? = null

    private var tabActivo = TAB_ALBARANES

    companion object {
        private const val TAB_ALBARANES = 0
        private const val TAB_DESPIECES = 1
    }

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentPickingListBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        albaranAdapter = AlbaranAdapter(albaranes) { alb ->
            (activity as? MainActivity)?.loadFragment(
                PickingDetailFragment.newInstance(alb.id), addToBackStack = true
            )
        }
        despieceAdapter = DespieceAdapter(despieces) { d ->
            (activity as? MainActivity)?.loadFragment(
                DespieceDetalleFragment.newInstance(d.id), addToBackStack = true
            )
        }

        binding.rvAlbaranes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlbaranes.adapter = albaranAdapter
        binding.rvDespieces.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDespieces.adapter = despieceAdapter

        binding.btnTabAlbaranes.setOnClickListener { seleccionarTab(TAB_ALBARANES) }
        binding.btnTabDespieces.setOnClickListener { seleccionarTab(TAB_DESPIECES) }
        binding.swipeRefresh.setOnRefreshListener { cargarTab() }

        seleccionarTab(TAB_ALBARANES)
    }

    private fun seleccionarTab(tab: Int) {
        tabActivo = tab
        val colorPrimary = ContextCompat.getColor(requireContext(), R.color.accent)
        val colorDark    = ContextCompat.getColor(requireContext(), R.color.bg_dark)
        val colorSurface = ContextCompat.getColor(requireContext(), R.color.bg_surface)
        val colorText    = ContextCompat.getColor(requireContext(), R.color.text_primary)

        if (tab == TAB_ALBARANES) {
            binding.btnTabAlbaranes.backgroundTintList = ColorStateList.valueOf(colorPrimary)
            binding.btnTabAlbaranes.setTextColor(colorDark)
            binding.btnTabDespieces.backgroundTintList = ColorStateList.valueOf(colorSurface)
            binding.btnTabDespieces.setTextColor(colorText)
            binding.rvAlbaranes.visibility = View.VISIBLE
            binding.rvDespieces.visibility = View.GONE
            binding.tvEmpty.text = "No hay albaranes pendientes"
        } else {
            binding.btnTabDespieces.backgroundTintList = ColorStateList.valueOf(colorPrimary)
            binding.btnTabDespieces.setTextColor(colorDark)
            binding.btnTabAlbaranes.backgroundTintList = ColorStateList.valueOf(colorSurface)
            binding.btnTabAlbaranes.setTextColor(colorText)
            binding.rvAlbaranes.visibility = View.GONE
            binding.rvDespieces.visibility = View.VISIBLE
            binding.tvEmpty.text = "No hay despieces pendientes de preparar"
        }
        cargarTab()
    }

    private fun cargarTab() {
        binding.swipeRefresh.isRefreshing = true
        if (tabActivo == TAB_ALBARANES) cargarAlbaranes() else cargarDespieces()
    }

    private fun cargarAlbaranes() {
        lifecycleScope.launch {
            try {
                val lista = RetrofitClient.api.getAlbaranes("en_preparacion")
                albaranes.clear()
                albaranes.addAll(lista)
                albaranAdapter?.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) { toast("Error: ${e.message}") }
            finally { binding.swipeRefresh.isRefreshing = false }
        }
    }

    private fun cargarDespieces() {
        lifecycleScope.launch {
            try {
                val lista = RetrofitClient.api.getDespieces("enviado")
                despieces.clear()
                despieces.addAll(lista)
                despieceAdapter?.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) { toast("Error: ${e.message}") }
            finally { binding.swipeRefresh.isRefreshing = false }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

// ── Adapters ──────────────────────────────────────────────────────────────────

private class AlbaranAdapter(
    private val items: List<AlbaranSalida>,
    private val onClick: (AlbaranSalida) -> Unit
) : RecyclerView.Adapter<AlbaranAdapter.VH>() {
    inner class VH(val view: View) : RecyclerView.ViewHolder(view)
    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_albaran, parent, false))
    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: VH, pos: Int) {
        val a = items[pos]
        h.view.findViewById<TextView>(R.id.tvNumero).text =
            "#${a.numero.toString().padStart(4, '0')}"
        h.view.findViewById<TextView>(R.id.tvObra).text = a.obraNombre ?: "Sin obra"
        h.view.findViewById<TextView>(R.id.tvFecha).text = a.creadoEnFmt ?: ""
        h.view.setOnClickListener { onClick(a) }
    }
}

private class DespieceAdapter(
    private val items: List<Despiece>,
    private val onClick: (Despiece) -> Unit
) : RecyclerView.Adapter<DespieceAdapter.VH>() {
    inner class VH(val view: View) : RecyclerView.ViewHolder(view)
    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_despiece, parent, false))
    override fun getItemCount() = items.size
    override fun onBindViewHolder(h: VH, pos: Int) {
        val d = items[pos]
        h.view.findViewById<TextView>(R.id.tvObra).text = d.obraNombre
        h.view.findViewById<TextView>(R.id.tvFecha).text = d.creadoEn
        h.view.findViewById<TextView>(R.id.tvStock).text =
            "${d.nLineasConStock}/${d.nLineas} art."
        h.view.setOnClickListener { onClick(d) }
    }
}
