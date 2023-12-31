package com.example.firebaseecom.repositories

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.firebaseecom.R
import com.example.firebaseecom.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun userLogin(email: String, password: String): Resource<FirebaseUser>
    suspend fun userSignUp(email: String, password: String, phNum: String): Resource<FirebaseUser>
    fun userSignOut()

    fun forgotPassword(email: String)

    fun checkForNewUser(): Boolean

}

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val context: Context
) : AuthRepository {

    interface AuthStateChange {
        fun navToSignUp()
    }

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    companion object {
        private var isNewUser = false
    }


    override suspend fun userLogin(email: String, password: String): Resource<FirebaseUser> {

        return try {
            val user = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(user.user!!)
        } catch (e: Exception) {
            Log.e("SignIn", "$e")
            Resource.Failed(context.getString(R.string.invalid_credentials_try_again))
        }
    }

    override suspend fun userSignUp(
        email: String,
        password: String,
        phNum: String
    ): Resource<FirebaseUser> {
        val msg = isValidated(password, phNum)
        Log.d("msg", msg)
        return if (msg == "") {
            try {
                val user = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                isNewUser = user.additionalUserInfo!!.isNewUser
                Log.d("userValueRepoOg", isNewUser.toString())
                Resource.Success(user?.user!!)
            } catch (e: FirebaseAuthException) {
                Log.e("signUp", "$e")
                val errorMsg = when (e) {
                    is FirebaseAuthUserCollisionException -> {
                        context.getString(R.string.credential_already_in_use)
                    }

                    is FirebaseAuthInvalidCredentialsException -> {
                        context.getString(R.string.credentials_not_valid)
                    }

                    else -> {
                        e.message.toString()
                    }
                }
                Resource.Failed(errorMsg)
            }

        } else {
            Resource.Failed(msg)
        }
    }


    override fun userSignOut() {
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            Log.e("signOut", "$e")
        }
    }

    override fun forgotPassword(email: String) {
        try {
            firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener {
                Toast.makeText(
                    context,
                    context.getString(R.string.check_your_mail),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        context.getString(R.string.invalid_credentials_try_again),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

        } catch (e: Exception) {
            Log.d("forgotPassword", e.toString())
        }
    }

    override fun checkForNewUser(): Boolean {
        try {
            Log.d("userValueRepo", isNewUser.toString())
            if (isNewUser) {
                return true
            }
        } catch (e: Exception) {
            Log.d("userValueRepo", e.cause.toString())
        }
        return false

    }

    private fun isValidated(password: String, phNum: String): String {
        var msg = ""
        var nD = 0
        var nL = 0

        if (password.length < 6) {
            msg = context.getString(R.string.lengthMsg)
            return msg

        }
        if (phNum.length != 10) {
            msg = context.getString(R.string.invalid_phone_number)
            return msg
        }
        for (i in password.indices) {
            if (password[i].isDigit()) {
                nD++
            }
            if (password[i].isLetter()) {
                nL++
            }

        }
        if (nD < 2) {
            msg = context.getString(R.string.DigitMsg)
        }
        if (nL < 2) {
            msg = context.getString(R.string.letterMsg)
        }
        return msg


    }
}