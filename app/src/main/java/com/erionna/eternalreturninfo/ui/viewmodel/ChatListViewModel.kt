package com.erionna.eternalreturninfo.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.erionna.eternalreturninfo.data.repository.ChatListRepositoryImpl
import com.erionna.eternalreturninfo.model.ERModel
import com.erionna.eternalreturninfo.ui.repository.ChatListRepository
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
        fun findIndex(item: ERModel?): Int {
            val currentList = list.value.orEmpty().toMutableList()
            val findER = currentList.find{
                it.name == item?.name
            }
            return currentList.indexOf(findER)
        }

        if (item == null) {
            return
        }

        // position 이 null 이면 indexOf 실시
        val findPosition = findIndex(item)

        if (findPosition < 0) {
            return
        }

        val sb = StringBuilder()
        var time = ""
        if (item.time != "") {
            sb.append(item.time )
            time = sb.substring(0,13)
        } else {
            time = ""
        }


        val currentList = list.value.orEmpty().toMutableList()
        currentList[findPosition] = item.copy(time = time)
        _list.value = currentList

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