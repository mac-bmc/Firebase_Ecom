package com.example.firebaseecom.customerChat

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.firebaseecom.R
import com.example.firebaseecom.databinding.ActivityCustomerChatBinding
import com.example.firebaseecom.main.BaseActivity
import com.example.firebaseecom.utils.ToastUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomerChatActivity : BaseActivity() {

    private lateinit var chatBinding: ActivityCustomerChatBinding
    private val botFragment = BotFragment()
    private val chatFragment = ExecutiveChatFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatBinding = DataBindingUtil.setContentView(this, R.layout.activity_customer_chat)
        changeFragment(botFragment)
        chatBinding.apply {
            navPop.setOnClickListener {
                finish()
            }
            toExecChat.setOnClickListener {
                ToastUtils().giveToast(
                    getString(R.string.executive_is_being_assigned_to_you),
                    this@CustomerChatActivity
                )
                changeFragment(chatFragment)
            }


        }

    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(chatBinding.frameLayout.id, fragment)
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
    }


}


