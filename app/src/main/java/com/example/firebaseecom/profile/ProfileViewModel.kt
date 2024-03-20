package com.example.firebaseecom.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseecom.model.UserModel
import com.example.firebaseecom.repositories.AuthRepository
import com.example.firebaseecom.repositories.AuthRepositoryImpl.Companion.userStateFlow
import com.example.firebaseecom.repositories.FirestoreRepository
import com.example.firebaseecom.repositories.StorageRepository
import com.example.firebaseecom.utils.AlertDialogUtils
import com.example.firebaseecom.utils.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {
    val user = UserModel("","", "", "", "", "")
    val userDetails = MutableLiveData<UserModel>()
    val userImage = MutableLiveData<String>()
    val userState = MutableLiveData<UserState>()
    val showLoading = MutableLiveData<Boolean>()
    fun updateUser(userModel: UserModel) {
        viewModelScope.launch(Dispatchers.IO) {
            firestoreRepository.addToUsers(userModel)
        }
    }

    fun userData() {
        viewModelScope.launch {
            getUserData()
        }
    }

    private suspend fun getUserData() {
        withContext(Dispatchers.IO) {
            val user = firestoreRepository.getFromUsers()!!
            userDetails.postValue(user)
        }
    }


    suspend fun storeImageAndGetUrl(imageUri: Uri?) {
        if (imageUri != null) {
            viewModelScope.launch(Dispatchers.IO) {
                storageRepository.storeImageAndGetUrl(imageUri).collect { downloadUrl ->
                    Log.d("ImageDownloadUrlModel", downloadUrl)
                    userImage.postValue(downloadUrl)
                }
            }
        } else {
            userImage.postValue(" ")
        }
    }

    private fun deleteUser(password: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.userDelete(password!!)
            userStateFlow.collect { userStateFlow ->
                Log.d("userDeleteViewM",userStateFlow.toString())
                userState.postValue(userStateFlow)
            }

        }
    }

    fun showUserDeletePrompt(context: Context) {
        AlertDialogUtils().apply {
            Log.d("UserDelete", "clicked")
            showDeleteUserDialog(context)
            viewModelScope.launch(Dispatchers.IO) {
                responsePassword.collect { userResponse ->
                    when (userResponse) {
                        null -> {

                        }

                        else -> {
                            showLoading.postValue(true)
                            deleteUser(userResponse)
                        }
                    }
                }
            }
        }
    }
    fun resetUserState()
    {
        userStateFlow.value = UserState.LoggedIn
    }

}



