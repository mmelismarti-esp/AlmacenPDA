package com.solenver.almacenpda.ui.salida

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.solenver.almacenpda.databinding.FragmentModulePlaceholderBinding

class AlbaranSalidaFragment : Fragment() {

    private var _binding: FragmentModulePlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentModulePlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvModuleIcon.text = "📤"
        binding.tvModuleTitle.text = "Albarán de Salida"
        binding.tvModuleSubtitle.text = "Confirmar entrega de material en obra"

        binding.tvModuleDescription.text =
            "Este módulo muestra los albaranes de salida que han sido preparados en el " +
            "almacén y están listos para ser entregados en obra. El operario puede buscar " +
            "el albarán, revisar los materiales que contiene y confirmar que la entrega " +
            "se ha realizado correctamente, cambiando el estado a 'entregado'.\n\n" +
            "Permite también registrar incidencias si algún material no se ha podido entregar."

        binding.tvModuleFeatures.text =
            "• Listar albaranes en estado 'preparado' pendientes de entregar\n" +
            "• Buscar albarán por número o escanear código\n" +
            "• Ver el detalle de materiales incluidos en el albarán\n" +
            "• Confirmar entrega completa (cambia estado a 'entregado')\n" +
            "• Registrar incidencias parciales (material no entregado)\n" +
            "• Añadir nota o firma de recepción"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
