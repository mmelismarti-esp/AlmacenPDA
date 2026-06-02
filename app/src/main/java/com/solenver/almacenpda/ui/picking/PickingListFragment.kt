package com.solenver.almacenpda.ui.picking

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.solenver.almacenpda.R
import com.solenver.almacenpda.data.api.RetrofitClient
import com.solenver.almacenpda.data.api.models.AlbaranSalida
import com.solenver.almacenpda.databinding.FragmentPickingListBinding
import com.solenver.almacenpda.ui.main.MainActivity
import com.solenver.almacenpda.utils.toast
import kotlinx.coroutines.launch

class PickingListFragment : Fragment() {

    private var _binding: FragmentPickingListBinding? = null
    private val binding get() = _binding!!
    private val albaranes = mutableListOf<AlbaranSalida>()
    private var adapter: AlbaranAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentPickingListBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        adapter = AlbaranAdapter(albaranes) { alb -> abrirDetalle(alb) }
        binding.rvAlbaranes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlbaranes.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { cargarAlbaranes() }
        cargarAlbaranes()
    }

    private fun cargarAlbaranes() {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            try {
                val lista = RetrofitClient.api.getAlbaranes("en_preparacion")
                albaranes.clear()
                albaranes.addAll(lista)
                adapter?.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                toast("Error: ${e.message}")
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun abrirDetalle(alb: AlbaranSalida) {
        (activity as? MainActivity)?.loadFragment(
            PickingDetailFragment.newInstance(alb.id),
            addToBackStack = true
        )
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

private class AlbaranAdapter(
    private val items: List<AlbaranSalida>,
    private val onClick: (AlbaranSalida) -> Unit
) : RecyclerView.Adapter<AlbaranAdapter.VH>() {

    inner class VH(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_albaran, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = items[position]
        holder.view.findViewById<TextView>(R.id.tvNumero).text =
            "#${a.numero.toString().padStart(4, '0')}"
        holder.view.findViewById<TextView>(R.id.tvObra).text = a.obraNombre ?: "Sin obra"
        holder.view.findViewById<TextView>(R.id.tvFecha).text = a.creadoEnFmt ?: ""
        holder.view.setOnClickListener { onClick(a) }
    }

    override fun getItemCount() = items.size
}
