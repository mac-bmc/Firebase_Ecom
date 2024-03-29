@file:Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")

package com.example.firebaseecom.home

import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseecom.CartOrder.ProductListActivity
import com.example.firebaseecom.R
import com.example.firebaseecom.category.ProductCategoryActivity
import com.example.firebaseecom.databinding.ActivityHomeBinding
import com.example.firebaseecom.detailsPg.ProductDetailsActivity
import com.example.firebaseecom.home.HomeViewModel.Companion.getChange
import com.example.firebaseecom.main.BaseActivity
import com.example.firebaseecom.model.ProductHomeModel
import com.example.firebaseecom.model.ProductOffersModel
import com.example.firebaseecom.offers.OfferZoneActivity
import com.example.firebaseecom.productSearch.ProductSearchActivity
import com.example.firebaseecom.profile.UserProfileActivity
import com.example.firebaseecom.utils.AlertDialogUtils
import com.example.firebaseecom.utils.NetworkState
import com.example.firebaseecom.utils.NotificationUtils
import com.example.firebaseecom.utils.Resource
import com.example.firebaseecom.utils.ToastUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.Serializable


@AndroidEntryPoint


class HomeActivity : BaseActivity() {

    private lateinit var homeBinding: ActivityHomeBinding
    private lateinit var homeViewModel: HomeViewModel
    private val carousalAdapter = CarousalAdapter(this@HomeActivity, ActivityFunctionClass())
    private val snapHelper = LinearSnapHelper()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var productJob: Job? = null
    private lateinit var adLayoutManager: LinearLayoutManager
    private lateinit var adView: RecyclerView
    private val handler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null
    private var catLaptopString = "Laptop"
    private var catPhoneString = "Phone"
    private var catTabletString = "Tablet"

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        sharedPreferences = getSharedPreferences("IN_APP_MESSAGING", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        homeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        changeUserDialog()
        checkForNewUser()
        observeNetwork()
        observeNewProducts()
        productByPage()
        adView = homeBinding.carousalView
        adView.adapter = carousalAdapter
        snapHelper.attachToRecyclerView(adView)
        adLayoutManager = LinearLayoutManager(
            this@HomeActivity, LinearLayoutManager.HORIZONTAL,
            false
        )
        adView.layoutManager = adLayoutManager



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
                intent.putExtra("dest", getString(R.string.cart))
                startActivity(intent)
            }
            catLaptop.setOnClickListener {
                navToCategory(catLaptopString)
            }
            catPhones.setOnClickListener {
                navToCategory(catPhoneString)

            }
            catTablet.setOnClickListener {
                navToCategory(catTabletString)
            }
        }


    }

    private fun navToCategory(category: String) {
        val intent = Intent(this@HomeActivity, ProductCategoryActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }

    private fun changeUserDialog() {
        editor.putInt("userProfileDialog", 0)
        editor.apply()
        Log.d("userDialogChanged", sharedPreferences.getInt("userProfileDialog", 0).toString())
    }

    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }

    private fun observeNetwork() {
        homeBinding.networkProgress.visibility = View.VISIBLE
        homeViewModel.getNetwork()
        homeViewModel.networkFlow.observe(this@HomeActivity) {
            Log.d("networkState", it.toString())
            when (it) {
                NetworkState.AVAILABLE -> {
                    homeBinding.apply {

                        networkStatusLayout.visibility = View.GONE
                        homeLayout.isVisible = true
                        networkProgress.visibility = View.GONE

                    }
                    observeCarousal()
                    observeCartNumber()
                    observeProducts()
                }

                NetworkState.UNAVAILABLE -> {
                    homeBinding.apply {

                        networkText.text = getString(R.string.no_internet_connection)
                        networkStatusLayout.isVisible = true
                        homeLayout.isVisible = false
                        networkProgress.isVisible = true


                    }
                    ToastUtils().giveToast(
                        getString(R.string.connection_unavailable),
                        this@HomeActivity
                    )
                }

                NetworkState.LOSING -> {
                    homeBinding.apply {

                        networkText.text = getString(R.string.no_internet_connection)
                        networkStatusLayout.isVisible = true

                    }

                    ToastUtils().giveToast(
                        getString(R.string.connection_losing),
                        this@HomeActivity
                    )
                }

                NetworkState.LOST -> {
                    homeBinding.apply {

                        networkText.text = getString(R.string.no_internet_connection)
                        networkStatusLayout.isVisible = true

                    }
                    ToastUtils().giveToast(
                        getString(R.string.connection_lost),
                        this@HomeActivity
                    )
                }
            }
        }
    }

    private fun observeCarousal() {
        homeBinding.homeAdViewProgress.isVisible = true
        homeViewModel.getAd()
        homeViewModel.adList.observe(this@HomeActivity) {
            carousalAdapter.setAd(it)
            homeBinding.homeAdViewProgress.isVisible = false
            //homeBinding.carousalView.scrollToPosition(Integer.MAX_VALUE / 2)


        }
    }


    private fun startAutoScroll() {
        autoScrollRunnable = object :
            Runnable {
            override fun run() {
                if (adLayoutManager.findLastCompletelyVisibleItemPosition() < carousalAdapter.itemCount - 1) {
                    adLayoutManager.smoothScrollToPosition(
                        adView,
                        RecyclerView.State(),
                        adLayoutManager.findLastCompletelyVisibleItemPosition() + 1
                    )
                } else if (adLayoutManager.findLastCompletelyVisibleItemPosition() == carousalAdapter.itemCount - 1) {
                    adLayoutManager.smoothScrollToPosition(
                        adView,
                        RecyclerView.State(),
                        0
                    )
                }
                handler.postDelayed(this, 4000L)
            }

        }
        handler.postDelayed(autoScrollRunnable!!, 4000L)
    }

    private fun stopAutoScroll() {
        autoScrollRunnable?.let {
            handler.removeCallbacks(it)
        }
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
        homeBinding.homeItemViewProgress.visibility = View.VISIBLE
        productJob?.cancel()
        val homeItemView = homeBinding.homeItemView
        val adapter = ProductHomeAdapter(this, NavigateClass(), langId)

        homeItemView.layoutManager = GridLayoutManager(this@HomeActivity, 2)
        homeItemView.adapter = adapter
        homeBinding.apply {
            productJob = lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    homeViewModel.getOffers()
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
                                homeViewModel.offerData.observe(this@HomeActivity) { offerData ->
                                    Log.d("offerData", offerData.toString())
                                    adapter.setProduct(it.data, offerData)
                                }


                            }

                            is Resource.Failed -> {
                                homeItemViewProgress.visibility = View.VISIBLE
                                Log.d("itemViewLoader", "Failed")
                            }
                            else -> {}


                        }

                    }
                }
            }
        }

    }

    inner class NavigateClass : ProductHomeAdapter.NavigationInterface {
        override fun navigateToDetails(
            productModel: ProductHomeModel,
            offersModelList: List<ProductOffersModel>
        ) {
            val bundle = Bundle()
            bundle.putSerializable("offers", offersModelList as Serializable)
            val intent = Intent(this@HomeActivity, ProductDetailsActivity::class.java)
            intent.putExtra("product", productModel as Serializable)
            intent.putExtras(bundle)
            startActivity(intent)
        }

    }

    inner class ActivityFunctionClass : CarousalAdapter.ActivityFunctionInterface {
        override fun navToOfferZone() {
            startActivity(Intent(this@HomeActivity, OfferZoneActivity::class.java))
        }

    }

    override fun onRestart() {
        super.onRestart()
        observeNetwork()
    }

    override fun onResume() {
        super.onResume()
        startAutoScroll()
    }


    private fun checkForNewUser() {
        if (sharedPreferences.getInt("newUserTrigger", 0) == 0) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    val isNewUser = homeViewModel.checkForNewUser()
                    if (isNewUser) {
                        editor.putInt("newUserTrigger", 1)
                        editor.apply()
                        showNewUserDialog()
                    }
                }
            }
        }
    }


    private fun showNewUserDialog() {
        AlertDialogUtils().showAlertDialogNotification(this, getString(R.string.enjoy_shopping))
    }

    private fun observeNewProducts() {
        getChange.observe(this@HomeActivity) {
            if (it) {
                showNewProductNotification()
            }

        }
        homeViewModel.changeNewProductStatus()


    }

    private fun showNewProductNotification() {
        val intent = Intent(this, OfferZoneActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        AlertDialogUtils().showAlertDialogNotification(
            this@HomeActivity,
            getString(R.string.new_products)
        )
        NotificationUtils(this).showNotification(pendingIntent, getString(R.string.new_products))
    }

    fun productByPage()
    {
        homeViewModel.getProductByPage()
    }


}


