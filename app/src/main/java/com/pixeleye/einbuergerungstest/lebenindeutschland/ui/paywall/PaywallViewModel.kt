package com.pixeleye.einbuergerungstest.lebenindeutschland.ui.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.SubscriptionRepository
import com.revenuecat.purchases.Package
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val preferenceManager: com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.PreferenceManager
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = subscriptionRepository.isPremium
    val offerings = subscriptionRepository.offerings
    val isLoading = subscriptionRepository.isLoading
    val error = subscriptionRepository.error

    init {
        subscriptionRepository.fetchOfferings()
    }

    fun purchase(activity: Activity, pkg: Package, onComplete: (Boolean) -> Unit) {
        subscriptionRepository.purchase(activity, pkg) { success ->
            if (success) {
                preferenceManager.setCloudSyncEnabled(true)
            }
            onComplete(success)
        }
    }

    fun restorePurchases(onComplete: (Boolean) -> Unit) {
        subscriptionRepository.restorePurchases { success ->
            if (success) {
                preferenceManager.setCloudSyncEnabled(true)
            }
            onComplete(success)
        }
    }

    fun clearError() {
        subscriptionRepository.clearError()
    }
}
