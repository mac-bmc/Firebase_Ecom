package com.example.firebaseecom.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.firebaseecom.R
import com.example.firebaseecom.databinding.ActivityLoginBinding
import com.example.firebaseecom.home.HomeActivity
import com.example.firebaseecom.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var activityLoginBinding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoginBinding = DataBindingUtil.setContentView(
            this@LoginActivity, R.layout.activity_login
        )
        authViewModel = ViewModelProvider(this@LoginActivity)[AuthViewModel::class.java]

        activityLoginBinding.apply {
            logInButton.setOnClickListener {
                lifecycleScope.launch {
                    authViewModel.apply {
                        logIn(editTextUsername.text.toString(), editTextPassword.text.toString())
                        loginAuth.collect {
                            when (it) {
                                is Resource.Loading -> {
                                    Log.d("Loading", "Loading")
                                    progressBar.visibility = View.VISIBLE
                                }

                                is Resource.Success -> {
                                    Log.d("success", "${it.data}")
                                    startActivity(
                                        Intent(
                                            this@LoginActivity, HomeActivity::class.java
                                        )
                                    )

                                }

                                is Resource.Failed -> {
                                    Log.d("failed", it.message)
                                    progressBar.visibility = View.INVISIBLE
                                    Toast.makeText(
                                        this@LoginActivity,it.message, Toast.LENGTH_LONG
                                    ).show()
                                }

                                else -> {}
                            }
                        }
                    }


                }
            }
        }
    }
}