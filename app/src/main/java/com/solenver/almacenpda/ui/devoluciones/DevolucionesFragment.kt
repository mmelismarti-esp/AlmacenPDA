package com.solenver.almacenpda.ui.devoluciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.solenver.almacenpda.databinding.FragmentModulePlaceholderBinding

class DevolucionesFragment : Fragment() {

    private var _binding: FragmentModulePlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentModulePlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvModuleIcon.text = "↩"
        binding.tvModuleTitle.text = "Devoluciones"
        binding.tvModuleSubtitle.text = "Devolver material desde obra al almacén"

        binding.tvModuleDescription.text =
            "Este módulo permite registrar la devolución de materiales que regresan " +
            "desde una obra al almacén. Cuando un operario trae material sobrante o " +
            "en buen estado de una obra, se escanean los artículos, se indica la cantidad " +
            "devuelta y el sistema actualiza el stock automáticamente.\n\n" +
            "También permite indicar el motivo de la devolución (sobrante, cambio de obra, etc.)."

        binding.tvModuleFeatures.text =
            "• Seleccionar la obra de origen de la devolución\n" +
            "• Escanear productos devueltos uno a uno\n" +
            "• Indicar cantidad y estado del material (bueno / dañado)\n" +
            "• Añadir comentario o motivo de devolución\n" +
            "• Confirmar y registrar la devolución (actualiza stock)\n" +
            "• Historial de devoluciones realizadas"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
