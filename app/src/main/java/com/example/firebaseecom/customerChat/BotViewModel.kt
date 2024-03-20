package com.example.firebaseecom.customerChat


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseecom.api.ProductOrderDeliveryStatus
import com.example.firebaseecom.repositories.BrainShopRepository
import com.example.firebaseecom.repositories.DatabaseRepository
import com.example.firebaseecom.repositories.FirestoreRepository
import com.example.firebaseecom.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import javax.inject.Inject

@HiltViewModel

class BotViewModel @Inject constructor(
    private val brainRepository: BrainShopRepository,
    val firestoreRepository: FirestoreRepository,
    val databaseRepository: DatabaseRepository
) : ViewModel() {

    companion object {
        var ifSucessfullOrderStatusFetch = false
        var ifSucessfullProductReviewFetch = false
        var incorrectStatusCounter = 0
        var incorrectReviewCounter = 0
    }


    suspend fun getResponse(msg: String): String {

        val botMsg = viewModelScope.async(Dispatchers.IO) {
            when (val response = brainRepository.getBrainResponse(msg)) {
                is Resource.Success -> {
                    Log.d("BrainShopModel", response.data.cnt.toString())
                    response.data.cnt.toString()
                }

                else -> {
                    Log.d("BrainShopModel", "NoResponse")
                    "No Response"
                }
            }

        }
        return botMsg.await()

    }

    suspend fun getOrderStatus(orderId: String): String {
        ifSucessfullOrderStatusFetch = false
        val botResponse = viewModelScope.async(Dispatchers.IO) {
            val orderModel = firestoreRepository.getOrderForChat(orderId)
            Log.d("OrderforChatViewM", orderModel.toString())
            if (orderModel != null) {
                val orderStatusInt = orderModel.productDeliveryStatusCode
                ifSucessfullOrderStatusFetch = true
                ProductOrderDeliveryStatus.values().first { it.statusCode == orderStatusInt }.msg


            } else {
                incorrectStatusCounter++
                "Enter correct order id"
            }


        }
        Log.d("OrderforChatViewM1", botResponse.await())
        return botResponse.await()
    }

    suspend fun getProductReviews(productName: String): String {
        ifSucessfullProductReviewFetch = false
        val botResponse = viewModelScope.async(Dispatchers.IO) {
            val productId = databaseRepository.getProductId(productName)
            Log.d("productId", productId.toString())
            if (productId != null) {
                when (val productReview = firestoreRepository.getProductReview(productId)) {
                    is Resource.Success -> {
                        ifSucessfullProductReviewFetch = true
                        productReview.data[0].productReview
                    }

                    else -> {

                        Log.d("productId", "failed")
                        "No Review"
                    }
                }
            } else {
                incorrectReviewCounter++
                "Not valid product"

            }


        }
        return botResponse.await()!!
    }

    fun resetStatusCounter() {
        incorrectStatusCounter = 0
    }

    fun resetReviewCounter() {
        incorrectStatusCounter = 0
    }

}