package com.solenver.almacenpda.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.solenver.almacenpda.R
import com.solenver.almacenpda.data.local.PreferencesManager
import com.solenver.almacenpda.databinding.FragmentSettingsBinding
import com.solenver.almacenpda.ui.printing.PrintService
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    data class AppStyle(val name: String, val colorRes: Int)

    private val styles = listOf(
        AppStyle("Azul", R.color.style_azul),
        AppStyle("Cielo", R.color.style_cielo),
        AppStyle("Verde azulado", R.color.style_verde_azulado),
        AppStyle("Verde", R.color.style_verde),
        AppStyle("Lima", R.color.style_lima),
        AppStyle("Ámbar", R.color.style_ambar),
        AppStyle("Naranja", R.color.style_naranja),
        AppStyle("Rojo", R.color.style_rojo),
        AppStyle("Rosa", R.color.style_rosa),
        AppStyle("Morado", R.color.style_morado)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentStyle = PreferencesManager.getTheme(requireContext())

        binding.rvStyles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStyles.adapter = StyleAdapter(styles, currentStyle) { style ->
            PreferencesManager.saveTheme(requireContext(), style.name)
            activity?.recreate()
        }

        // Sección impresora
        binding.etPrinterUrl.setText(PreferencesManager.getPrinterUrl(requireContext()))

        binding.btnTestPrinter.setOnClickListener {
            val url = binding.etPrinterUrl.text.toString().trim()
            if (url.isBlank()) return@setOnClickListener
            PreferencesManager.savePrinterUrl(requireContext(), url)

            binding.tvPrinterStatus.visibility = View.VISIBLE
            binding.tvPrinterStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            binding.tvPrinterStatus.text = getString(R.string.printer_testing)
            binding.btnTestPrinter.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val resp = PrintService.checkEstado(url)
                    if (resp.ok && resp.conectada) {
                        binding.tvPrinterStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success))
                        binding.tvPrinterStatus.text = getString(R.string.printer_ok, resp.modelo ?: "QL-800")
                    } else if (resp.ok) {
                        binding.tvPrinterStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning))
                        binding.tvPrinterStatus.text = getString(R.string.printer_not_connected)
                    } else {
                        binding.tvPrinterStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
                        binding.tvPrinterStatus.text = getString(R.string.printer_error, resp.error ?: "")
                    }
                } catch (e: Exception) {
                    binding.tvPrinterStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
                    binding.tvPrinterStatus.text = getString(R.string.printer_unreachable)
                } finally {
                    binding.btnTestPrinter.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class StyleAdapter(
        private val items: List<AppStyle>,
        private val current: String,
        private val onSelected: (AppStyle) -> Unit
    ) : RecyclerView.Adapter<StyleAdapter.VH>() {

        inner class VH(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_style, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val s = items[position]
            holder.view.findViewById<TextView>(R.id.tvStyleName).text = s.name
            holder.view.findViewById<View>(R.id.viewColorCircle).backgroundTintList = 
                ContextCompat.getColorStateList(requireContext(), s.colorRes)
            
            holder.view.findViewById<RadioButton>(R.id.rbSelected).isChecked = s.name == current

            holder.view.setOnClickListener { onSelected(s) }
        }

        override fun getItemCount() = items.size
    }
}