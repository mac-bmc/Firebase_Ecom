package com.example.firebaseecom.customerChat

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebaseecom.R
import com.example.firebaseecom.databinding.RecieverLayoutBinding
import com.example.firebaseecom.databinding.SendLayoutBinding
import com.example.firebaseecom.model.BrainShopModel
import com.example.firebaseecom.model.UserModel
import com.example.firebaseecom.model.message.MessageModel

class ChatAdapter<T : Any>(private val populationData: List<T>, private val userData: UserModel?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var recieverLayoutBinding: RecieverLayoutBinding
    private lateinit var sendLayoutBinding: SendLayoutBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        sendLayoutBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.send_layout, parent, false)
        recieverLayoutBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.reciever_layout, parent, false)

        return when (viewType) {
            0 -> {

                SendLayoutViewHolder(sendLayoutBinding)

            }

            1 -> {

                ReceiveLayoutViewHolder(recieverLayoutBinding)
            }

            else -> {
                ReceiveLayoutViewHolder(recieverLayoutBinding)
            }
        }
    }

    override fun getItemCount(): Int {
        return populationData.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val data = populationData[position]) {
            is BrainShopModel -> {
                when (data.code) {
                    0 -> {
                        Log.d("chat", "me")
                        val user = holder as SendLayoutViewHolder
                        user.sendText.text = data.cnt
                        Glide.with(holder.itemView)
                            .load(userData?.userImg)
                            .error(R.drawable.placeholder_image)
                            .into(user.sendImage)
                    }

                    1 -> {
                        Log.d("chat", "end")
                        val sender = holder as ReceiveLayoutViewHolder
                        sender.receiveText.text = data.cnt
                    }
                }
            }

            is MessageModel -> {
                when (data.isSendByMe) {
                    true -> {
                        Log.d("chat", "me")
                        val user = holder as SendLayoutViewHolder
                        user.sendText.text = data.content
                        Glide.with(holder.itemView)
                            .load(userData?.userImg)
                            .error(R.drawable.placeholder_image)
                            .into(user.sendImage)

                    }

                    false -> {
                        Log.d("chat", "other")
                        val sender = holder as ReceiveLayoutViewHolder
                        sender.receiveText.text = data.content
                    }
                }
            }

            else -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val listData = populationData[position]) {
            is BrainShopModel -> {
                when (listData.code) {
                    0 -> 0
                    1 -> 1
                    else -> -1
                }
            }

            is MessageModel -> {
                when (listData.isSendByMe) {
                    true -> 0
                    false -> 1
                }
            }

            else -> -1
        }
    }

    class SendLayoutViewHolder(sendLayoutBinding: SendLayoutBinding) :
        RecyclerView.ViewHolder(sendLayoutBinding.root) {
        var sendText = sendLayoutBinding.SendText
        var sendImage = sendLayoutBinding.userImg

    }

    class ReceiveLayoutViewHolder(recieverLayoutBinding: RecieverLayoutBinding) :
        RecyclerView.ViewHolder(recieverLayoutBinding.root) {
        var receiveText = recieverLayoutBinding.BotText


    }
}