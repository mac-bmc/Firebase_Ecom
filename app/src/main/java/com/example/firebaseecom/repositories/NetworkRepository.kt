package com.example.firebaseecom.repositories

import android.util.Log
import com.example.firebaseecom.api.EkartApiEndPoints
import com.example.firebaseecom.api.EkartApi
import com.example.firebaseecom.local.ProductDao
import com.example.firebaseecom.model.ProductDetailsModel
import com.example.firebaseecom.model.ProductHomeModel
import com.example.firebaseecom.utils.Resource
import retrofit2.Response
import javax.inject.Inject

interface NetworkRepository {

    suspend fun fetchFromRemote(): List<ProductHomeModel>?
    suspend fun storeInLocal(remoteData: List<ProductHomeModel>?)
    suspend fun fetchFromLocal(): Resource<List<ProductHomeModel>>

    suspend fun fetchDetailsFromRemote(): Resource<List<ProductDetailsModel>?>?
    suspend fun fetchProductByCategory(category:String):Resource<List<ProductHomeModel>>
}

class NetworkRepositoryImpl @Inject constructor(
    private val ekartApi: EkartApi,
    private val productDao: ProductDao
) :
    NetworkRepository {
    lateinit var apiCall: Response<List<ProductHomeModel>?>
    lateinit var apiCallDetails: Response<List<ProductDetailsModel>?>

    override suspend fun fetchFromRemote(): List<ProductHomeModel>? {
        try {
            apiCall = ekartApi.getProducts(EkartApiEndPoints.END_POINT_PRODUCTS.url)
            Log.d("apiCall","success")
        } catch (e: Exception) {
            Log.d("apiCall", e.toString())
        }
        if(::apiCall.isInitialized) {

            if (apiCall.code() != 200) {
                return null
            }
            return apiCall.body()
        }
        return null



    }

    override suspend fun fetchFromLocal(): Resource<List<ProductHomeModel>> {
        var productData = listOf<ProductHomeModel>()
        try {
            productData = productDao.getProductFromDb()
        } catch (e: Exception) {
            Log.d("fetchFromLocal", e.toString())
        }
        if (productData == null) {
            return Resource.Failed("data from local is null")
        }
        return Resource.Success(productData)
    }

    override suspend fun storeInLocal(remoteData: List<ProductHomeModel>?) {
        try {
            productDao.insertProduct(remoteData!!)
        } catch (e: Exception) {
            Log.e("storeinLocal", e.toString())

        }

    }

    override suspend fun fetchDetailsFromRemote(): Resource<List<ProductDetailsModel>?>? {
        try {
            apiCallDetails = ekartApi.getProductDetails(EkartApiEndPoints.END_POINT_PRODUCT_META.url)
        } catch (e: Exception) {
            Log.d("apiCall", e.toString())
        }
        if(::apiCallDetails.isInitialized) {

            if (apiCallDetails.code() != 200) {
                return Resource.Failed("apiFail")
            }
            return Resource.Success(apiCallDetails.body())
        }
        return Resource.Failed("apiFail")

    }

    override suspend fun fetchProductByCategory(category: String): Resource<List<ProductHomeModel>> {
        var productData:MutableList<ProductHomeModel> = mutableListOf()
        try {
            productData=productDao.getProductByCategory(category).toMutableList()
        }
        catch(e:Exception)
        {
            Log.e("fetchProductByCategory",e.toString())
        }
        if(productData==null)
        {
            return Resource.Failed("Database Error,Try Again")
        }
        return Resource.Success(productData)
    }
}
