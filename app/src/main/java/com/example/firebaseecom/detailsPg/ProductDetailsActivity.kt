@file:Suppress("DEPRECATION")

package com.example.firebaseecom.detailsPg

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebaseecom.R
import com.example.firebaseecom.databinding.ActivityProductDetailsBinding
import com.example.firebaseecom.model.ProductHomeModel
import com.example.firebaseecom.payments.ProductCheckoutActivity
import com.example.firebaseecom.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint

class ProductDetailsActivity : AppCompatActivity() {
    private lateinit var activityProductDetailsBinding: ActivityProductDetailsBinding
    private lateinit var productDetailsViewModel: ProductDetailsViewModel
    private lateinit var productHome: ProductHomeModel
    private val carousalAdapter = ProductDetailsAdapter()
    var productList = arrayListOf<ProductHomeModel?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productDetailsViewModel = ViewModelProvider(this)[ProductDetailsViewModel::class.java]
        activityProductDetailsBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_product_details)
        productHome = intent.extras!!.get("product") as ProductHomeModel
        val productView = activityProductDetailsBinding.productCarousalView

        productView.adapter = carousalAdapter
        productView.layoutManager = LinearLayoutManager(
            this@ProductDetailsActivity, LinearLayoutManager.HORIZONTAL,
            false
        )

        observeProductDetails()

        activityProductDetailsBinding.apply {
            productTitleText.text = productHome.productTitle
            productTitleHeader.text = productHome.productTitle
            productPriceText.text = productHome.productPrice.toString()
            shareButton.setOnClickListener {
                productDetailsViewModel.shareProduct(productHome, this@ProductDetailsActivity)
            }
            backButton.setOnClickListener {
                finish()
            }
            buttonBuyNow.setOnClickListener {
                productList.add(productHome)
                Log.d("productList", productList.toString())
                val intent =
                    Intent(this@ProductDetailsActivity, ProductCheckoutActivity::class.java)
                if (productList.isNotEmpty()) {
                    intent.putExtra("productList", productList)
                }
                startActivity(intent)
            }
            buttonAddToCart.setOnClickListener {
                productDetailsViewModel.addToCart(productHome)
                Toast.makeText(this@ProductDetailsActivity, "Added To Cart", Toast.LENGTH_SHORT)
                    .show()
            }

        }

    }

    private fun observeProductDetails() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                productDetailsViewModel.getProductDetails(productHome.productId!!)
                productDetailsViewModel.productDetails.collect {
                    when (it) {
                        is Resource.Loading -> {
                            activityProductDetailsBinding.progressBar.isVisible = true
                            Toast.makeText(
                                this@ProductDetailsActivity, "Details Loading", Toast.LENGTH_SHORT
                            ).show()
                        }

                        is Resource.Success -> {
                            activityProductDetailsBinding.progressBar.isVisible = false
                            val myList = it.data
                            activityProductDetailsBinding.productDetails =
                                myList?.singleOrNull { list ->
                                    list.productId == productHome.productId
                                }
                            carousalAdapter.setAd(activityProductDetailsBinding.productDetails?.productImage!!)

                        }

                        is Resource.Failed -> {
                            activityProductDetailsBinding.progressBar.isVisible = true
                            Toast.makeText(
                                this@ProductDetailsActivity,
                                it.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }


                        else -> {}
                    }
                }
            }
        }
        }
    }
}