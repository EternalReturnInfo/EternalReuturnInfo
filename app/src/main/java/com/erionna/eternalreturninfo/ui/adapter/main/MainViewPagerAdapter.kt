package com.erionna.eternalreturninfo.ui.adapter.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.model.MainTabs
import com.erionna.eternalreturninfo.ui.fragment.board.BoardFragment
import com.erionna.eternalreturninfo.ui.fragment.chat.ChatListFragment
import com.erionna.eternalreturninfo.ui.fragment.myprofile.MyProfileFragment
import com.erionna.eternalreturninfo.ui.fragment.findduo.FindDuoFragment
import com.erionna.eternalreturninfo.ui.fragment.main.MainFragment

class MainViewPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(
    fragmentActivity
) {
    private val fragments = listOf(
        MainTabs(MainFragment.newInstance(), R.string.main_tab_main_title),
        MainTabs(FindDuoFragment.newInstance(), R.string.main_tab_search_duo),
        MainTabs(BoardFragment.newInstance(), R.string.main_tab_board_title),
        MainTabs(ChatListFragment.newInstance(), R.string.main_tab_chat_title),
        MainTabs(MyProfileFragment.newInstance(), R.string.main_tab_my_profile)
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