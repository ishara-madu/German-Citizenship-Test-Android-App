# ============================================
# ProGuard / R8 Rules for Einbürgerungstest App
# ============================================

# ---- General Android ----
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep the application class
-keep class com.pixeleye.einbuergerungstest.lebenindeutschland.BürgertestApp { *; }

# ---- Kotlin Serialization / Coroutines ----
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlin.reflect.jvm.internal.**

# ---- Jetpack Compose ----
# Compose compiler handles most of this, but keep @Composable metadata
-dontwarn androidx.compose.**

# ---- Hilt / Dagger ----
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keepclassmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel *;
}
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.InstallIn class *
-keep @dagger.Module class *
-keep @dagger.hilt.android.EarlyEntryPoint class *

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# ---- Firebase ----
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ---- Firebase Auth ----
-keepattributes Signature
-keepattributes *Annotation*

# ---- Firebase Firestore ----
-keep class com.google.firebase.firestore.** { *; }

# ---- Credentials API (Google Sign-In) ----
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.android.libraries.identity.googleid.**

# ---- Gson ----
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Keep any data classes used with Gson
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ---- OkHttp ----
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# ---- RevenueCat ----
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**

# ---- AdMob / Google Mobile Ads ----
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# ---- ML Kit Translate ----
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-keep class com.google.android.gms.internal.mlkit_translate.** { *; }

# ---- Coil (Image Loading) ----
-dontwarn coil.**
-keep class coil.** { *; }

# ---- Glance AppWidget ----
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# ---- WorkManager ----
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ---- Data classes used in JSON parsing ----
# Keep question entity fields for JSON deserialization
-keepclassmembers class com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.QuestionEntity { *; }
-keepclassmembers class com.pixeleye.einbuergerungstest.lebenindeutschland.data.local.entity.** { *; }

# ---- Prevent stripping of R8 markers ----
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.**

# ---- Keep BuildConfig ----
-keep class com.pixeleye.einbuergerungstest.lebenindeutschland.BuildConfig { *; }