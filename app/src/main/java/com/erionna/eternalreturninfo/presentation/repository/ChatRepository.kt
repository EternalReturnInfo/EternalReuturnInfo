package com.erionna.eternalreturninfo.presentation.repository

import com.erionna.eternalreturninfo.model.Message

interface ChatRepository {
    fun addItem(message: Message?) : List<Message>
    fun clearList() : List<Message>
    fun getListSize() : Int
}