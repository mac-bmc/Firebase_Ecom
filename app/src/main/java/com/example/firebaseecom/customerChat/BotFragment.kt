package com.example.firebaseecom.customerChat

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebaseecom.R
import com.example.firebaseecom.databinding.FragmentBotBinding
import com.example.firebaseecom.model.BrainShopModel
import com.example.firebaseecom.model.UserModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BotFragment : Fragment() {

    private lateinit var botBinding: FragmentBotBinding
    private lateinit var adapter: ChatAdapter<BrainShopModel>
    private val sendKey = 0
    private var userData: UserModel? = null
    private val receiveKey = 1
    private val helloText = "Hi"
    private var brainList = mutableListOf<BrainShopModel>()
    private lateinit var chatViewModel: BotViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private companion object;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        botBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bot, container, false)
        attachListeners()
        return botBinding.root
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences =
            requireActivity().getSharedPreferences("Customer_Chat", AppCompatActivity.MODE_PRIVATE)

        editor = sharedPreferences.edit()
        editor.putInt("orderStatus", 0)
        editor.putInt("productRating", 0)
        editor.apply()
        chatViewModel = ViewModelProvider(this)[BotViewModel::class.java]
        val bundle = requireActivity().intent.extras
        userData = if (bundle?.getSerializable("userData", UserModel::class.java) != null) {
            bundle.getSerializable("userData", UserModel::class.java)
        } else {
            null
        }


    }

    private fun sendToBrain(msgQuery: String) {
        botBinding.progressBar.isVisible = true
        brainList.add(BrainShopModel(msgQuery, sendKey))
        adapter.notifyItemChanged(brainList.size - 1)
        botBinding.chatQuery.text.clear()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                if (sharedPreferences.getInt("orderStatus", 0) == 1) {
                    brainList.add(
                        BrainShopModel(
                            chatViewModel.getOrderStatus(msgQuery), receiveKey
                        )
                    )
                    botBinding.progressBar.visibility = View.GONE
                    adapter.notifyItemChanged(brainList.size - 1)
                    if (BotViewModel.ifSucessfullOrderStatusFetch || BotViewModel.incorrectStatusCounter > 2) {
                        editor.putInt("orderStatus", 0)
                        editor.apply()
                        chatViewModel.resetStatusCounter()
                    }


                } else if (sharedPreferences.getInt("productRating", 0) == 1) {
                    brainList.add(
                        BrainShopModel(
                            chatViewModel.getProductReviews(msgQuery), receiveKey
                        )
                    )
                    botBinding.progressBar.visibility = View.GONE
                    adapter.notifyItemChanged(brainList.size - 1)
                    if (BotViewModel.ifSucessfullProductReviewFetch || BotViewModel.incorrectReviewCounter > 2) {
                        editor.putInt("productRating", 0)
                        editor.apply()
                        chatViewModel.resetReviewCounter()
                    }


                } else {
                    val botResponse = chatViewModel.getResponse(msgQuery)
                    if (botResponse.contains("[1]")) {
                        brainList.add(BrainShopModel(botResponse, receiveKey))
                        botBinding.progressBar.visibility = View.GONE
                        adapter.notifyItemChanged(brainList.size - 1)
                        editor.putInt("orderStatus", 1)
                        editor.apply()
                    } else if (botResponse.contains("[2]")) {
                        brainList.add(BrainShopModel(botResponse, receiveKey))
                        botBinding.progressBar.visibility = View.GONE
                        adapter.notifyItemChanged(brainList.size - 1)
                        editor.putInt("productRating", 1)
                        editor.apply()
                    } else {
                        brainList.add(BrainShopModel(botResponse, receiveKey))
                        botBinding.progressBar.visibility = View.GONE
                        adapter.notifyItemChanged(brainList.size - 1)
                    }


                }


            }

        }


    }

    private fun attachListeners() {
        botBinding.apply {
            sendQuery.setOnClickListener {
                sendToBrain(chatQuery.text.toString())
            }
            adapter = ChatAdapter(brainList, userData)
            chatView.adapter = adapter
            chatView.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            sendToBrain(helloText)

        }
    }


}