package com.thatwaz.dadjokes.viewmodel




import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingViewModel(app: Application) : AndroidViewModel(app),
    PurchasesUpdatedListener, BillingClientStateListener {

    // Public: true = show ads, false = user owns remove_ads
    private val _adsEnabled = MutableStateFlow(true)
    val adsEnabled = _adsEnabled.asStateFlow()

    private val productId = "remove_ads" // create this INAPP product in Play Console

    private val billingClient = BillingClient.newBuilder(app)
        .enablePendingPurchases()
        .setListener(this)
        .build()

    private var productDetails: ProductDetails? = null

    init {
        connect()
    }

    private fun connect() {
        if (!billingClient.isReady) billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            queryProductDetails()
            restorePurchases() // set adsEnabled correctly at startup
        }
    }

    override fun onBillingServiceDisconnected() {
        // Try reconnecting later
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            connect()
        }
    }

    private fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        billingClient.queryProductDetailsAsync(params) { _, detailsList ->
            productDetails = detailsList.firstOrNull()
        }
    }

    fun launchRemoveAdsPurchase(activity: Activity) {
        val details = productDetails ?: return
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams
                        .newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            )
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { _, purchases ->
            val ownsRemoveAds = purchases.any { p ->
                p.products.contains(productId) && p.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            _adsEnabled.value = !ownsRemoveAds
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (p in purchases) {
                if (p.products.contains(productId) && p.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge if needed, then disable ads
                    if (!p.isAcknowledged) {
                        val ack = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(p.purchaseToken)
                            .build()
                        billingClient.acknowledgePurchase(ack) { _ -> _adsEnabled.value = false }
                    } else {
                        _adsEnabled.value = false
                    }
                }
            }
        }
    }
}
