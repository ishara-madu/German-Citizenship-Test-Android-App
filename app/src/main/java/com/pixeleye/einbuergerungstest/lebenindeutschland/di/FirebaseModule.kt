package com.pixeleye.einbuergerungstest.lebenindeutschland.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.AuthService
import com.pixeleye.einbuergerungstest.lebenindeutschland.data.remote.AuthServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthService(authServiceImpl: AuthServiceImpl): AuthService = authServiceImpl
}
