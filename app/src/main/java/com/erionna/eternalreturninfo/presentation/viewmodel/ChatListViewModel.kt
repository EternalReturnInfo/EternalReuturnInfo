package com.erionna.eternalreturninfo.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.erionna.eternalreturninfo.data.repository.ChatListRepositoryImpl
import com.erionna.eternalreturninfo.model.ERModel
import com.erionna.eternalreturninfo.presentation.repository.ChatListRepository
import java.util.concurrent.atomic.AtomicLong

class ChatListViewModel(
    val repository: ChatListRepository
) : ViewModel() {
    private val _list: MutableLiveData<List<ERModel>> = MutableLiveData()
    val list: LiveData<List<ERModel>> get() = _list

    fun addUser(
        item: ERModel?
    ) {
        _list.value = repository.addUser(item)
    }

    fun clearList() {
        _list.value = repository.clearList()
    }

    fun modifyItemForCallBack(position: Int, message: String, time: String) {
        _list.value = repository.modifyItemForCallBack(position, message, time)
    }

    fun modifyItemForChatList(item: ERModel?) {
        _list.value = repository.modifyItemForChatList(item)
    }

}

class ChatListViewModelFactory : ViewModelProvider.Factory {
    private val repository: ChatListRepository = ChatListRepositoryImpl(
        AtomicLong(1L)
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) {
            return ChatListViewModel(repository) as T
        } else {
            throw IllegalArgumentException("not found correct viewModel")
        }
    }
}