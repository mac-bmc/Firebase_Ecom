package com.example.firebaseecom.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.firebaseecom.R
import com.example.firebaseecom.local.ProductDao
import com.example.firebaseecom.model.ProductHomeModel
import com.example.firebaseecom.model.ProductOffersModel
import com.example.firebaseecom.model.ProductOrderReviews
import com.example.firebaseecom.pagination.ProductHomePagingSource
import com.example.firebaseecom.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


interface DatabaseRepository {

    suspend fun mapOfferWithProducts(offerList: List<ProductOffersModel>): List<ProductHomeModel>?
    suspend fun storeInLocal(remoteData: List<ProductHomeModel>?)
    suspend fun fetchFromLocal(): Resource<List<ProductHomeModel>>
    suspend fun searchForProducts(searchQuery: String): Resource<List<ProductHomeModel>>
    suspend fun fetchProductByCategory(category: String): Resource<List<ProductHomeModel>>
    suspend fun getProducts(reviewList: List<ProductOrderReviews>):List<ProductHomeModel>
    suspend fun getProductId(name:String):Int?

    suspend fun getProductByPage():Flow<PagingData<ProductHomeModel>>


}


class DatabaseRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val context: Context
) :
    DatabaseRepository {
    override suspend fun mapOfferWithProducts(offerList: List<ProductOffersModel>): List<ProductHomeModel>? {
        val offerProductList = mutableListOf<ProductHomeModel>()
        offerList.forEach { offer ->
            offerProductList.add(productDao.getOfferProduct(offer.productId!!))
        }
        if (offerProductList.isEmpty())
            return null
        return offerProductList

    }

    override suspend fun storeInLocal(remoteData: List<ProductHomeModel>?) {
        try {
            productDao.deleteAll()
            productDao.insertProduct(remoteData!!)
        } catch (e: Exception) {
            Log.e("storeInLocal", e.toString())

        }

    }

    override suspend fun fetchFromLocal(): Resource<List<ProductHomeModel>> {

        var productData = mutableListOf<ProductHomeModel>()
        try {
            productData = productDao.getProductFromDb().toMutableList()
        } catch (e: Exception) {
            Log.d("fetchFromLocal", e.toString())
        }
        if (productData.isEmpty()) {
            return Resource.Failed(context.getString(R.string.data_from_local_is_null))
        }

        return Resource.Success(productData)
    }

    override suspend fun searchForProducts(searchQuery: String): Resource<List<ProductHomeModel>> {
        var productData = listOf<ProductHomeModel>()
        try {
            productData = productDao.searchForProducts(searchQuery)
        } catch (e: Exception) {
            Log.d("fetchFromLocal", e.toString())
        }
        if (productData.isEmpty()) {
            return Resource.Failed(context.getString(R.string.no_results_for_your_search))
        }
        return Resource.Success(productData)
    }

    override suspend fun fetchProductByCategory(category: String): Resource<List<ProductHomeModel>> {
        var productData: MutableList<ProductHomeModel> = mutableListOf()
        try {
            productData = productDao.getProductByCategory(category).toMutableList()
            Log.d("productBYCatRepo", productData.toString())
        } catch (e: Exception) {
            Log.e("fetchProductByCategory", e.toString())
        }
        if (productData.isEmpty()) {
            return Resource.Failed("Database Error,Try Again")
        }
        return Resource.Success(productData)
    }

    override suspend fun getProducts(reviewList: List<ProductOrderReviews>): List<ProductHomeModel> {
        val productIdList = getId(reviewList)
        val productList = mutableListOf<ProductHomeModel>()
          for(id in productIdList)
            {
                try{
                    productList.add(productDao.getOfferProduct(id))
                }
                catch (e:Exception)
                {
                    Log.d("Exception",e.toString())
                }

            }
        return productList



    }

    override suspend fun getProductId(name: String): Int? {
        var productId : Int? = null
        try {
            productId = productDao.getProductId(name)
        } catch (e: Exception) {
            Log.d("fetchFromLocal", e.toString())
        }
        if (productId==null) {
            return null
        }
        return productId
    }

    override suspend fun getProductByPage(): Flow<PagingData<ProductHomeModel>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ProductHomePagingSource(productDao)
            }
        ).flow
    }

    private fun getId(reviewList: List<ProductOrderReviews>): List<Int> {
        val idList = mutableListOf<Int>()

        for(review in reviewList)
        {
            idList.add(review.productId!!)
        }

        return idList

    }

}