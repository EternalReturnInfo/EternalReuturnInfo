package com.erionna.eternalreturninfo.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.model.MainTabs
import com.erionna.eternalreturninfo.ui.fragment.BoardFragment
import com.erionna.eternalreturninfo.ui.fragment.ChatListFragment
import com.erionna.eternalreturninfo.ui.fragment.MainFragment
import com.erionna.eternalreturninfo.ui.fragment.MyProfileFragment
import com.erionna.eternalreturninfo.ui.fragment.SearchFragment
import com.erionna.eternalreturninfo.ui.fragment.WalkThroughFragment

class MainViewPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(
    fragmentActivity
) {

    private val fragments = listOf(
        MainTabs(MainFragment.newInstance(), R.string.main_tab_main_title),
        MainTabs(WalkThroughFragment.newInstance(), R.string.main_tab_walkthrough_title),
        MainTabs(BoardFragment.newInstance(), R.string.main_tab_board_title),
        MainTabs(ChatListFragment.newInstance(), R.string.main_tab_chat_title),
        MainTabs(MyProfileFragment.newInstance(), R.string.main_tab_search_title)
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