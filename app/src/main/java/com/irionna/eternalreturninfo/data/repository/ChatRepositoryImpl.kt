package com.irionna.eternalreturninfo.data.repository

import com.irionna.eternalreturninfo.data.model.Message
import com.irionna.eternalreturninfo.presentation.repository.ChatRepository

class ChatRepositoryImpl() : ChatRepository {

    private val list = mutableListOf<Message>()

    override fun addItem(message: Message?): List<Message> {
        if (message == null) {
            return list
        }

        list.apply {
            add(
                message
            )
        }
        return ArrayList<Message>(list)
    }

    override fun clearList(): List<Message> {
        list.clear()
        return ArrayList<Message>(list)
    }

    override fun getListSize(): Int {
        return ArrayList<Message>(list).size
    }

}