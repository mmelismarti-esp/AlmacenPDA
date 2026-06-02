package com.solenver.almacenpda.data.api.models

import com.google.gson.annotations.SerializedName

data class InventarioRequest(
    val nombre: String,
    val fecha: String,
    val lineas: List<InventarioLinea>
)

data class InventarioLinea(
    @SerializedName("producto_id") val productoId: Int,
    @SerializedName("stock_real") val stockReal: Double
)

data class InventarioResponse(val id: Int)
