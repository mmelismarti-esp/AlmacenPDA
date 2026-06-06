package com.solenver.almacenpda.ui.printing

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class PrintRequest(
    val tipo: String,
    val datos: Map<String, String>,
    val cantidad: Int = 1,
    val red: Boolean = false,
)

data class PrintResponse(
    val ok: Boolean,
    val resultado: String? = null,
    val error: String? = null,
)

data class PrintStatusResponse(
    val ok: Boolean,
    val conectada: Boolean = false,
    val modelo: String? = null,
    val error: String? = null,
)

private interface PrintApiService {
    @GET("status")
    suspend fun status(): PrintStatusResponse

    @POST("print")
    suspend fun print(@Body request: PrintRequest): PrintResponse
}

object PrintService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private fun api(baseUrl: String): PrintApiService {
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(url)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PrintApiService::class.java)
    }

    suspend fun checkEstado(serverUrl: String): PrintStatusResponse =
        api(serverUrl).status()

    suspend fun imprimir(
        serverUrl: String,
        tipo: String,
        datos: Map<String, String>,
        cantidad: Int = 1,
        red: Boolean = false,
    ): PrintResponse = api(serverUrl).print(PrintRequest(tipo, datos, cantidad, red))
}
