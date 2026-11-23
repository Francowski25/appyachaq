package com.yachaq.minero.data.remote.api

import com.yachaq.minero.data.remote.model.RequestTerms
import com.yachaq.minero.data.remote.model.RequestUserPlant
import com.yachaq.minero.data.remote.model.ResponseTerms
import com.yachaq.minero.data.remote.model.ResponseUserPlant
import retrofit2.Response
import retrofit2.http.*

interface UserPlantApi {
    @POST("user-plant/add")
    suspend fun crearPlanta(@Body request: RequestUserPlant): Response<ResponseUserPlant>

    @GET("user-plant/list")
    suspend fun listarPlantas(): Response<List<ResponseUserPlant>>

    @GET("user-plant/{id}")
    suspend fun obtenerPlanta(@Path("id") idUserPlant: String): Response<ResponseUserPlant>

    @PUT("user-plant/update/{id}")
    suspend fun actualizarPlanta(
        @Path("id") idUserPlant: String,
        @Body request: RequestUserPlant
    ): Response<ResponseUserPlant>

    @DELETE("user-plant/delete/{id}")
    suspend fun eliminarPlanta(@Path("id") idUserPlant: String): Response<ResponseUserPlant>

    @GET("user-plant/terms/{id}")
    suspend fun obtenerTerminos(@Path("id") idUserPlant: String): Response<ResponseTerms>

    @PUT("user-plant/terms/update/{id}")
    suspend fun actualizarTerminos(
        @Path("id") idUserPlant: String,
        @Body request: RequestTerms
    ): Response<ResponseTerms>
}