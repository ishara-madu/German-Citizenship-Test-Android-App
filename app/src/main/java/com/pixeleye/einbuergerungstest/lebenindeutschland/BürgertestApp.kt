package com.pixeleye.einbuergerungstest.lebenindeutschland
import com.pixeleye.einbuergerungstest.lebenindeutschland.BuildConfig

import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BürgertestApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // RevenueCat SDK Configuration
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(
                this,
                BuildConfig.REVENUECAT_API_KEY
            ).build()
        )

        // AdMob SDK Configuration
        com.google.android.gms.ads.MobileAds.initialize(this) { }
    }
}
