package com.yachaq.minero.data.remote.api

import com.yachaq.minero.data.remote.model.RequestMarketPrice
import com.yachaq.minero.data.remote.model.ResponseMarketPrice
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface MarketPriceApi {

    @GET("market-prices/list")
    suspend fun listar(): Response<List<ResponseMarketPrice>>

    @PUT("market-prices/update")
    suspend fun actualizar(@Body request: RequestMarketPrice): Response<ResponseMarketPrice>
}