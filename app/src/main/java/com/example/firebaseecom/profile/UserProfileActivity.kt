package com.example.firebaseecom.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.firebaseecom.CartOrder.ProductListActivity
import com.example.firebaseecom.R
import com.example.firebaseecom.auth.AuthViewModel
import com.example.firebaseecom.auth.SignUpActivity
import com.example.firebaseecom.databinding.ActivityUserProfileBinding
import com.example.firebaseecom.main.BaseActivity
import com.example.firebaseecom.utils.FirebaseEcomApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.Serializable
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint

class UserProfileActivity : BaseActivity() {
    private lateinit var activityUserProfileBinding: ActivityUserProfileBinding
    private lateinit var authViewModel: AuthViewModel
    private lateinit var profileViewModel: ProfileViewModel


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        activityUserProfileBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_user_profile)
        getUserdata()

        activityUserProfileBinding.apply {
            darkModeBtn.setOnClickListener { changeMode() }
            navPop.setOnClickListener { finish() }
            userLogout.setOnClickListener {
                userSignout()
            }
            orderLayout.setOnClickListener {
                navToCartOrders(getString(R.string.order))
            }
            cartLayout.setOnClickListener {
                navToCartOrders(getString(R.string.cart))
            }
            editProfile.setOnClickListener {
                navToEditProfile()
            }
            malayalamLanguageLayout.setOnClickListener {
                Toast.makeText(
                    this@UserProfileActivity,
                    getString(R.string.malayalam),
                    Toast.LENGTH_SHORT
                ).show()
                changeLocale("ml")

            }
            englishLanguageLayout.setOnClickListener {
                Toast.makeText(
                    this@UserProfileActivity,
                    getString(R.string.english),
                    Toast.LENGTH_SHORT
                ).show()
                changeLocale("en")
            }

        }

    }

    private fun changeMode() {
        FirebaseEcomApp().changeMode()
    }


    override fun onResume() {
        super.onResume()
        getUserdata()
    }

    private fun navToEditProfile() {
        if(activityUserProfileBinding.userDetails != null) {
            val intent = Intent(this@UserProfileActivity, EditProfileActivity::class.java)
            intent.putExtra("user", activityUserProfileBinding.userDetails as Serializable)
            startActivity(intent)
        }
    }

    private fun navToCartOrders(dest: String) {
        val intent = Intent(this@UserProfileActivity, ProductListActivity::class.java)
        intent.putExtra("dest", dest)
        startActivity(intent)
    }

    private fun userSignout() {
        val intent = Intent(this@UserProfileActivity, SignUpActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        authViewModel.logout()
        finish()
    }

    private fun getUserdata() {
        profileViewModel.userData()
        profileViewModel.userDetails.observe(this) {
            activityUserProfileBinding.apply {
                userDetails = it
                if (userDetails?.userName!!.isNotEmpty()) {
                    greetingText.text = getString(R.string.hey, userDetails?.userName)
                }
                Glide.with(this@UserProfileActivity)
                    .load(userDetails?.userImg)
                    .error(R.drawable.placeholder_image)
                    .into(userImage)
            }
        }

    }

    private fun changeLocale(langId: String) {
        val newLocale = Locale(langId)
        localeDelegate.setLocale(this, newLocale)

    }


}