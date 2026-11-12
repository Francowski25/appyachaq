package com.yachaq.minero.data.remote.api

import com.yachaq.minero.data.remote.model.RequestUserTerms
import com.yachaq.minero.data.remote.model.ResponseGenericTerms
import com.yachaq.minero.data.remote.model.ResponseUserTerms
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserTermsApi {

    @GET("userterms/listar/{idUser}")
    suspend fun listar(
        @Path("idUser") idUser: String
    ): Response<List<ResponseUserTerms>>

    @PUT("userterms/guardar")
    suspend fun guardar(
        @Body request: RequestUserTerms
    ): Response<ResponseGenericTerms>

    @POST("userterms/restablecer")
    suspend fun restablecer(
        @Body request: RequestUserTerms
    ): Response<ResponseGenericTerms>
}