package com.example.firebaseecom.payments


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseecom.model.ProductHomeModel
import com.example.firebaseecom.model.ProductOrderModel
import com.example.firebaseecom.repositories.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

import javax.inject.Inject

@HiltViewModel


class ProductCheckoutViewModel @Inject constructor(val firestoreRepository: FirestoreRepository) :
    ViewModel() {
    @RequiresApi(Build.VERSION_CODES.O)
    val today = LocalDate.now()!!
    @RequiresApi(Build.VERSION_CODES.O)
    val dDay= today.plusDays(5)!!


    @RequiresApi(Build.VERSION_CODES.O)
    fun addToOrders(productList: List<ProductHomeModel>) {

        val productOrderList = mutableListOf<ProductOrderModel>()
        for (productModel in productList) {
            val productOrderModel = ProductOrderModel(
                productModel.productCategory, productModel.productId, productModel.productImage,
                productModel.productPrice, productModel.productTitle, today.toString(), dDay.toString()
            )
            productOrderList.add(productOrderModel)
        }
        viewModelScope.launch(Dispatchers.IO) {
            firestoreRepository.addToOrders(productOrderList)

        }
    }

    fun removeAllFromCart(productList: List<ProductHomeModel>) {
        viewModelScope.launch(Dispatchers.IO) {

            firestoreRepository.removeFromCartIfOrder(productList)
        }
    }
}