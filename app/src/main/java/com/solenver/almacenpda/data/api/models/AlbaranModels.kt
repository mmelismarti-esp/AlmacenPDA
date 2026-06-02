package com.solenver.almacenpda.data.api.models

import com.google.gson.annotations.SerializedName

data class AlbaranSalida(
    val id: Int,
    val numero: Int,
    val estado: String,
    @SerializedName("obra_nombre") val obraNombre: String?,
    @SerializedName("obra_numero") val obraNumero: String?,
    @SerializedName("creado_en_fmt") val creadoEnFmt: String?,
    val lineas: List<LineaAlbaran>?,
    @SerializedName("importe_total") val importeTotal: Double?
)

data class LineaAlbaran(
    val id: Int,
    @SerializedName("producto_id") val productoId: Int,
    @SerializedName("producto_nombre") val productoNombre: String,
    val referencia: String?,
    val fabricante: String?,
    @SerializedName("codigo_barras") val codigoBarras: String?,
    @SerializedName("unidad_medida") val unidadMedida: String,
    val cantidad: Double,
    @SerializedName("precio_coste") val precioCoste: Double?,
    @SerializedName("requiere_serie") val requiereSerie: Int,
    var checked: Boolean = false
)

data class IncidenciaRequest(val lineas: List<IncidenciaLinea>)

data class IncidenciaLinea(
    val id: Int,
    @SerializedName("nueva_cantidad") val nuevaCantidad: Double,
    val razon: String
)
