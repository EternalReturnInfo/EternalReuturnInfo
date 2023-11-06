package com.erionna.eternalreturninfo.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.erionna.eternalreturninfo.model.ERModel
import java.util.concurrent.atomic.AtomicLong

class ChatListViewModel(
    private val idGenerate: AtomicLong
) : ViewModel() {
    private val _list: MutableLiveData<List<ERModel>> = MutableLiveData()
    val list: LiveData<List<ERModel>> get() = _list

    fun addUser(
        item: ERModel?
    ) {
        if (item == null) {
            return
        }

        val sb = StringBuilder()
        var time = ""
        if (item.time != "") {
            sb.append(item.time)
            time = sb.substring(0, 13)
        } else {
            time = ""
        }

        val currentList = list.value.orEmpty().toMutableList()
        var gotCha: Boolean? = null

        if (currentList.size > 0) {

            for (i in 0 until currentList.size) {
                if (currentList[i].name == item.name) {
                    gotCha = true
                }
            }
            if (gotCha != true) {
                _list.value = currentList.apply {
                    add(
                        item.copy(
                            time = time
                        )
                    )
                }
            }

        } else {
            _list.value = currentList.apply {
                add(
                    item.copy(
                        time = time
                    )
                )
            }
        }
    }

    fun currentList(): List<ERModel> {
        return list.value.orEmpty().toMutableList()
    }

    fun clearList() {
        val currentList = list.value.orEmpty().toMutableList()
        currentList.clear()
        _list.value = currentList
    }

    fun deleteUser(user: String) {
        val currentList = list.value.orEmpty().toMutableList()
        currentList.remove(ERModel(name=user))
        _list.value = currentList
    }

    fun modifyItem(position: Int, message: String, time: String) {
        val currentList = list.value.orEmpty().toMutableList()

        currentList[position].msg = message

        val sb = StringBuilder()
        if (time != "") {
            sb.append(time)
            currentList[position].time = sb.substring(0,13)
        } else {
            currentList[position].time = ""
        }

        _list.value = currentList

    }

    fun modifyItem2(item: ERModel?) {
        fun findIndex(item: ERModel?): Int {
            val currentList = list.value.orEmpty().toMutableList()
            val findER = currentList.find{
                it.name == item?.name
            }
            return currentList.indexOf(findER)
        }

        if (item == null) {
            return
        }

        // position 이 null 이면 indexOf 실시
        val findPosition = findIndex(item)

        if (findPosition < 0) {
            return
        }

        val sb = StringBuilder()
        var time = ""
        if (item.time != "") {
            sb.append(item.time )
            time = sb.substring(0,13)
        } else {
            time = ""
        }


        val currentList = list.value.orEmpty().toMutableList()
        currentList[findPosition] = item.copy(time = time)
        _list.value = currentList

    }
}

class ChatListViewModelFactory : ViewModelProvider.Factory {
    private val idGenerate = AtomicLong(1L)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) {
            return ChatListViewModel(idGenerate) as T
        } else {
            throw IllegalArgumentException("not found correct viewModel")
        }
    }
}