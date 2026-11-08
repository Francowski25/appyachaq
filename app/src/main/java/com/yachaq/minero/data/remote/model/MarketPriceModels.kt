package com.yachaq.minero.data.remote.model

import com.google.gson.annotations.SerializedName

data class RequestMarketPrice(
    @SerializedName("metal") val metal: String,
    @SerializedName("price") val price: Double
)

data class ResponseMarketPrice(
    @SerializedName("idMarketPrice") val idMarketPrice: String? = null,
    @SerializedName("metal")         val metal: String? = null,
    @SerializedName("price")         val price: Double? = null,
    @SerializedName("updatedAt")     val updatedAt: String? = null,
    @SerializedName("message")       val message: String? = null,
    @SerializedName("listMessage")   val listMessage: List<String>? = null,
    @SerializedName("type")          val type: String? = null
)

data class RequestCalculo(
    @SerializedName("idUser")   val idUser: String,
    @SerializedName("idPlant")  val idPlant: String,
    @SerializedName("material") val material: String,
    @SerializedName("tonelaje") val tonelaje: Double,
    @SerializedName("leyCu")    val leyCu: Double,
    @SerializedName("leyAg")    val leyAg: Double,
    @SerializedName("leyAu")    val leyAu: Double,
    @SerializedName("adelanto") val adelanto: Double? = null
)