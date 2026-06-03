package com.solenver.almacenpda.ui.obras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.solenver.almacenpda.databinding.FragmentModulePlaceholderBinding

class ConsultaObraFragment : Fragment() {

    private var _binding: FragmentModulePlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentModulePlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvModuleIcon.text = "🏗"
        binding.tvModuleTitle.text = "Consulta de Obra"
        binding.tvModuleSubtitle.text = "Ver el estado del material de una obra"

        binding.tvModuleDescription.text =
            "Este módulo permite consultar rápidamente toda la información de material " +
            "asociada a una obra concreta. Muestra qué materiales están reservados, " +
            "cuáles están pendientes de preparar, cuáles ya han sido preparados y " +
            "cuáles se han entregado.\n\n" +
            "Muy útil para que el operario del taller sepa qué queda por hacer para " +
            "cada obra sin tener que consultar el ordenador."

        binding.tvModuleFeatures.text =
            "• Buscar obra por número, nombre o escaneando código\n" +
            "• Ver lista de materiales del despiece con estado\n" +
            "• Filtrar por estado: reservado / por preparar / preparado / entregado\n" +
            "• Ver stock disponible de cada material en tiempo real\n" +
            "• Identificar materiales con falta de stock\n" +
            "• Acceder al detalle de albaranes de salida de la obra"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
