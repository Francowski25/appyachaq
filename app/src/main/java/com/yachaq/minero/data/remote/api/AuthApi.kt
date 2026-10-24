package com.yachaq.minero.data.remote.api

import com.yachaq.minero.data.remote.model.AuthLoginRequest
import com.yachaq.minero.data.remote.model.AuthRegisterRequest
import com.yachaq.minero.data.remote.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: AuthLoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: AuthRegisterRequest): Response<LoginResponse>
}