package com.example.firebaseecom.repositories

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.firebaseecom.R
import com.example.firebaseecom.auth.SignUpActivity
import com.example.firebaseecom.model.message.MessageModel
import com.example.firebaseecom.utils.Resource
import com.example.firebaseecom.utils.UserState
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject


interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun userLogin(email: String, password: String): Resource<FirebaseUser>
    suspend fun userSignUp(email: String, password: String, phNum: String): Resource<FirebaseUser>
    fun userSignOut()

    fun forgotPassword(email: String)

    fun checkForNewUser(): Boolean

    suspend fun sendOtpCode(number: String, activity: SignUpActivity)

    suspend fun userDelete(password: String)

    fun checkForSender(msg: MessageModel): Boolean

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

    var phoneAuthId = ""


    companion object {
        private var isNewUser = false
        val userStateFlow = MutableStateFlow<UserState>(UserState.LoggedIn)
    }


    override suspend fun userLogin(email: String, password: String): Resource<FirebaseUser> {

        return try {
            val user = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            userStateFlow.value = UserState.LoggedIn
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
                userStateFlow.value = UserState.SignedUp
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
            userStateFlow.value = UserState.LoggedOut
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

    override suspend fun sendOtpCode(number: String, activity: SignUpActivity) {
        val options: PhoneAuthOptions = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setCallbacks(mCallBack)
            .setPhoneNumber(number)
            .setActivity(activity)// Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val mCallBack: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                phoneAuthId = s
            }


            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

                val code = phoneAuthCredential.smsCode


                if (code != null) {

                }
            }


            override fun onVerificationFailed(e: FirebaseException) {
                // displaying error message with firebase exception.
                Log.d("phoneAuth", e.toString())
            }
        }

    override suspend fun userDelete(password: String) {

        val user = firebaseAuth.currentUser
        val credential = EmailAuthProvider.getCredential(currentUser?.email!!, password)
        try {
            Tasks.await(
                user?.reauthenticate(credential)
                !!.addOnCompleteListener { reAuth ->
                    if (reAuth.isSuccessful) {
                        user.delete().addOnCompleteListener { userDelete ->
                            if (userDelete.isSuccessful) {
                                Log.d("userDelete", "deleted")
                                userStateFlow.value = UserState.Deleted("User Deleted")
                            }
                        }

                    }
                }


            )
        } catch (e: Exception) {
            Log.d("userDeleteException", e.toString())
            userStateFlow.value = UserState.DeleteFailure("Something went wrong Try Again later")
        }


    }

    override fun checkForSender(msg: MessageModel): Boolean {
        return msg.sendUserId == firebaseAuth.currentUser!!.uid
    }


    private fun isValidated(password: String, phNum: String): String {
        var msg = ""
        var nD = 0
        var nL = 0

        if (password.length < 6) {
            msg = context.getString(R.string.lengthMsg)
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