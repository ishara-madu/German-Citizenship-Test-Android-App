package com.pixeleye.einbuergerungstest.lebenindeutschland.ads
import com.pixeleye.einbuergerungstest.lebenindeutschland.BuildConfig

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

object AdConfig {
    val BANNER_ID = BuildConfig.ADMOB_BANNER_ID
    val INTERSTITIAL_ID = BuildConfig.ADMOB_INTERSTITIAL_ID
}

@Composable
fun BannerAdView(adUnitId: String = AdConfig.BANNER_ID) {
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
