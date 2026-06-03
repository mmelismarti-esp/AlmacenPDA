package com.solenver.almacenpda.ui.despiece

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.solenver.almacenpda.databinding.FragmentModulePlaceholderBinding

class DespieceFragment : Fragment() {

    private var _binding: FragmentModulePlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentModulePlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvModuleIcon.text = "📋"
        binding.tvModuleTitle.text = "Despiece"
        binding.tvModuleSubtitle.text = "Preparar materiales de una obra artículo a artículo"

        binding.tvModuleDescription.text =
            "Este módulo permite preparar físicamente el material de un despiece de obra. " +
            "El operario selecciona o escanea la obra, ve la lista de materiales necesarios " +
            "y va escaneando cada artículo conforme lo recoge del almacén. " +
            "Al escanear, el sistema marca el material como preparado y actualiza " +
            "el stock reservado.\n\n" +
            "Es la función principal de preparación de pedidos en el taller, sustituyendo " +
            "al proceso manual de papel en el picking de obras."

        binding.tvModuleFeatures.text =
            "• Seleccionar obra por número o escaneando código\n" +
            "• Ver lista de materiales pendientes de preparar del despiece\n" +
            "• Escanear artículo para marcarlo como preparado\n" +
            "• Indicar cantidad preparada (parcial o completa)\n" +
            "• Alertar si el artículo escaneado no está en el despiece\n" +
            "• Indicar incidencia si no hay stock disponible\n" +
            "• Generar albarán de salida al completar la preparación"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
