package com.yachaq.minero.data.remote.api

import com.yachaq.minero.data.remote.model.RequestTracking
import com.yachaq.minero.data.remote.model.ResponseTracking
import retrofit2.Response
import retrofit2.http.*

interface TrackingApi {

    @POST("tracking/add")
    suspend fun crearCarga(@Body request: RequestTracking): Response<ResponseTracking>

    @GET("tracking/list/{idUser}")
    suspend fun listarCargas(@Path("idUser") idUser: String): Response<List<ResponseTracking>>

    @PUT("tracking/status/{id}")
    suspend fun actualizarEstado(
        @Path("id") idTracking: String,
        @Query("nuevoEstado") nuevoEstado: String
    ): Response<ResponseTracking>

    @DELETE("tracking/delete/{id}")
    suspend fun eliminarCarga(@Path("id") idTracking: String): Response<ResponseTracking>
}