package com.erionna.eternalreturninfo.presentation.fragment.board

import androidx.fragment.app.activityViewModels
import com.erionna.eternalreturninfo.presentation.viewmodel.BoardListViewModel

class BoardNoticeFragment : BaseFragment() {

    companion object {
        fun newInstance() = BoardNoticeFragment()
    }

    private val boardViewModel: BoardListViewModel by activityViewModels()

    override fun initModel() = with(boardViewModel) {
        boardList.observe(viewLifecycleOwner) {

            val noticeList = it.filter { it.category == "공지" }

            listAdapter.submitList(
                noticeList.toMutableList().reversed()
            )
        }
    }

}