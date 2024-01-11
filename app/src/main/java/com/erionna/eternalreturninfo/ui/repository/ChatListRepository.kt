package com.erionna.eternalreturninfo.ui.repository

import com.erionna.eternalreturninfo.model.ERModel

interface ChatListRepository {
    fun addUser(item: ERModel?) : List<ERModel>
    fun currentList()
    fun clearList()
    fun modifyItemForCallBack()
    fun modifyItemForChatList()

}