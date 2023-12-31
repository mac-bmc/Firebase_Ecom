package com.example.firebaseecom.di

import android.app.Application
import android.content.Context
import com.example.firebaseecom.profile.EditProfileActivity
import com.example.firebaseecom.repositories.AuthRepositoryImpl
import com.example.firebaseecom.repositories.ProductRepository
import com.example.firebaseecom.repositories.ProductRepositoryImpl
import com.example.firebaseecom.utils.ToastUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object EkartAppModule {

    @Provides
    fun provideApplicationContext(application: Application):Context=application.applicationContext

    @Provides
    fun providesProductRepository(repository: ProductRepositoryImpl):ProductRepository=repository

    @Provides
    fun provideAuthStateChange(): AuthRepositoryImpl.AuthStateChange {
        return EditProfileActivity().AuthStateChangeImpl()
    }

    @Provides
    fun provideToastUtils(toastUtils: ToastUtils):ToastUtils = toastUtils


}