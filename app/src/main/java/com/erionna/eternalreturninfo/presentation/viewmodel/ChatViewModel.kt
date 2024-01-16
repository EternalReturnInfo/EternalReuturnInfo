package com.erionna.eternalreturninfo.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.erionna.eternalreturninfo.data.repository.ChatRepositoryImpl
import com.erionna.eternalreturninfo.model.Message
import com.erionna.eternalreturninfo.presentation.repository.ChatRepository

class ChatViewModel(
    val repository: ChatRepository
) : ViewModel() {

    private val _list: MutableLiveData<List<Message>> = MutableLiveData()
    val list: LiveData<List<Message>> get() = _list

    fun addItem(
        message: Message?
    ) {
        _list.value = repository.addItem(message)
    }

    fun clearList() {
        _list.value = repository.clearList()
    }

    fun getListSize(): Int {
        return repository.getListSize()
    }
}

class ChatViewModelFactory : ViewModelProvider.Factory {
    private val repository: ChatRepository = ChatRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(repository) as T
        } else {
            throw IllegalArgumentException("not found correct viewModel")
        }
    }
}
