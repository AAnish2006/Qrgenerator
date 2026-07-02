package com.example.billing

import android.app.Activity
import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Real Google Play Billing integration for Premium.
 *
 * Design notes (security):
 *  - Product IDs below are NOT secrets — they must match the IDs you configure
 *    in Play Console > Monetize > Products, so they are safe to keep as
 *    constants rather than pulling from `.env`.
 *  - Entitlement (`isPremium`) is never trusted from local storage alone. On
 *    every app start and after every purchase event, we re-query Play Billing
 *    (`queryPurchasesAsync`) and only grant premium if Play reports an active,
 *    purchased, non-pending entry for one of these product IDs. This prevents
 *    bypassing premium by editing local SharedPreferences.
 *  - For production apps handling real money, add server-side receipt
 *    verification: send `purchase.purchaseToken` to your backend, call the
 *    Play Developer API (`purchases.subscriptions.get` /
 *    `purchases.products.get`) there, and only then mark the user premium.
 *    That requires a backend + a service-account key, which must live on the
 *    server only (never bundled in the app) — do not put that credential in
 *    this app's `.env`.
 */
class BillingManager(
    context: Context,
    private val onEntitlementChanged: (Boolean) -> Unit
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"

        // Must match Play Console product IDs exactly.
        const val PRODUCT_MONTHLY = "qr_pro_monthly"
        const val PRODUCT_YEARLY = "qr_pro_yearly"
        const val PRODUCT_LIFETIME = "qr_pro_lifetime"

        val SUBSCRIPTION_PRODUCT_IDS = listOf(PRODUCT_MONTHLY, PRODUCT_YEARLY)
        val ONE_TIME_PRODUCT_IDS = listOf(PRODUCT_LIFETIME)
    }

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetails: StateFlow<Map<String, ProductDetails>> = _productDetails.asStateFlow()

    private val _billingReady = MutableStateFlow(false)
    val billingReady: StateFlow<Boolean> = _billingReady.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _billingReady.value = true
                    refreshEntitlements()
                    queryProductDetailsInternal()
                } else {
                    Log.w(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingReady.value = false
                // The library recommends retrying with backoff; a simple app can
                // just reconnect the next time the Premium screen is opened.
            }
        })
    }

    fun endConnection() {
        billingClient.endConnection()
    }

    private fun queryProductDetailsInternal() {
        val subsProducts = SUBSCRIPTION_PRODUCT_IDS.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val inAppProducts = ONE_TIME_PRODUCT_IDS.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val allProducts = subsProducts + inAppProducts
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(allProducts)
            .build()

        billingClient.queryProductDetails(params) { billingResult, result ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = result.productDetailsList.associateBy { it.productId }
            } else {
                Log.w(TAG, "queryProductDetails failed: ${billingResult.debugMessage}")
            }
        }
    }

    /**
     * Re-checks Play Billing directly (source of truth) and reports whether
     * the user currently owns an active premium entitlement.
     */
    fun refreshEntitlements() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { subsResult, subsPurchases ->
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
            ) { inAppResult, inAppPurchases ->
                val ok = subsResult.responseCode == BillingClient.BillingResponseCode.OK &&
                    inAppResult.responseCode == BillingClient.BillingResponseCode.OK
                if (!ok) {
                    Log.w(TAG, "queryPurchasesAsync failed")
                    return@queryPurchasesAsync
                }

                val allPurchases = subsPurchases + inAppPurchases
                val hasActivePremium = allPurchases.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.products.any {
                            it in SUBSCRIPTION_PRODUCT_IDS || it in ONE_TIME_PRODUCT_IDS
                        }
                }

                allPurchases.forEach { acknowledgeIfNeeded(it) }
                onEntitlementChanged(hasActivePremium)
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        val details = _productDetails.value[productId] ?: run {
            Log.w(TAG, "No ProductDetails loaded for $productId yet")
            return
        }

        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        // Subscriptions require an offer token; one-time products do not.
        details.subscriptionOfferDetails?.firstOrNull()?.let {
            productParamsBuilder.setOfferToken(it.offerToken)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { acknowledgeIfNeeded(it) }
            refreshEntitlements()
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled the purchase flow")
        } else {
            Log.w(TAG, "onPurchasesUpdated error: ${billingResult.debugMessage}")
        }
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "acknowledgePurchase failed: ${result.debugMessage}")
                }
            }
        }
    }
}
