package com.irionna.eternalreturninfo.presentation.repository

import com.irionna.eternalreturninfo.data.model.Message

interface ChatRepository {
    fun addItem(message: Message?) : List<Message>
    fun clearList() : List<Message>
    fun getListSize() : Int
}