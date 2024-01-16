package com.erionna.eternalreturninfo.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.erionna.eternalreturninfo.model.BoardModel
import com.erionna.eternalreturninfo.model.CommentModel
import com.erionna.eternalreturninfo.retrofit.FBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class BoardListViewModel() : ViewModel(){

    private val _boardList = MutableLiveData<MutableList<BoardModel>>()
    val boardList: LiveData<MutableList<BoardModel>>
        get() = _boardList

    private val _commentList = MutableLiveData<MutableList<CommentModel>>()
    val commentList: LiveData<MutableList<CommentModel>>
        get() = _commentList

    private val _searchBoardList = MutableLiveData<MutableList<BoardModel>>()
    val searchBoardList: LiveData<MutableList<BoardModel>>
        get() = _searchBoardList


    init {
        FBRef.postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val unsortedBoardList = mutableListOf<BoardModel>()
                for (data in snapshot.children) {
                    val board = data.getValue<BoardModel>()
                    if (board != null) {
                        unsortedBoardList.add(board)
                    }
                }
                _boardList.value = unsortedBoardList
            }

            override fun onCancelled(error: DatabaseError) {
                // 에러 처리
            }
        })
    }

    fun initComment(items: MutableList<CommentModel>) {
        if (items == null) {
            return
        }
        _commentList.value = items
    }

    fun initBoard(items: MutableList<BoardModel>) {
        if (items == null) {
            return
        }
        _boardList.value = items
    }

    fun addSearchBoard(item: BoardModel) {
        if (item == null) {
            return
        }

        val currentList = searchBoardList.value.orEmpty().toMutableList()
        currentList.add(item)
        val sortedList = currentList.sortedBy { it.date }
        _searchBoardList.value = sortedList.toMutableList()
    }

    fun clearSearchBoard(){
        _searchBoardList.value = mutableListOf()
    }

    fun updateBoard(item: BoardModel) {

        fun findIndex(item: BoardModel?): Int {
            val currentList = boardList.value.orEmpty().toMutableList()
            // 같은 id 를 찾음
            val findTodo = currentList.find {
                it.id == item?.id
            }

            // 찾은 model 기준으로 index 를 찾음
            return currentList.indexOf(findTodo)
        }

        if (item == null) {
            return
        }

        // position 이 null 이면 indexOf 실시
        val findPosition = findIndex(item)
        if (findPosition < 0) {
            return
        }

        val currentList = boardList.value.orEmpty().toMutableList()
        currentList[findPosition] = item
        _boardList.value = currentList
    }

    fun removeComment(position: Int?) {
        if (position == null || position < 0) {
            return
        }

        val currentList =  commentList.value.orEmpty().toMutableList()
        currentList.removeAt(position)
        _commentList.value = currentList
    }

}

class BoardListViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoardListViewModel::class.java)) {
            return BoardListViewModel() as T
        } else {
            throw IllegalArgumentException("Not found ViewModel class.")
        }
    }
}
