import java.util.Properties

plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.pixeleye.einbuergerungstest.lebenindeutschland"
    compileSdk = 36

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }
    
    val groqApiKey = localProperties.getProperty("GROQ_API_KEY") ?: ""
    val admobAppId = localProperties.getProperty("ADMOB_APP_ID") ?: ""
    val admobBannerId = localProperties.getProperty("ADMOB_BANNER_ID") ?: ""
    val admobInterstitialId = localProperties.getProperty("ADMOB_INTERSTITIAL_ID") ?: ""
    val revenueCatApiKey = localProperties.getProperty("REVENUECAT_API_KEY") ?: ""

    defaultConfig {
        applicationId = "com.pixeleye.einbuergerungstest.lebenindeutschland"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        manifestPlaceholders["ADMOB_APP_ID"] = admobAppId
    }

    buildTypes {
        debug {
            buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
            buildConfigField("String", "ADMOB_BANNER_ID", "\"$admobBannerId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$admobInterstitialId\"")
            buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenueCatApiKey\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
            buildConfigField("String", "ADMOB_BANNER_ID", "\"$admobBannerId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$admobInterstitialId\"")
            buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenueCatApiKey\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module",
                "META-INF/versions/**",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json"
            )
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.id)
    implementation(libs.coil.compose)
    
    implementation(libs.gson)
    implementation(libs.mlkit.translate)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // RevenueCat
    implementation("com.revenuecat.purchases:purchases:10.5.0")

    // AdMob
    implementation("com.google.android.gms:play-services-ads:23.6.0")

    // WorkManager for daily reminders
    implementation(libs.androidx.work.runtime)



    // Glance Widget
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}