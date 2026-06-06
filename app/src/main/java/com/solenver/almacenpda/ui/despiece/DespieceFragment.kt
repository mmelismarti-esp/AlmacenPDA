package com.solenver.almacenpda.ui.despiece

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.solenver.almacenpda.R
import com.solenver.almacenpda.data.api.RetrofitClient
import com.solenver.almacenpda.data.api.models.Despiece
import com.solenver.almacenpda.databinding.FragmentDespieceListBinding
import com.solenver.almacenpda.ui.main.MainActivity
import com.solenver.almacenpda.utils.toast
import kotlinx.coroutines.launch

class DespieceFragment : Fragment() {

    private var _binding: FragmentDespieceListBinding? = null
    private val binding get() = _binding!!
    private val items = mutableListOf<Despiece>()
    private var adapter: DespieceAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentDespieceListBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        adapter = DespieceAdapter(items) { d ->
            (activity as? MainActivity)?.loadFragment(
                DespieceDetalleFragment.newInstance(d.id), addToBackStack = true
            )
        }
        binding.rvDespieces.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDespieces.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { cargar() }
        cargar()
    }

    private fun cargar() {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            try {
                val lista = RetrofitClient.api.getDespieces("enviado")
                items.clear()
                items.addAll(lista)
                adapter?.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                toast("Error: ${e.message}")
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
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
