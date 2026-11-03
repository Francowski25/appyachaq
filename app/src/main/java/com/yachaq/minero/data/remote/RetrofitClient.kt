package com.yachaq.minero.data.remote

import com.yachaq.minero.data.remote.api.AuthApi
import com.yachaq.minero.data.remote.api.CalculationApi
import com.yachaq.minero.data.remote.api.MarketPriceApi
import com.yachaq.minero.data.remote.api.PlantApi
import com.yachaq.minero.data.remote.api.TermsApi
import com.yachaq.minero.data.remote.api.UserApi
import com.yachaq.minero.data.remote.model.SesionUsuario
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/"

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
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val plantApi: PlantApi by lazy { retrofit.create(PlantApi::class.java) }
    val termsApi: TermsApi by lazy { retrofit.create(TermsApi::class.java) }
    val calculationApi: CalculationApi by lazy { retrofit.create(CalculationApi::class.java) }
    val marketPriceApi: MarketPriceApi by lazy { retrofit.create(MarketPriceApi::class.java) }
}