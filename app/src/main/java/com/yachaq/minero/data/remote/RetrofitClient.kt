package com.yachaq.minero.data.remote

import com.yachaq.minero.data.remote.api.*
import com.yachaq.minero.data.remote.model.SesionUsuario
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://apirfyachaq-podo.onrender.com/"

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = SesionUsuario.usuarioActual?.token
        val requestWithToken = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        chain.proceed(requestWithToken)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val userApi: UserApi = retrofit.create(UserApi::class.java)
    val plantApi: PlantApi = retrofit.create(PlantApi::class.java)
    val termsApi: TermsApi = retrofit.create(TermsApi::class.java)
    val calculationApi: CalculationApi = retrofit.create(CalculationApi::class.java)
    val marketPriceApi: MarketPriceApi = retrofit.create(MarketPriceApi::class.java)
    val userTermsApi: UserTermsApi = retrofit.create(UserTermsApi::class.java)
    val userPlantApi: UserPlantApi = retrofit.create(UserPlantApi::class.java)
}