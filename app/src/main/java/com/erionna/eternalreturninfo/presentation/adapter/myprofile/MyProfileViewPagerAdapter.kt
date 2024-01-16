package com.erionna.eternalreturninfo.presentation.adapter.myprofile

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.model.MainTabs
import com.erionna.eternalreturninfo.presentation.fragment.myprofile.MyProfileBoardFragment
import com.erionna.eternalreturninfo.presentation.fragment.myprofile.MyprofileRecordFragment

class MyProfileViewPagerAdapter (fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

        private val fragments = listOf(
            MainTabs(MyprofileRecordFragment.newInstance(), R.string.myprofile_record),
            MainTabs(MyProfileBoardFragment.newInstance(), R.string.myprofile_board),
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