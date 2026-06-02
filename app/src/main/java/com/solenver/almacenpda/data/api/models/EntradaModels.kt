package com.solenver.almacenpda.data.api.models

import com.google.gson.annotations.SerializedName

data class Proveedor(
    val id: Int,
    val nombre: String
)

data class EntradaRequest(
    @SerializedName("proveedor_id") val proveedorId: Int?,
    @SerializedName("numero_albaran") val numeroAlbaran: String?,
    val fecha: String?,
    val notas: String?,
    val lineas: List<EntradaLinea>
)

data class EntradaLinea(
    @SerializedName("producto_id") val productoId: Int,
    val cantidad: Double,
    @SerializedName("precio_coste") val precioCoste: Double?
)

data class EntradaResponse(val id: Int, val ok: Boolean)
