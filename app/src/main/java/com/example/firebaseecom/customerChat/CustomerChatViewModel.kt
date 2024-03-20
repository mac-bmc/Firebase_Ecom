package com.example.firebaseecom.customerChat

import androidx.lifecycle.ViewModel
import com.example.firebaseecom.repositories.BrainShopRepository
import com.example.firebaseecom.repositories.DatabaseRepository
import com.example.firebaseecom.repositories.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel

class CustomerChatViewModel @Inject constructor(
    private val brainRepository: BrainShopRepository,
    val firestoreRepository: FirestoreRepository,
    val databaseRepository: DatabaseRepository
) : ViewModel()