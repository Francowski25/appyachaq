package com.yachaq.minero.data.remote.api

import com.yachaq.minero.data.remote.model.RequestPlant
import com.yachaq.minero.data.remote.model.ResponsePlant
import retrofit2.Response
import retrofit2.http.*

interface PlantApi {
    @POST("plant/add")
    suspend fun crearPlanta(@Body request: RequestPlant): Response<ResponsePlant>

    @GET("plant/list")
    suspend fun listarPlantas(): Response<List<ResponsePlant>>

    @PUT("plant/update/{id}")
    suspend fun editarPlanta(
        @Path("id") idPlant: String,
        @Body request: RequestPlant
    ): Response<ResponsePlant>

    @DELETE("plant/delete/{id}")
    suspend fun eliminarPlanta(@Path("id") idPlant: String): Response<ResponsePlant>
}