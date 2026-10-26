package com.yachaq.minero.data.remote.api

import com.yachaq.minero.data.remote.model.RequestCalculo
import com.yachaq.minero.data.remote.model.ResponseCalculo
import com.yachaq.minero.data.remote.model.ResponseHistorial
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CalculationApi {

    @POST("calculation/calcular")
    suspend fun calcular(@Body request: RequestCalculo): Response<ResponseCalculo>

    @GET("calculation/historial/{idUser}")
    suspend fun obtenerHistorial(@Path("idUser") idUser: String): Response<List<ResponseHistorial>>

    @DELETE("calculation/delete/{idUser}/{idCalculation}")
    suspend fun eliminarCalculo(
        @Path("idUser") idUser: String,
        @Path("idCalculation") idCalculation: String
    ): Response<ResponseCalculo>

    @DELETE("calculation/deleteAll/{idUser}")
    suspend fun eliminarTodoHistorial(
        @Path("idUser") idUser: String
    ): Response<ResponseCalculo>
}