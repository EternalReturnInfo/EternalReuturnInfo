package com.erionna.eternalreturninfo.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.erionna.eternalreturninfo.model.Message

class ChatViewModel() : ViewModel() {

    private val _list: MutableLiveData<List<Message>> = MutableLiveData()
    val list: LiveData<List<Message>> get() = _list

    fun addItem(
        message: Message?
    ) {
        if (message == null) {
            return
        }
        val currentList = list.value.orEmpty().toMutableList()
        _list.value = currentList.apply {
            add(
                message
            )
        }
    }

    fun clearList() {
        val currentList = list.value.orEmpty().toMutableList()
        currentList.clear()
        _list.value = currentList
    }

    fun getListSize(): Int {
        val currentList = list.value.orEmpty().toMutableList()
        return currentList.size
    }

//    class ChatViewModelFactory : ViewModelProvider.Factory {
//        private val idGenerate = AtomicLong(1L)
//
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
//                return ChatViewModel(idGenerate) as T
//            } else {
//                throw IllegalArgumentException("not found correct viewModel")
//            }
//        }
//    }
}