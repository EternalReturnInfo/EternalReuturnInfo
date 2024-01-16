package com.erionna.eternalreturninfo.data.repository

import android.util.Log
import com.erionna.eternalreturninfo.data.model.ERModel
import com.erionna.eternalreturninfo.presentation.repository.ChatListRepository
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
                 return ArrayList<ERModel>(list.apply {
                    add(
                        item.copy(
                            time = time
                        )
                    )
                })
            }

        }

        else {
            return ArrayList<ERModel>(list.apply {
                add(
                    item.copy(
                        time = time
                    )
                )
            })
        }
        return ArrayList<ERModel>(list)
    }

    override fun clearList() : MutableList<ERModel> {
        list.clear()
        return ArrayList<ERModel>(list)
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

        return ArrayList<ERModel>(list)
    }


    override fun modifyItemForChatList(item: ERModel?): List<ERModel> {

        fun findIndex(item: ERModel?): Int {
            val findER = list.find{
                it.name == item?.name
            }
            return list.indexOf(findER)
        }

        if (item == null) {
            return list
        }

        // position 이 null 이면 indexOf 실시
        val findPosition = findIndex(item)

        if (findPosition < 0) {
            return list
        }

        val sb = StringBuilder()
        var time = ""
        if (item.time != "") {
            sb.append(item.time )
            time = sb.substring(0,13)
        } else {
            time = ""
        }


        list[findPosition] = item.copy(time = time)
        return ArrayList<ERModel>(list)
    }
}