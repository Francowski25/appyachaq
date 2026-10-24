package com.yachaq.minero.data.remote.api

import com.yachaq.minero.data.remote.model.RequestTerms
import com.yachaq.minero.data.remote.model.ResponseTerms
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface TermsApi {
    @GET("terms/get/{idPlant}")
    suspend fun obtenerTerminos(@Path("idPlant") idPlant: String): Response<ResponseTerms>

    @PUT("terms/update/{idPlant}")
    suspend fun actualizarTerminos(
        @Path("idPlant") idPlant: String,
        @Body request: RequestTerms
    ): Response<ResponseTerms>
}