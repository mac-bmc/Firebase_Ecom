@file:Suppress("DEPRECATION")

package com.example.firebaseecom.payments

import android.content.Intent

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebaseecom.R
import com.example.firebaseecom.api.RazorPayJson
import com.example.firebaseecom.api.RazorpayKey
import com.example.firebaseecom.auth.SignUpActivity
import com.example.firebaseecom.databinding.ActivityProductCheckoutBinding
import com.example.firebaseecom.home.HomeActivity
import com.example.firebaseecom.model.ProductHomeModel
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

class ProductCheckoutActivity : AppCompatActivity(), PaymentResultListener {
    private lateinit var activityProductCheckoutBinding: ActivityProductCheckoutBinding
    private lateinit var productCheckoutViewModel: ProductCheckoutViewModel
    private var productList = arrayListOf<ProductHomeModel>()
    val adapter = ProductCheckoutAdapter(ActivityFunctionClass())
    var totalAmount = 0.0
    private val checkOut = Checkout()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityProductCheckoutBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_product_checkout)
        productCheckoutViewModel = ViewModelProvider(this)[ProductCheckoutViewModel::class.java]
        checkOut.setKeyID(RazorpayKey.EKART_RAZORPAY_KEY.key)
        checkOut.setImage(R.drawable.ic_cart)
        productList = (intent.extras?.get("productList") as? ArrayList<ProductHomeModel>)!!
        activityProductCheckoutBinding.apply {
            productListView.layoutManager = LinearLayoutManager(
                this@ProductCheckoutActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            productListView.adapter = adapter
            backButton.setOnClickListener {
                finish()
            }
            PaymentBtn.setOnClickListener {
                launchPay()
            }

        }
        adapter.setProducts(productList.toList())
    }

    private fun launchPay() {
        val razorpayJSON = RazorPayJson(totalAmount).razorpayJSON
        try {

            checkOut.open(this, razorpayJSON)

        } catch (e: Exception) {
            Log.d("launchRazorPay", e.toString())
        }
    }

    inner class ActivityFunctionClass : ProductCheckoutAdapter.ActivityFunctionInterface {
        override fun addTotalPrice(productList: List<ProductHomeModel?>) {
            for (product in productList) {
                totalAmount += product?.productPrice!!
            }
            activityProductCheckoutBinding.totalPrice.text =
                getString(R.string.total_amount_to_be_paid, totalAmount)

        }
    }



    @RequiresApi(Build.VERSION_CODES.O)

    override fun onPaymentSuccess(p0: String?) {
        Toast.makeText(this, "Order Placed", Toast.LENGTH_SHORT).show()
        productCheckoutViewModel.addToOrders(productList)
        Log.d("productListSize", productList.size.toString())
        productCheckoutViewModel.removeAllFromCart(productList)
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        Toast.makeText(this, p1, Toast.LENGTH_SHORT).show()
    }
}
