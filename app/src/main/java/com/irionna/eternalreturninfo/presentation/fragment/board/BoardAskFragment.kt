package com.irionna.eternalreturninfo.presentation.fragment.board

import androidx.fragment.app.activityViewModels
import com.irionna.eternalreturninfo.presentation.viewmodel.BoardListViewModel

class BoardAskFragment : BaseFragment() {

    companion object {
        fun newInstance() = BoardAskFragment()
    }

    private val boardViewModel: BoardListViewModel by activityViewModels()

    override fun initModel() = with(boardViewModel) {
        boardList.observe(viewLifecycleOwner) {

            val noticeList = it.filter { it.category == "질문" }

            listAdapter.submitList(
                noticeList.toMutableList().reversed()
            )
        }
    }

}