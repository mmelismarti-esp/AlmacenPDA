package com.solenver.almacenpda.data.api

import com.solenver.almacenpda.data.api.models.*
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun me(): LoginResponse

    // Productos
    @GET("productos/lookup/{codigo}")
    suspend fun lookupProducto(@Path("codigo") codigo: String): LookupResponse

    @GET("productos")
    suspend fun getProductos(
        @Query("q") q: String? = null,
        @Query("activo") activo: String = "1"
    ): List<Producto>

    // Proveedores
    @GET("proveedores")
    suspend fun getProveedores(): List<Proveedor>

    // Entradas
    @POST("entradas")
    suspend fun crearEntrada(@Body request: EntradaRequest): EntradaResponse

    // Albaranes de salida
    @GET("albaranes-salida")
    suspend fun getAlbaranes(@Query("estado") estado: String): List<AlbaranSalida>

    @GET("albaranes-salida/{id}")
    suspend fun getAlbaran(@Path("id") id: Int): AlbaranSalida

    @PATCH("albaranes-salida/{id}/preparar")
    suspend fun prepararAlbaran(@Path("id") id: Int): Map<String, Any>

    @POST("albaranes-salida/{id}/aplicar-incidencias")
    suspend fun aplicarIncidencias(
        @Path("id") id: Int,
        @Body request: IncidenciaRequest
    ): Map<String, Any>

    // Inventarios
    @POST("inventarios")
    suspend fun crearInventario(@Body request: InventarioRequest): InventarioResponse

    // Despieces
    @GET("despieces")
    suspend fun getDespieces(@Query("estado") estado: String = "enviado"): List<Despiece>

    @GET("despieces/{id}")
    suspend fun getDespieceDetalle(@Path("id") id: Int): DespieceDetalle

    @POST("despieces/{id}/confirmar-entrega")
    suspend fun confirmarEntregaDespiece(@Path("id") id: Int): Map<String, Any>
}
