package com.yachaq.minero.data.remote

import com.yachaq.minero.data.remote.api.*
import com.yachaq.minero.data.remote.model.SesionUsuario
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL_LOCAL = "http://127.0.0.1:8080/"
    private var BASE_URL = BASE_URL_LOCAL

    // ---------- RENDER (comentado, fácil de activar) ----------
    //private const val BASE_URL_RENDER = "https://apirfyachaq-podo.onrender.com/"
    //private var BASE_URL = BASE_URL_RENDER

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
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    var authApi: AuthApi = buildRetrofit().create(AuthApi::class.java)
        private set
    var userApi: UserApi = buildRetrofit().create(UserApi::class.java)
        private set
    var plantApi: PlantApi = buildRetrofit().create(PlantApi::class.java)
        private set
    var termsApi: TermsApi = buildRetrofit().create(TermsApi::class.java)
        private set
    var calculationApi: CalculationApi = buildRetrofit().create(CalculationApi::class.java)
        private set
    var marketPriceApi: MarketPriceApi = buildRetrofit().create(MarketPriceApi::class.java)
        private set
    var userTermsApi: UserTermsApi = buildRetrofit().create(UserTermsApi::class.java)
        private set

    // ---------- CAMBIO DE SERVIDOR (comentado, solo aplica con Render) ----------
    /*
    fun cambiarServidor(usarLocal: Boolean) {
        BASE_URL = if (usarLocal) BASE_URL_LOCAL else BASE_URL_RENDER

        val newRetrofit = buildRetrofit()
        authApi = newRetrofit.create(AuthApi::class.java)
        userApi = newRetrofit.create(UserApi::class.java)
        plantApi = newRetrofit.create(PlantApi::class.java)
        termsApi = newRetrofit.create(TermsApi::class.java)
        calculationApi = newRetrofit.create(CalculationApi::class.java)
        marketPriceApi = newRetrofit.create(MarketPriceApi::class.java)
        userTermsApi = newRetrofit.create(UserTermsApi::class.java)
    }
    */
}