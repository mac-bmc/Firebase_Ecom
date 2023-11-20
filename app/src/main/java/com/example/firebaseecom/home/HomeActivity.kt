package com.example.firebaseecom.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebaseecom.CartOrder.ProductListActivity
import com.example.firebaseecom.R
import com.example.firebaseecom.category.ProductCategoryActivity
import com.example.firebaseecom.databinding.ActivityHomeBinding
import com.example.firebaseecom.detailsPg.ProductDetailsActivity
import com.example.firebaseecom.model.ProductHomeModel
import com.example.firebaseecom.productSearch.ProductSearchActivity
import com.example.firebaseecom.profile.UserProfileActivity
import com.example.firebaseecom.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

@AndroidEntryPoint


class HomeActivity : AppCompatActivity() {

    private lateinit var homeBinding: ActivityHomeBinding
    private lateinit var homeViewModel: HomeViewModel
    private val carousalAdapter = CarousalAdapter(this@HomeActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        homeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        val adView = homeBinding.carousalView

        adView.adapter = carousalAdapter
        adView.layoutManager = LinearLayoutManager(
            this@HomeActivity, LinearLayoutManager.HORIZONTAL,
            false
        )

        observeCartNumber()
        observeCarousal()
        observeProducts()



        homeBinding.apply {
            cartNumber.text = "0"
            profButton.setOnClickListener {
                startActivity(Intent(this@HomeActivity, UserProfileActivity::class.java))
            }
            searchHomeButton.setOnClickListener {
                startActivity(Intent(this@HomeActivity, ProductSearchActivity::class.java))
            }
            cartHomeButton.setOnClickListener {
                val intent = Intent(this@HomeActivity, ProductListActivity::class.java)
                intent.putExtra("dest", "cart")
                startActivity(intent)
            }
            catLaptop.setOnClickListener {
                val intent = Intent(this@HomeActivity, ProductCategoryActivity::class.java)
                intent.putExtra("category", "Laptop")
                startActivity(intent)
            }
            catPhones.setOnClickListener {
                val intent = Intent(this@HomeActivity, ProductCategoryActivity::class.java)
                intent.putExtra("category", "Phone")
                startActivity(intent)
            }
            catTablet.setOnClickListener {
                val intent = Intent(this@HomeActivity, ProductCategoryActivity::class.java)
                intent.putExtra("category", "Tablet")
                startActivity(intent)
            }
        }


    }

    private fun observeCarousal() {
        homeViewModel.adList.observe(this@HomeActivity, Observer {
            carousalAdapter.setAd(it)
        })
        homeViewModel.getAd()
    }


    override fun onRestart() {
        super.onRestart()


        observeCartNumber()
    }

    private fun observeCartNumber() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val size = homeViewModel.checkNumbWishlist("cart")
                homeBinding.cartNumber.text = size.toString()
            }
        }
    }



    private fun observeProducts() {
        val homeItemView = homeBinding.homeItemView
        val adapter = ProductHomeAdapter(NavigateClass())
        homeItemView.layoutManager = GridLayoutManager(this@HomeActivity, 2)
        homeItemView.adapter = adapter
        homeBinding.apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                homeViewModel.getProductHome()
                homeViewModel.products.collect()
                {
                    when (it) {
                        is Resource.Loading -> {
                            homeItemViewProgress.visibility = View.VISIBLE
                            Log.d("itemViewLoader", "Loading")
                        }

                        is Resource.Success -> {
                            homeItemViewProgress.visibility = View.INVISIBLE
                            Log.d("itemViewLoader", "success")
                            adapter.setProduct(it.data)

                        }

                        is Resource.Failed -> {
                            homeItemViewProgress.visibility = View.VISIBLE
                            Log.d("itemViewLoader", "Failed")
                            Toast.makeText(this@HomeActivity, it.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
                }
            }}
        }

    }

    inner class NavigateClass : ProductHomeAdapter.NavigationInterface {
        override fun navigateToDetails(productModel: ProductHomeModel) {
            val intent = Intent(this@HomeActivity, ProductDetailsActivity::class.java)
            intent.putExtra("product", productModel as Serializable)
            startActivity(intent)
        }

    }
}
