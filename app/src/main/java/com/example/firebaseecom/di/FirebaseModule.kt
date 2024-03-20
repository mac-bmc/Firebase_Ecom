package com.example.firebaseecom.di

import com.example.firebaseecom.repositories.AuthRepository
import com.example.firebaseecom.repositories.AuthRepositoryImpl
import com.example.firebaseecom.repositories.FirestoreRepository
import com.example.firebaseecom.repositories.FirestoreRepositoryImpl
import com.example.firebaseecom.repositories.StorageRepository
import com.example.firebaseecom.repositories.StorageRepositoryImp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    fun provideFirestore()=FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuth():FirebaseAuth= FirebaseAuth.getInstance()
    @Provides
    @Singleton
    fun provideCloudStorage():FirebaseStorage=FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideRealTimeDB():FirebaseDatabase=FirebaseDatabase.getInstance()

    @Provides
    fun provideAuthRepository(repository: AuthRepositoryImpl): AuthRepository = repository

    @Provides
    fun provideFirestoreRepository(repository: FirestoreRepositoryImpl): FirestoreRepository = repository
    @Provides
    fun provideStorageRepository(repositoryImpl: StorageRepositoryImp): StorageRepository =repositoryImpl



}