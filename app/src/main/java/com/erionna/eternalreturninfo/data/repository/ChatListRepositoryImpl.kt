package com.erionna.eternalreturninfo.data.repository

import android.util.Log
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

    override fun clearList() : MutableList<ERModel> {
        list.clear()
        return list
    }

    override fun modifyItemForCallBack(
        position: Int,
        message: String,
        time: String
    ): List<ERModel> {

        list[position].msg = message

        val sb = StringBuilder()
        if (time != "") {
            sb.append(time)
            list[position].time = sb.substring(0,13)
        } else {
            list[position].time = ""
        }

        Log.d("choco5733 : 뷰모델 ", "${list[position]}")

        return list
    }

    override fun modifyItemForChatList() {
        TODO("Not yet implemented")
    }
}