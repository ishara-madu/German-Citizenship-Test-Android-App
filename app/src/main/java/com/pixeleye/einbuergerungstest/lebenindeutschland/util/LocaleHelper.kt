package com.pixeleye.einbuergerungstest.lebenindeutschland.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun updateLocale(context: Context, language: String): Context {
        val localeCode = when (language) {
            "German" -> "de"
            "English" -> "en"
            "Turkish" -> "tr"
            "Arabic" -> "ar"
            "Persian" -> "fa"
            else -> "de"
        }
        
        val locale = Locale(localeCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        // We return a custom wrapper that preserves the Activity Context for Hilt
        return LocalizedContextWrapper(context, config)
    }
}

class LocalizedContextWrapper(base: Context, config: Configuration) : ContextWrapper(base) {
    private val localizedContext = base.createConfigurationContext(config)

    override fun getResources() = localizedContext.resources
    override fun getAssets() = localizedContext.assets
}
