package com.example.firebaseecom.repositories

import android.util.Log
import com.example.firebaseecom.model.ProductHomeModel
import com.example.firebaseecom.model.ProductOrderModel
import com.example.firebaseecom.model.ProductOrderReviews
import com.example.firebaseecom.model.UserModel
import com.example.firebaseecom.model.message.ExecutiveModel
import com.example.firebaseecom.model.message.MappedExecModel
import com.example.firebaseecom.model.message.MessageModel
import com.example.firebaseecom.utils.Resource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlin.random.Random

interface FirestoreRepository {
    val currentUser: FirebaseUser?
    suspend fun addToUsers(userModel: UserModel): Int
    suspend fun getFromUsers(): UserModel?
    suspend fun addToDest(dest: String, productModel: ProductHomeModel)
    suspend fun checkNumDest(dest: String): Int
    suspend fun removeFromDest(dest: String, productModel: ProductHomeModel)
    suspend fun getAd(): List<String>
    suspend fun addToOrders(productList: List<ProductOrderModel>)
    suspend fun getFromDest(dest: String): Resource<List<ProductHomeModel>>
    suspend fun getFromOrders(): Resource<List<ProductOrderModel>>
    suspend fun removeFromCartIfOrder(productList: List<ProductHomeModel>)
    suspend fun addToReviews(productOrderReviews: ProductOrderReviews)
    suspend fun getProductReview(productId: Int): Resource<List<ProductOrderReviews>>
    suspend fun getReviewUsers(reviewData: List<ProductOrderReviews>): List<UserModel>
    suspend fun getUserReviews(): List<ProductOrderReviews>
    suspend fun getOrderForChat(orderId: String): ProductOrderModel?
    suspend fun sendMessages(message: MessageModel): Resource<Boolean>
    suspend fun syncDataOnChange(assignedExecutiveModel: ExecutiveModel)
    suspend fun getRandomExec(): Resource<ExecutiveModel>

    suspend fun updateMapping(mapModel: MappedExecModel)


}


class FirestoreRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth, private val firestore: FirebaseFirestore
) : FirestoreRepository {
    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    companion object {
        val messageFlow = MutableStateFlow<List<MessageModel>>(listOf())
    }


    override suspend fun addToUsers(userModel: UserModel): Int {
        var status = 400
        userModel.userId = currentUser!!.uid
        Log.d("currentUser", currentUser?.email.toString())
        try {
            val doc = firestore.collection("users").document(currentUser!!.uid).set(userModel)
            doc.addOnSuccessListener {
                status = 200
                Log.d("success", "$status")
            }.addOnFailureListener {
                Log.e("toUse", "${it.message}")

            }
        } catch (e: Exception) {
            Log.e("toUser", "$e")

        }
        return status

    }

    override suspend fun getFromUsers(): UserModel {
        val uid = currentUser?.uid
        Log.d("currentUser", currentUser?.email.toString())
        val db = firestore.collection("users").document(uid.toString())
        var userInfo = UserModel()

        try {
            val snapshot = Tasks.await(db.get().addOnCompleteListener {
                if (it.isSuccessful) it.result
            })
            val data = snapshot.data
            userInfo = snapshot.toObject(UserModel::class.java)!!
            data.let {

            }
            Log.d("userinforepoIN", snapshot.data.toString())
            /*userInfo = UserModel(
                data?.get("userName").toString(),
                data?.get("userEmail").toString(),
                data?.get("userImg").toString(),
                data?.get("phNo").toString(),
                data?.get("address").toString()
            )*/

        } catch (e: Exception) {
            Log.d("exceptio", e.toString())
        }
        Log.d("userinforepo", userInfo.toString())

        return userInfo

    }


    override suspend fun addToDest(dest: String, productModel: ProductHomeModel) {
        try {
            val db =
                firestore.collection("user-$dest").document(currentUser!!.uid).collection("items")
                    .document(productModel.productId.toString())
            db.set(productModel).addOnSuccessListener {
                Log.d("add", "success")
            }.addOnFailureListener {
                Log.d("add", "failure")
            }
        } catch (e: Exception) {
            Log.d("exception", "$e")
        }
    }

    override suspend fun getAd(): List<String> {
        val adList: MutableList<String> = mutableListOf()
        val db = firestore.collection("ads")
        try {
            val snapshot = Tasks.await(db.get().addOnCompleteListener {
                if (it.isSuccessful) it.result
            })
            snapshot.let {
                for (doc in it.documents) {

                    val data = doc.data!!["imageUrl"].toString()
                    adList.add(data)
                }
            }
        } catch (e: Exception) {
            Log.d("exception", e.toString())
        }
        Log.d("adList", adList.toString())
        return adList
    }

    override suspend fun addToOrders(productList: List<ProductOrderModel>) {
        for (productModel in productList) {
            try {
                val db = firestore.collection("user-orders").document(currentUser!!.uid)
                    .collection("items").document(productModel.productId.toString())
                db.set(productModel).addOnSuccessListener {
                    Log.d("add", "success")
                }.addOnFailureListener {
                    Log.d("add", "failure")
                }
            } catch (e: Exception) {
                Log.d("exception", "$e")
            }
        }
    }

    override suspend fun removeFromDest(dest: String, productModel: ProductHomeModel) {
        val db = firestore.collection("user-$dest").document(currentUser!!.uid).collection("items")
            .document(productModel.productId.toString())
        db.delete()
    }

    override suspend fun checkNumDest(dest: String): Int {
        var count = 0
        val db = firestore.collection("user-$dest").document(currentUser!!.uid).collection("items")

        try {
            val snapshot = Tasks.await(db.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result
                }
            })
            snapshot.let {
                for (doc in it.documents) {
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e("ErrorNumbCheck", e.toString())
        }
        Log.d("cartNUmrepo", count.toString())
        return count

    }

    override suspend fun getFromDest(dest: String): Resource<List<ProductHomeModel>> {
        val productList: MutableList<ProductHomeModel> = mutableListOf()
        var response = 0
        val db = firestore.collection("user-$dest").document(currentUser!!.uid).collection("items")
        try {
            val snapshot = Tasks.await(db.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result
                }
            })
            snapshot.let {
                for (doc in it.documents) {
                    val data = doc.toObject(ProductHomeModel::class.java)
                    if (data != null) {
                        response = 200
                        productList.add(data)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("exception", e.toString())
        }
        if (response != 200) {
            return Resource.Failed("No Available Data")
        }
        Log.d("cartdatarepo", productList.toString())
        return Resource.Success(productList)
    }

    override suspend fun getFromOrders(): Resource<List<ProductOrderModel>> {
        val productList: MutableList<ProductOrderModel> = mutableListOf()
        var response = 0
        val db = firestore.collection("user-orders").document(currentUser!!.uid).collection("items")
        try {
            val snapshot = Tasks.await(db.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result
                }
            })
            snapshot.let {
                for (doc in it.documents) {
                    val data = doc.toObject(ProductOrderModel::class.java)
                    if (data != null) {
                        response = 200
                        productList.add(data)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("exception", e.toString())
        }
        if (response != 200) {
            return Resource.Failed("No Available Data")
        }
        Log.d("cartdatarepo", productList.toString())
        return Resource.Success(productList)
    }

    override suspend fun removeFromCartIfOrder(productList: List<ProductHomeModel>) {
        val db = firestore.collection("user-cart").document(currentUser!!.uid).collection("items")
        for (productModel in productList) {
            db.document(productModel.productId.toString()).get().addOnSuccessListener {
                if (it.exists()) db.document(productModel.productId.toString()).delete()
            }
        }

    }

    override suspend fun addToReviews(productOrderReviews: ProductOrderReviews) {
        productOrderReviews.userId = currentUser!!.uid
        val db =
            firestore.collection("user-reviews").document(currentUser!!.uid).collection("items")
                .document(productOrderReviews.productId.toString())
        val dbProduct = firestore.collection("product-reviews")
            .document(productOrderReviews.productId.toString()).collection("reviews")
            .document(currentUser!!.uid)
        try {
            db.set(productOrderReviews).addOnSuccessListener {
                Log.d("review", "success")
            }.addOnFailureListener {
                Log.d("review", "failed")
            }
            dbProduct.set(productOrderReviews).addOnSuccessListener {
                Log.d("review", "success")
            }.addOnFailureListener {
                Log.d("review", "failed")
            }
        } catch (e: Exception) {
            Log.d("review", e.toString())
        }

    }

    override suspend fun getProductReview(productId: Int): Resource<List<ProductOrderReviews>> {
        val reviewList = mutableListOf<ProductOrderReviews>()
        var response = 0
        val db = firestore.collection("product-reviews").document(productId.toString())
            .collection("reviews")

        try {
            val snapshot = Tasks.await(db.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result
                }
            })
            snapshot.let {
                for (doc in it.documents) {
                    val data = doc.toObject(ProductOrderReviews::class.java)
                    if (data != null) {
                        response = 200
                        reviewList.add(data)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("exception", e.toString())
        }
        if (response != 200) {
            return Resource.Failed("No Available Data")
        }
        Log.d("reviewdatarepo", reviewList.toString())
        return Resource.Success(reviewList)
    }

    override suspend fun getReviewUsers(reviewData: List<ProductOrderReviews>): List<UserModel> {
        val reviewUser = mutableListOf<UserModel>()
        val userIdList = getUserId(reviewData)
        Log.d("reviewUserID", userIdList.toString())

        for (userId in userIdList) {
            val db = firestore.collection("users").document(userId)
            var userInfo = UserModel()
            try {
                val snapshot = Tasks.await(db.get().addOnCompleteListener {
                    if (it.isSuccessful) it.result
                })
                val data = snapshot.data
                userInfo = snapshot.toObject(UserModel::class.java)!!
                Log.d("userinforepoIN", snapshot.data.toString())
                /* userInfo = UserModel(
                     data?.get("userName").toString(),
                     data?.get("userEmail").toString(),
                     data?.get("userImg").toString(),
                     data?.get("phNo").toString(),
                     data?.get("address").toString()
                 )*/

            } catch (e: Exception) {
                Log.d("exception", e.toString())
            }
            reviewUser.add(userInfo)
            Log.d("reviewUserListRepoIn", userIdList.toString())

        }
        Log.d("reviewUserListRepo", userIdList.toString())
        return reviewUser

    }

    override suspend fun getUserReviews(): List<ProductOrderReviews> {
        var response = 0
        val reviewList = mutableListOf<ProductOrderReviews>()
        val db =
            firestore.collection("user-reviews").document(currentUser!!.uid).collection("items")

        try {
            val snapshot = Tasks.await(db.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result
                }
            })
            snapshot.let {
                for (doc in it.documents) {
                    val data = doc.toObject(ProductOrderReviews::class.java)
                    if (data != null) {
                        response = 200
                        reviewList.add(data)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("exception", e.toString())
        }
        if (response != 200) {
            return listOf()
        }
        Log.d("reviewdatarepo", reviewList.toString())
        return reviewList

    }

    override suspend fun getOrderForChat(orderId: String): ProductOrderModel? {
        when (val productOrderList = getFromOrders()) {
            is Resource.Success -> {
                for (productOrder in productOrderList.data) {
                    if (productOrder.orderId == orderId) {
                        Log.d("OrderforChat", productOrder.toString())
                        return productOrder
                    }
                }
            }

            else -> {
                return null
            }
        }
        return null
    }

    override suspend fun sendMessages(message: MessageModel): Resource<Boolean> {
        var status: Resource<Boolean> = Resource.Success(true)
        message.sendUserId = firebaseAuth.currentUser!!.uid
        Log.d("currentUser", message.toString() + message.msgId)
        val doc = firestore.collection("messages-executive").document(message.receiveUserId)
            .collection(currentUser!!.uid).document(message.msgId)
        try {
            val send = Tasks.await(doc.set(message).addOnCompleteListener {
                if (it.isSuccessful) Log.d("success", "$status")
            }.addOnFailureListener {
                Log.e("toMes", "${it.message}")
                status = Resource.Failed("DB ERROR")
            })
            send.hashCode()
        } catch (e: Exception) {
            Log.e("toUser", "$e")

        }
        return status


    }

    override suspend fun syncDataOnChange(assignedExecutiveModel: ExecutiveModel) {
        val doc = firestore.collection("messages-executive").document(assignedExecutiveModel.execId)
            .collection(currentUser!!.uid)

        doc.addSnapshotListener { value, error ->
            error.let {
                Log.d("ReceiveListERR", it.toString())
            }
            value.let {
                Log.d("ReceiveList!", it!!.documents.size.toString())
                val data = value?.toObjects(MessageModel::class.java)
                Log.d("ReceiveList!!!", data.toString())
                messageFlow.value = data!!


            }
        }


    }

    override suspend fun getRandomExec(): Resource<ExecutiveModel> {
        var status = false
        val executiveList = mutableListOf<ExecutiveModel>()
        val doc = firestore.collection("executive")
        try {
            val snapshot = Tasks.await(doc.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result
                }
            })
            snapshot.let {
                for (documentSnapshot in it.documents) {
                    val data = documentSnapshot.toObject(ExecutiveModel::class.java)
                    if (data != null) {
                        status = true
                        executiveList.add(data)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("error", e.toString())
        }

        if (status) return Resource.Success(executiveList[Random.nextInt(executiveList.size)])

        return Resource.Failed("DB ERROR")
    }

    override suspend fun updateMapping(mapModel: MappedExecModel) {
        mapModel.user = getFromUsers()
        val doc = firestore.collection("mapping-exec-user").document(mapModel.executive!!.execId)
        try {
            val send = Tasks.await(doc.set(mapModel).addOnCompleteListener {
                if (it.isSuccessful)
                    Log.d("MapExec", "success")
            }.addOnFailureListener {
                Log.e("toMes", "${it.message}")
            })
            send.hashCode()
        } catch (e: Exception) {
            Log.e("toUser", "$e")

        }
    }


    private fun getUserId(reviewData: List<ProductOrderReviews>): List<String> {
        val userIdList = mutableListOf<String>()
        for (review in reviewData) {
            userIdList.add(review.userId.toString())
        }
        return userIdList

    }
}
