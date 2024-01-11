package com.erionna.eternalreturninfo.data.repository

import com.erionna.eternalreturninfo.model.ERModel
import com.erionna.eternalreturninfo.ui.repository.ChatListRepository
import java.util.concurrent.atomic.AtomicLong

class ChatListRepositoryImpl(
    val idGenerate: AtomicLong
) : ChatListRepository {

    private val list = mutableListOf<ERModel>()

    override fun addUser(item: ERModel?): MutableList<ERModel> {
        if (item == null) {
            return list
        }

        val sb = StringBuilder()
        var time = ""
        if (item.time != "") {
            sb.append(item.time)
            time = sb.substring(0, 13)
        } else {
            time = ""
        }

        var gotCha: Boolean? = null

        if (list.size > 0) {

            for (i in 0 until list.size) {
                if (list[i].name == item.name) {
                    gotCha = true
                }
            }
            if (gotCha != true) {
                 return list.apply {
                    add(
                        item.copy(
                            time = time
                        )
                    )
                }
            }

        }

        else {
            return list.apply {
                add(
                    item.copy(
                        time = time
                    )
                )
            }
        }
        return list
    }

    override fun currentList() {
        TODO("Not yet implemented")
    }

    override fun clearList() {
        TODO("Not yet implemented")
    }

    override fun modifyItemForCallBack() {
        TODO("Not yet implemented")
    }

    override fun modifyItemForChatList() {
        TODO("Not yet implemented")
    }
}