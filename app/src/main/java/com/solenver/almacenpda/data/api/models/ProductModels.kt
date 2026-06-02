package com.solenver.almacenpda.data.api.models

import com.google.gson.annotations.SerializedName

data class Producto(
    val id: Int,
    val nombre: String,
    val referencia: String?,
    val marca: String?,
    @SerializedName("codigo_barras") val codigoBarras: String?,
    @SerializedName("unidad_medida") val unidadMedida: String,
    @SerializedName("stock_minimo") val stockMinimo: Double,
    @SerializedName("stock_total") val stockTotal: Double,
    @SerializedName("stock_libre") val stockLibre: Double,
    @SerializedName("stock_reservado") val stockReservado: Double,
    @SerializedName("necesita_reposicion") val necesitaReposicion: Boolean
)

data class LookupResponse(
    @SerializedName("encontrado_en_bd") val encontrado: Boolean,
    val id: Int?,
    val nombre: String?,
    val referencia: String?,
    val marca: String?,
    @SerializedName("codigo_barras") val codigoBarras: String?,
    @SerializedName("unidad_medida") val unidadMedida: String?,
    @SerializedName("stock_total") val stockTotal: Double?,
    @SerializedName("stock_libre") val stockLibre: Double?
)
