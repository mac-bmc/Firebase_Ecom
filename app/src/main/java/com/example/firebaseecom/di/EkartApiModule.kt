package com.example.firebaseecom.di

import com.example.firebaseecom.api.EkartApi
import com.example.firebaseecom.api.EkartApiEndPoints
import com.example.firebaseecom.repositories.NetworkRepository
import com.example.firebaseecom.repositories.NetworkRepositoryImpl
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EkartApiModule {

    @Provides
    @Singleton
    fun provideRetrofit() : EkartApi{
        val okHttpClient = OkHttpClient.Builder()

            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.SECONDS)

            .build()
        return Retrofit.Builder()
            .baseUrl(EkartApiEndPoints.END_POINT_BASE.url)
            .client(okHttpClient)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                )
            )
            .build()
            .create(EkartApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkRepository(repository:NetworkRepositoryImpl):NetworkRepository = repository
}
