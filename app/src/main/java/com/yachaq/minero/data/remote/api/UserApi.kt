package com.yachaq.minero.data.remote.api

import com.yachaq.minero.data.remote.model.RequestUserAdmin
import com.yachaq.minero.data.remote.model.ResponseUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {

    @POST("user/add")
    suspend fun crearUsuario(@Body request: RequestUserAdmin): Response<ResponseUser>

    @GET("user/list")
    suspend fun listarUsuarios(): Response<List<ResponseUser>>

    @PUT("user/update/{id}")
    suspend fun editarUsuario(
        @Path("id") idUser: String,
        @Body request: RequestUserAdmin
    ): Response<ResponseUser>

    @DELETE("user/delete/{id}")
    suspend fun eliminarUsuario(@Path("id") idUser: String): Response<ResponseUser>
}