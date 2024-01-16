package com.erionna.eternalreturninfo.presentation.fragment.board

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.erionna.eternalreturninfo.databinding.BoardRvFragmentBinding
import com.erionna.eternalreturninfo.model.BoardModel
import com.erionna.eternalreturninfo.retrofit.FBRef
import com.erionna.eternalreturninfo.presentation.activity.board.BoardDeletedActivity
import com.erionna.eternalreturninfo.presentation.activity.board.BoardPostActivity
import com.erionna.eternalreturninfo.presentation.adapter.board.BoardRecyclerViewAdapter
import com.erionna.eternalreturninfo.presentation.viewmodel.BoardListViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

abstract class BaseFragment() : Fragment() {

    var _binding: BoardRvFragmentBinding? = null
    val binding get() = _binding!!

    val listAdapter by lazy {
        BoardRecyclerViewAdapter()
    }

    private val boardViewModel: BoardListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BoardRvFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initModel()

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun initView() = with(binding){
        val loadBoardLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra("updateBoard", BoardModel::class.java)?.let { updateBoard ->
                        boardViewModel.updateBoard(updateBoard)
                    }


                } else {
                    result.data?.getParcelableExtra<BoardModel>("updateBoard")?.let { updateBoard ->
                        boardViewModel.updateBoard(updateBoard)
                    }
                }

            }else{

            }
        }

        boardNoticeRv.adapter = listAdapter
        boardNoticeRv.layoutManager = LinearLayoutManager(requireContext())

        listAdapter.setOnItemClickListener(object : BoardRecyclerViewAdapter.OnItemClickListener{
            override fun onItemClick(boardItem: BoardModel) {

                FBRef.postRef.child(boardItem.id).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            val intent = Intent(requireContext(), BoardDeletedActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent = Intent(requireContext(), BoardPostActivity::class.java)
                            intent.putExtra("ID", boardItem.id)
                            loadBoardLauncher.launch(intent)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("error", "firebase data loading failed")
                    }
                })

            }
        })
    }
    abstract fun initModel()
}