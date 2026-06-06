package com.solenver.almacenpda.data.api.models

import com.google.gson.annotations.SerializedName

data class Despiece(
    val id: Int,
    val estado: String,
    @SerializedName("obra_nombre") val obraNombre: String,
    @SerializedName("creado_en") val creadoEn: String,
    @SerializedName("n_lineas") val nLineas: Int,
    @SerializedName("n_lineas_con_stock") val nLineasConStock: Int
)

data class DespieceDetalle(
    val id: Int,
    val estado: String,
    @SerializedName("obra_nombre") val obraNombre: String,
    @SerializedName("creado_en") val creadoEn: String,
    val lineas: List<DespieceLinea>
)

data class DespieceLinea(
    val id: Int,
    @SerializedName("producto_id") val productoId: Int,
    val nombre: String,
    @SerializedName("codigo_barras") val codigoBarras: String?,
    @SerializedName("unidad_medida") val unidadMedida: String,
    @SerializedName("cantidad_solicitada") val cantidadSolicitada: Double,
    @SerializedName("cantidad_reservada") val cantidadReservada: Double,
    @SerializedName("cantidad_disponible") val cantidadDisponible: Double,
    @SerializedName("estado_pedido") val estadoPedido: String?,
    val preparado: Boolean,
    var checked: Boolean = false
)
