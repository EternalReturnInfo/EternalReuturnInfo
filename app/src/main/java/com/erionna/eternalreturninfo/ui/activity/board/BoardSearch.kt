package com.erionna.eternalreturninfo.ui.activity.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.BoardSearchActivityBinding
import com.erionna.eternalreturninfo.model.BoardModel
import com.erionna.eternalreturninfo.retrofit.FBRef
import com.erionna.eternalreturninfo.ui.adapter.board.BoardRecyclerViewAdapter
import com.erionna.eternalreturninfo.ui.viewmodel.BoardListViewModel
import com.erionna.eternalreturninfo.ui.viewmodel.BoardListViewModelFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class BoardSearch : AppCompatActivity() {

    private lateinit var binding: BoardSearchActivityBinding
    private var isBackgroundChanged = false;

    private val boardViewModel by lazy {
        ViewModelProvider(this, BoardListViewModelFactory()).get(BoardListViewModel::class.java)
    }

    private val listAdapter by lazy {
        BoardRecyclerViewAdapter()
    }

    private var postCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BoardSearchActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initModel()
    }

    private fun initView() = with(binding) {

        boardSearchRv.adapter = listAdapter
        boardSearchRv.layoutManager = LinearLayoutManager(this@BoardSearch)

        boardSearchIbBack.setOnClickListener {
            finish()
        }

        boardSearchIbClear.setOnClickListener {
            boardSearchEtSearch.setText("")
        }

        boardSearchEtSearch.setOnFocusChangeListener { view, b ->

            if(b){
                boardSearchEtSearch.setBackgroundResource(R.drawable.shape_board_search_clicked)
                boardSearchIbClear.visibility = View.VISIBLE

            } else {
                boardSearchEtSearch.setBackgroundResource(R.drawable.shape_board_search)
                boardSearchIbClear.visibility = View.INVISIBLE
            }

        }

        boardSearchEtSearch.setOnEditorActionListener { v, actionId, event ->
            if (event != null) { // event 객체가 null이 아닌 경우에만 처리
                if (actionId == EditorInfo.IME_ACTION_SEARCH || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        postCount = 0
                        boardViewModel.clearSearchBoard()
                        val searchText = boardSearchEtSearch.text.toString()
                        Search(searchText)
                    }
                }
            }
            true
        }



        listAdapter.setOnItemClickListener(object : BoardRecyclerViewAdapter.OnItemClickListener{
            override fun onItemClick(boardItem: BoardModel) {

                FBRef.postRef.child(boardItem.id).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {

                            val board = dataSnapshot.getValue<BoardModel>()

                            val views = board?.views?.plus(1)
                            FBRef.postRef.child(boardItem.id).child("views").setValue(views)
                            val intent = Intent(this@BoardSearch, BoardPost::class.java)
                            intent.putExtra("ID", boardItem.id)
                            startActivity(intent)

                        } else {
                            val intent = Intent(this@BoardSearch, BoardDeleted::class.java)
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // 데이터 읽기 실패 처리
                    }
                })
            }
        })

    }

    private fun initModel() = with(binding) {
        boardViewModel.searchBoardList.observe(this@BoardSearch){ searchBoardList ->
            val newBoardList = searchBoardList.reversed()
            listAdapter.submitList(newBoardList.toMutableList())
        }
    }

    fun Search(searchText: String)= with(binding){

        val query = FBRef.postRef.orderByChild("title").startAt(searchText).endAt(searchText + "\uf8ff")
        val query2 = FBRef.postRef.orderByChild("content").startAt(searchText).endAt(searchText + "\uf8ff")

        SearchFirebase(query)
        SearchFirebase(query2)

        if(postCount == 0){
            boardSearchTvResult.visibility = View.VISIBLE
            boardSearchTvResult.text = "검색 결과가 없습니다."
        }

    }

    fun SearchFirebase(query: Query)= with(binding){
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                boardSearchTvPostCount.visibility = View.VISIBLE
                postCount = postCount + dataSnapshot.childrenCount.toInt()
                boardSearchTvPostCount.text = postCount.toString() + " Post"

                if(dataSnapshot.exists()){
                    boardSearchTvResult.visibility = View.INVISIBLE

                    for (snapshot in dataSnapshot.children) {
                        val searchBoard = snapshot.getValue<BoardModel>()
                        if (searchBoard != null) {
                            boardViewModel.addSearchBoard(searchBoard)
                        }
                    }
                }else{
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 검색이 실패한 경우
            }
        })
    }

}