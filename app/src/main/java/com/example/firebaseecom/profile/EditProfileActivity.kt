@file:Suppress("DEPRECATION")

package com.example.firebaseecom.profile

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.firebaseecom.R
import com.example.firebaseecom.auth.SignUpActivity
import com.example.firebaseecom.databinding.ActivityEditProfileBinding
import com.example.firebaseecom.main.BaseActivity
import com.example.firebaseecom.model.UserModel
import com.example.firebaseecom.repositories.AuthRepositoryImpl
import com.example.firebaseecom.utils.ToastUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileActivity : BaseActivity() {
    private lateinit var activityEditProfileBinding: ActivityEditProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var imgUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                android.Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_DENIED
        ) {
            val permissionArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                )
            } else {
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            requestPermissions(permissionArray, 123)
        }
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode != RESULT_CANCELED) {
                    imgUri = result.data?.data!!
                    if (imgUri != null) {
                        Glide.with(this).load(imgUri.toString()).error(R.drawable.placeholder_image)
                            .into(activityEditProfileBinding.userProfileImage)

                    }
                }

            }
        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode != RESULT_CANCELED) {
                    if (imgUri != null) {
                        Glide.with(this).load(imgUri.toString()).error(R.drawable.placeholder_image)
                            .into(activityEditProfileBinding.userProfileImage)

                    }
                }


            }
        activityEditProfileBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        profileViewModel = ViewModelProvider(this@EditProfileActivity)[ProfileViewModel::class.java]
        getPlaceholderData()
        activityEditProfileBinding.apply {
            navPop.setOnClickListener {
                finish()
            }
            submitBtn.setOnClickListener {
                updateProfileData(imgUri)
            }
            imageAdd.setOnClickListener {
                selectUserProfileImage()

            }
        }
    }

    private fun getPlaceholderData() {
        activityEditProfileBinding.apply {
            userDetails = intent.extras!!.get("user") as UserModel
            Log.d("userDeta",userDetails.toString())
            Glide.with(this@EditProfileActivity).load(userDetails?.userImg).error(R.drawable.ic_add)
                .into(userProfileImage)
        }
    }


    private fun selectUserProfileImage() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.profile_image))
        builder.setPositiveButton(R.string.Gallery) { _, _ ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(intent)

        }
        builder.setNeutralButton(R.string.Camera) { _, _ ->
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "ProfileImage")
            values.put(MediaStore.Images.Media.DESCRIPTION, "from camera")
            imgUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
            cameraLauncher.launch(intent)

        }
        builder.setNegativeButton(R.string.cancel) { _, _ ->

        }
        builder.show()
    }

    private fun updateProfileData(imgUri: Uri?) {
        activityEditProfileBinding.apply {
            progressBar.isVisible = true
            ToastUtils().giveToast(getString(R.string.please_wait), this@EditProfileActivity)
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    profileViewModel.storeImageAndGetUrl(imgUri)
                    profileViewModel.userImage.observe(
                        this@EditProfileActivity
                    ) { imgUrl ->
                        Log.d("ImageActivity", imgUrl)
                        when (imgUrl) {
                            " " -> {
                                val user = UserModel(
                                    "",
                                    editTextUsername.text.toString(),
                                    editTextEmail.text.toString(),
                                    userDetails!!.userImg,
                                    editTextPhone.text.toString(),
                                    editTextAddress.text.toString()
                                )
                                Log.d("dataUpdate", user.toString())
                                profileViewModel.updateUser(user)
                                progressBar.isVisible = false
                                ToastUtils().giveToast(
                                    getString(R.string.data_updated),
                                    this@EditProfileActivity
                                )
                                finish()
                            }

                            else -> {
                                val user = UserModel(
                                    "",
                                    editTextUsername.text.toString(),
                                    editTextEmail.text.toString(),
                                    imgUrl,
                                    editTextPhone.text.toString(),
                                    editTextAddress.text.toString()
                                )
                                Log.d("dataUpdate", user.toString())
                                profileViewModel.updateUser(user)
                                progressBar.isVisible = false
                                ToastUtils().giveToast(
                                    getString(R.string.data_updated),
                                    this@EditProfileActivity
                                )
                                finish()
                            }
                        }
                    }
                }
            }
        }


    }

    inner class AuthStateChangeImpl : AuthRepositoryImpl.AuthStateChange {
        override fun navToSignUp() {
            Log.d("After email verification", "navToSignUp called")
            val intent = Intent(this@EditProfileActivity, SignUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

    }

}