package com.erionna.eternalreturninfo.presentation.adapter.board

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.model.MainTabs
import com.erionna.eternalreturninfo.presentation.fragment.board.BoardAskFragment
import com.erionna.eternalreturninfo.presentation.fragment.board.BoardDailyFragment
import com.erionna.eternalreturninfo.presentation.fragment.board.BoardNoticeFragment
import com.erionna.eternalreturninfo.presentation.fragment.board.BoardTipsFragment
import com.erionna.eternalreturninfo.presentation.fragment.board.BoardTotalFragment

class BoardViewPagerAdapter (fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

        private val fragments = listOf(
            MainTabs(BoardTotalFragment.newInstance(), R.string.board_total),
            MainTabs(BoardNoticeFragment.newInstance(), R.string.board_notice),
            MainTabs(BoardAskFragment.newInstance(), R.string.board_ask),
            MainTabs(BoardTipsFragment.newInstance(), R.string.board_tips),
            MainTabs(BoardDailyFragment.newInstance(), R.string.board_daily)
        )

        fun getFragment(position: Int): Fragment {
            return fragments[position].fragment
        }

        fun getTitle(position: Int): Int {
            return fragments[position].titleRes
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position].fragment
        }
        override fun getItemCount(): Int {
            return fragments.size
        }
}