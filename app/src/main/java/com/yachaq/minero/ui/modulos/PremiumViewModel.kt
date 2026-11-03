package com.yachaq.minero.ui.modulos

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PremiumViewModel : ViewModel() {

    private var billingClient: BillingClient? = null

    private val _precioSuscripcion = MutableStateFlow("Cargando precio...")
    val precioSuscripcion: StateFlow<String> = _precioSuscripcion

    private val _estadoCompra = MutableStateFlow<String?>(null)
    val estadoCompra: StateFlow<String?> = _estadoCompra

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    // ID del producto que creaste en Google Play Console
    private val PRODUCT_ID = "premium_mensual"

    fun inicializarBilling(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        procesarCompra(purchase)
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    _estadoCompra.value = "Compra cancelada por el usuario."
                } else {
                    _estadoCompra.value = "Error en la compra: ${billingResult.debugMessage}"
                }
            }
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    consultarPrecio()
                } else {
                    _precioSuscripcion.value = "Error al conectar con Google Play"
                }
            }

            override fun onBillingServiceDisconnected() {
                _precioSuscripcion.value = "Desconectado de Google Play"
            }
        })
    }

    private fun consultarPrecio() {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val precio = productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                _precioSuscripcion.value = precio ?: "$5.00 / mes"
            }
        }
    }

    fun lanzarCompra(activity: Activity) {
        _cargando.value = true
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: "")
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                billingClient?.launchBillingFlow(activity, billingFlowParams)
                _cargando.value = false
            } else {
                _estadoCompra.value = "Error al obtener el producto."
                _cargando.value = false
            }
        }
    }

    private fun procesarCompra(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Aquí obtienes el Token de compra
            val token = purchase.purchaseToken

            // TODO: Enviar este token al backend (Spring Boot) para validarlo.
            // Por ahora, simulamos que el backend lo validó exitosamente:
            _estadoCompra.value = "SUCCESS"

            // Confirmar la compra para que Google no la reembolse
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(token)
                .build()

            billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                // Compra confirmada
            }
        }
    }
}