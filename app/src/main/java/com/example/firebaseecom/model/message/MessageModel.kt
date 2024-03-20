package com.example.firebaseecom.model.message


data class MessageModel(
    val msgId: String = "",
    val content: String = "",
    val sentTime: String = "",
    val timeStamp: Int? = 0,
    var sendUserId: String = "",
    var receiveUserId: String = "",
    var isSendByMe: Boolean = false,

    )


