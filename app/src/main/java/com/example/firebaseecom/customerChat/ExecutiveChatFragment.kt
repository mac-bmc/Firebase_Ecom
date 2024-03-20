@file:Suppress("DEPRECATION")

package com.example.firebaseecom.customerChat

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebaseecom.R
import com.example.firebaseecom.databinding.FragmentExecutiveChatBinding
import com.example.firebaseecom.model.UserModel
import com.example.firebaseecom.model.message.MessageModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExecutiveChatFragment : Fragment() {

    private lateinit var chatBinding: FragmentExecutiveChatBinding
    private lateinit var adapter: ChatAdapter<MessageModel>
    private var userData: UserModel? = null
    private var populationData = mutableListOf<MessageModel>()
    private var syncRunnable: Runnable? = null
    val handler = Handler()

    companion object {
        fun newInstance() = ExecutiveChatFragment()
    }

    private lateinit var viewModel: ExecutiveChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        chatBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_executive_chat, container, false)
        chatBinding.apply {
            sendQuery.setOnClickListener {
                sendMessage()

            }
            adapter = ChatAdapter(populationData, userData)
            chatView.adapter = adapter
            chatView.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        }
        attachListeners()

        return chatBinding.root
    }

    private fun sendMessage() {
        viewModel.sendMessages(chatBinding.chatQuery.text.toString())
        chatBinding.chatQuery.text.clear()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = requireActivity().intent.extras
        userData = if (bundle?.getSerializable("userData", UserModel::class.java) != null) {
            bundle.getSerializable("userData", UserModel::class.java)
        } else {
            null
        }
        viewModel = ViewModelProvider(this)[ExecutiveChatViewModel::class.java]
        viewModel.getExecutive()
    }

    private fun attachListeners() {
        attachExecutiveStatus()

    }

    private fun attachExecutiveStatus() {
        viewModel.executiveStatus.observe(viewLifecycleOwner) { status ->
            if (status) {
                attachPopulateMsg()
            }
        }
    }

    private fun attachPopulateMsg() {
        viewModel.getMessages()
        viewModel.msgLiveData.observe(viewLifecycleOwner) { data ->
            populationData.clear()
            if (data != null) {
                for (doc in data) {
                    populationData.add(doc)
                    Log.d("populationData", populationData.toString())
                    adapter.notifyItemChanged(populationData.size - 1)
                }
            }


        }
    }

    private fun pauseDataSync() {
        syncRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    private fun startDataSync() {
        syncRunnable = object : Runnable {
            override fun run() {
                try {
                    viewModel.getMessages()
                } catch (e: Exception) {
                    Log.d("TIMER", e.toString())
                } finally {
                    handler.postDelayed(this, 1000L)
                }
            }

        }
        handler.postDelayed(syncRunnable!!, 1000L)
    }


}