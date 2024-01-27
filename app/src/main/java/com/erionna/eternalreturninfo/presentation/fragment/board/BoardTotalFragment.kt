package com.erionna.eternalreturninfo.presentation.fragment.board

import androidx.fragment.app.activityViewModels
import com.erionna.eternalreturninfo.presentation.viewmodel.BoardListViewModel

class BoardTotalFragment : BaseFragment() {

    companion object {
        fun newInstance() = BoardTotalFragment()
    }

    private val boardViewModel: BoardListViewModel by activityViewModels()

    override fun initModel() = with(boardViewModel) {
        boardList.observe(viewLifecycleOwner) {

            val (noticeItems, nonNoticeItems) = it.partition { it.category == "공지" }
            val sortedNonNoticeItems = nonNoticeItems.sortedBy { it.date }
            val combinedList = sortedNonNoticeItems + noticeItems

            listAdapter.submitList(
                combinedList.reversed()
            )
        }
    }

}