package com.example.firebaseecom.CartOrder

import ProductCartAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebaseecom.R
import com.example.firebaseecom.databinding.ActivityProductListBinding
import com.example.firebaseecom.detailsPg.ProductDetailsActivity
import com.example.firebaseecom.model.ProductHomeModel
import com.example.firebaseecom.model.ProductOrderModel

import com.example.firebaseecom.payments.ProductCheckoutActivity
import com.example.firebaseecom.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.Serializable

@AndroidEntryPoint
class ProductListActivity : AppCompatActivity() {
    private lateinit var activityProductListBinding: ActivityProductListBinding
    private lateinit var productListViewModel: ProductListViewModel
    val cartAdapter = ProductCartAdapter(ActivityFunctionClass())

    val orderAdapter = ProductOrderAdapter(navClass())

    var productList = arrayListOf<ProductHomeModel>()
    var dest = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityProductListBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_product_list)
        productListViewModel = ViewModelProvider(this)[ProductListViewModel::class.java]
        dest = intent.getStringExtra("dest")!!
        observeProducts(dest)
        activityProductListBinding.apply {
            if (dest == "orders") {

                ButtonHolder.visibility= View.GONE

                recyclerView.adapter = orderAdapter
            } else {
                recyclerView.adapter = cartAdapter
            }
            backButton.setOnClickListener {
                finish()
            }
            destText.text = dest
            recyclerView.layoutManager = LinearLayoutManager(
                this@ProductListActivity, LinearLayoutManager.VERTICAL,
                false
            )
            buttonBuyNow.setOnClickListener {
                if (productList.isNotEmpty()) {
                    val intent =
                        Intent(this@ProductListActivity, ProductCheckoutActivity::class.java)
                    intent.putExtra("productList", productList)
                    Log.d("productList", productList.toString())
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@ProductListActivity,
                        "Add items to cart first",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            }

        }

    }

    private fun observeProducts(dest: String?) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                if (dest == "orders") {
                    productListViewModel.getProductFromOrder()
                    productListViewModel.productOrderList.collect {
                        when (it) {
                            is Resource.Loading -> {
                                activityProductListBinding.progressBar.isVisible = true
                            }

                            is Resource.Success -> {
                                activityProductListBinding.progressBar.isVisible = false
                                Log.d("cartData", it.data.toString())
                                orderAdapter.setProduct(it.data)

                            }

                            is Resource.Failed -> {
                                activityProductListBinding.progressBar.isVisible = false
                                Toast.makeText(
                                    this@ProductListActivity,
                                    it.message,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }

                        }
                    }
                } else {
                    productListViewModel.getProductFromDest(dest!!)
                    productListViewModel.productCartList.collect {
                        when (it) {
                            is Resource.Loading -> {
                                activityProductListBinding.progressBar.isVisible = true
                            }

                            is Resource.Success -> {
                                activityProductListBinding.progressBar.isVisible = false
                                Log.d("cartData", it.data.toString())
                                cartAdapter.setProduct(it.data)
                                productList = ArrayList(it.data)

                            }

                            is Resource.Failed -> {
                                activityProductListBinding.progressBar.isVisible = false
                                Toast.makeText(
                                    this@ProductListActivity,
                                    it.message,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
                }

            }

        }


    }

    inner class ActivityFunctionClass : ProductCartAdapter.ActivityFunctionInterface {
        override fun navigateToDetails(productHomeModel: ProductHomeModel) {
            val intent = Intent(this@ProductListActivity, ProductDetailsActivity::class.java)
            intent.putExtra("product", productHomeModel as Serializable)
            startActivity(intent)
        }
        override fun deleteFromCart(productHomeModel: ProductHomeModel, position: Int) {


            productListViewModel.removeFromCart(productHomeModel)
            productList.remove(productHomeModel)


        }


        override fun addTotalPrice(productList: List<ProductHomeModel>) {
            var totalPrice = 0
            for (product in productList) {
                totalPrice += product.productPrice!!
            }
            activityProductListBinding.cartPrice.text =
                getString(R.string.price, totalPrice.toString())
        }
    }


    /*private fun deleteFromOrder(productHomeModel: ProductHomeModel) {

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.cancel_your_order))
        builder.setMessage(getString(R.string.cancelOrder))
        builder.setPositiveButton(R.string.submit) { _, _ ->
            productListViewModel.removeFromOrder(productHomeModel)
        }
        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        builder.show()

    }*/

    inner class navClass : ProductOrderAdapter.navInterface {
        override fun navToOrderView(productOrderModel: ProductOrderModel) {
            val intent=Intent(this@ProductListActivity,ProductOrderViewActivity::class.java)
            intent.putExtra("productDetails",productOrderModel)
            startActivity(intent)
        }


    }
}