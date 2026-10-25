package com.yachaq.minero.data.remote

import com.yachaq.minero.data.remote.api.AuthApi
import com.yachaq.minero.data.remote.api.CalculationApi
import com.yachaq.minero.data.remote.api.PlantApi
import com.yachaq.minero.data.remote.api.TermsApi
import com.yachaq.minero.data.remote.api.TrackingApi
import com.yachaq.minero.data.remote.api.UserApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val plantApi: PlantApi by lazy { retrofit.create(PlantApi::class.java) }
    val termsApi: TermsApi by lazy { retrofit.create(TermsApi::class.java) }
    val calculationApi: CalculationApi by lazy { retrofit.create(CalculationApi::class.java) }
    val trackingApi: TrackingApi by lazy { retrofit.create(TrackingApi::class.java) }
}