package com.example.firebaseecom.CartOrder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseecom.model.ProductHomeModel
import com.example.firebaseecom.model.ProductOrderModel
import com.example.firebaseecom.repositories.FirestoreRepository
import com.example.firebaseecom.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(val firestoreRepository: FirestoreRepository) :
    ViewModel() {
    private val _productCartList =
        MutableStateFlow<Resource<List<ProductHomeModel>>>(Resource.Loading())
    val productCartList: StateFlow<Resource<List<ProductHomeModel>>> = _productCartList
    private val _productOrderList =
        MutableStateFlow<Resource<List<ProductOrderModel>>>(Resource.Loading())
    val productOrderList: StateFlow<Resource<List<ProductOrderModel>>> = _productOrderList

    fun getProductFromDest(dest: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = firestoreRepository.getFromDest(dest)
            if (data != null) {
                _productCartList.value = data
            } else {
                _productCartList.value = Resource.Failed("NO Available Data")
            }

        }
    }

    fun getProductFromOrder() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = firestoreRepository.getFromOrders()
            if (data != null) {
                _productOrderList.value = data
            } else {
                _productOrderList.value = Resource.Failed("NO Available Data")
            }

        }
    }


    fun removeFromCart(productModel: ProductHomeModel) {
        viewModelScope.launch(Dispatchers.IO) {
            firestoreRepository.removeFromDest("cart", productModel)
        }
    }


    fun removeFromOrder(productModel: ProductHomeModel) {

        viewModelScope.launch(Dispatchers.IO) {
            firestoreRepository.removeFromDest("orders", productModel)
        }
    }
}