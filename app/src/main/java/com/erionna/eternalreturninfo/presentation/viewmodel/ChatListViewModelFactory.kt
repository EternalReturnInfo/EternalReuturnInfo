package com.erionna.eternalreturninfo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.erionna.eternalreturninfo.data.repository.ChatListRepositoryImpl
import com.erionna.eternalreturninfo.presentation.repository.ChatListRepository
import java.util.concurrent.atomic.AtomicLong

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