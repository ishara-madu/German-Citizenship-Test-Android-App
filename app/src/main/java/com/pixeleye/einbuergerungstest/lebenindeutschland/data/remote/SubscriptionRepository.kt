package com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote

import android.app.Activity
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.GetStoreProductsCallback
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor() {

    companion object {
        private const val TAG = "SubscriptionRepo"
        const val ENTITLEMENT_ID = "Bürgertest Pro"
    }

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _offerings = MutableStateFlow<Offerings?>(null)
    val offerings: StateFlow<Offerings?> = _offerings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refreshCustomerInfo()
        Purchases.sharedInstance.updatedCustomerInfoListener =
            UpdatedCustomerInfoListener { customerInfo ->
                updatePremiumStatus(customerInfo)
            }
    }

    /**
     * Refresh the customer's subscription info from RevenueCat.
     */
    fun refreshCustomerInfo() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                updatePremiumStatus(customerInfo)
            }
            override fun onError(error: PurchasesError) {
                Log.e(TAG, "Error fetching customer info: ${error.message}")
                _error.value = error.message
            }
        })
    }

    /**
     * Fetch available offerings (subscription packages).
     */
    fun fetchOfferings() {
        _isLoading.value = true
        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                _offerings.value = offerings
                _isLoading.value = false
                Log.d(TAG, "Offerings fetched: ${offerings.current?.availablePackages?.size} packages")
            }
            override fun onError(error: PurchasesError) {
                Log.e(TAG, "Error fetching offerings: ${error.message}")
                _error.value = error.message
                _isLoading.value = false
            }
        })
    }

    /**
     * Purchase a specific package. Requires an Activity for Google Play billing.
     */
    fun purchase(activity: Activity, pkg: Package, onComplete: (Boolean) -> Unit) {
        _isLoading.value = true
        _error.value = null

        Purchases.sharedInstance.purchase(
            PurchaseParams.Builder(activity, pkg).build(),
            object : PurchaseCallback {
                override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                    _isLoading.value = false
                    updatePremiumStatus(customerInfo)
                    Log.d(TAG, "Purchase successful!")
                    onComplete(true)
                }
                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    _isLoading.value = false
                    if (userCancelled) {
                        Log.d(TAG, "User cancelled the purchase.")
                    } else {
                        Log.e(TAG, "Purchase error: ${error.message}")
                        _error.value = error.message
                    }
                    onComplete(false)
                }
            }
        )
    }

    /**
     * Restore previously made purchases.
     */
    fun restorePurchases(onComplete: (Boolean) -> Unit) {
        _isLoading.value = true
        _error.value = null
        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                _isLoading.value = false
                updatePremiumStatus(customerInfo)
                val isActive = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                Log.d(TAG, "Restore successful. Premium: $isActive")
                onComplete(isActive)
            }
            override fun onError(error: PurchasesError) {
                _isLoading.value = false
                Log.e(TAG, "Restore error: ${error.message}")
                _error.value = error.message
                onComplete(false)
            }
        })
    }

    /**
     * Login the RevenueCat user with a Firebase UID for cross-device sync.
     */
    fun loginUser(appUserId: String) {
        Purchases.sharedInstance.logIn(
            appUserId,
            object : LogInCallback {
                override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
                    updatePremiumStatus(customerInfo)
                    Log.d(TAG, "User logged in. Created new: $created")
                }
                override fun onError(error: PurchasesError) {
                    Log.e(TAG, "Login error: ${error.message}")
                }
            }
        )
    }

    /**
     * Logout the RevenueCat user (resets to anonymous).
     */
    fun logoutUser() {
        Purchases.sharedInstance.logOut(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                updatePremiumStatus(customerInfo)
                Log.d(TAG, "User logged out.")
            }
            override fun onError(error: PurchasesError) {
                Log.e(TAG, "Logout error: ${error.message}")
            }
        })
    }

    fun clearError() {
        _error.value = null
    }

    private fun updatePremiumStatus(customerInfo: CustomerInfo) {
        val isActive = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
        _isPremium.value = isActive
        Log.d(TAG, "Premium status updated: $isActive")
    }
}
