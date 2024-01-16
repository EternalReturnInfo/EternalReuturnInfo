package com.erionna.eternalreturninfo.presentation.repository

import com.erionna.eternalreturninfo.model.ERModel

interface ChatListRepository {
    fun addUser(item: ERModel?) : List<ERModel>
    fun clearList() : List<ERModel>
    fun modifyItemForCallBack(position: Int, message: String, time: String) : List<ERModel>
    fun modifyItemForChatList(item: ERModel?) : List<ERModel>
}