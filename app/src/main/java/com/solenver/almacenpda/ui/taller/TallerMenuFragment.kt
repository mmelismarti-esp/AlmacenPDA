package com.solenver.almacenpda.ui.taller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.solenver.almacenpda.databinding.FragmentTallerMenuBinding
import com.solenver.almacenpda.ui.despiece.DespieceFragment
import com.solenver.almacenpda.ui.devoluciones.DevolucionesFragment
import com.solenver.almacenpda.ui.obras.ConsultaObraFragment
import com.solenver.almacenpda.ui.inventory.InventoryFragment
import com.solenver.almacenpda.ui.main.MainActivity

class TallerMenuFragment : Fragment() {

    private var _binding: FragmentTallerMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTallerMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardDespiece.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(DespieceFragment(), addToBackStack = true)
        }
        binding.cardDevoluciones.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(DevolucionesFragment(), addToBackStack = true)
        }
        binding.cardConsultaObra.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(ConsultaObraFragment(), addToBackStack = true)
        }
        binding.cardInventario.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(InventoryFragment(), addToBackStack = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
