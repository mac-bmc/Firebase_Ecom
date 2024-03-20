package com.example.firebaseecom.customerChat

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebaseecom.model.message.ExecutiveModel
import com.example.firebaseecom.model.message.MappedExecModel
import com.example.firebaseecom.model.message.MessageModel
import com.example.firebaseecom.repositories.AuthRepository
import com.example.firebaseecom.repositories.FirestoreRepository
import com.example.firebaseecom.repositories.FirestoreRepositoryImpl.Companion.messageFlow
import com.example.firebaseecom.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ExecutiveChatViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _msgLiveData = MutableLiveData<List<MessageModel>?>()
    val msgLiveData = _msgLiveData

    private val _sendMessageStatus = MutableLiveData<Boolean?>()
    val sendMessageStatus = _sendMessageStatus

    private val _executiveStatus = MutableLiveData(false)
    val executiveStatus = _executiveStatus

    companion object {
        private var assigned_Executive: ExecutiveModel? = null
    }

    fun getMessages() {
        Log.d("ReceiveListVM", "called")
        viewModelScope.launch(Dispatchers.IO) {

            firestoreRepository.syncDataOnChange(assigned_Executive!!)
            messageFlow.collect {
                Log.d("RecieveListVMM", it.toString())
                _msgLiveData.postValue(checkForSendUser(it))
            }
        }
    }

    private fun checkForSendUser(messages: List<MessageModel>): List<MessageModel> {
        for (message in messages) {
            if (authRepository.checkForSender(message))
                message.isSendByMe = true
        }
        return messages
    }

    fun sendMessages(messageString: String) {
        Log.d("SENDVM", messageString)
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = firestoreRepository.sendMessages(getMessageModel(messageString))) {
                is Resource.Success -> {
                    _sendMessageStatus.postValue(response.data)
                }

                is Resource.Failed -> {
                    _sendMessageStatus.postValue(false)
                }

                else -> {}
            }
        }
    }

    private fun getMessageModel(messageString: String): MessageModel {
        return MessageModel(
            UUID.randomUUID().toString(), messageString, Calendar.getInstance().time.toString(),
            System.currentTimeMillis().toInt(), "", assigned_Executive!!.execId, true
        )
    }

    fun getExecutive() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = firestoreRepository.getRandomExec()) {
                is Resource.Success -> {
                    if (response.data.isOccupied)
                        getExecutive()
                    else {
                        _executiveStatus.postValue(true)
                        assigned_Executive = response.data
                        firestoreRepository.updateMapping(
                            MappedExecModel(UUID.randomUUID().toString(), assigned_Executive, null)
                        )
                    }

                }

                else -> {}
            }
        }
    }

}