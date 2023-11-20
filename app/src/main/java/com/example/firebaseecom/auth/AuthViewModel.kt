package com.example.firebaseecom.auth


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseecom.model.UserModel
import com.example.firebaseecom.repositories.AuthRepository
import com.example.firebaseecom.repositories.FirestoreRepository
import com.example.firebaseecom.utils.Resource
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val _loginAuth = MutableStateFlow<Resource<FirebaseUser>?>(Resource.Loading())
    private val _signUpAuth = MutableStateFlow<Resource<FirebaseUser>?>(Resource.Loading())
    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    init {
        if (authRepository.currentUser != null) {
            _loginAuth.value = Resource.Success(authRepository.currentUser!!)
        }
    }

    var loginAuth: StateFlow<Resource<FirebaseUser>?> = _loginAuth
    var signUpAuth: StateFlow<Resource<FirebaseUser>?> = _signUpAuth

    fun logIn(email: String, password: String) {
        _loginAuth.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            val result = authRepository.userLogin(email, password)
            _loginAuth.value = result


        }
    }

    fun signUp(email: String, password: String, phNum: String) {
        val userModel = UserModel("", email, "", phNum,"")

        _signUpAuth.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            _signUpAuth.value = authRepository.userSignUp(email, password)
            Log.d("userData", userModel.toString())
            firestoreRepository.addToUsers(userModel)
        }

    }

    fun logout() {
        authRepository.userSignOut()
        _loginAuth.value = null
        _signUpAuth.value = null
    }


}