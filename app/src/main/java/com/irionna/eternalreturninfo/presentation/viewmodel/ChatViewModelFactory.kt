package com.irionna.eternalreturninfo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.irionna.eternalreturninfo.data.repository.ChatRepositoryImpl
import com.irionna.eternalreturninfo.presentation.repository.ChatRepository

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