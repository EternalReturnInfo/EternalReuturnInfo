package com.erionna.eternalreturninfo.presentation.fragment.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.erionna.eternalreturninfo.databinding.MainFragmentBinding
import com.erionna.eternalreturninfo.model.Notice
import com.erionna.eternalreturninfo.model.VideoModel
import com.erionna.eternalreturninfo.presentation.activity.main.WebView
import com.erionna.eternalreturninfo.presentation.adapter.main.NoticeBannerListAdapter
import com.erionna.eternalreturninfo.presentation.adapter.main.VideoListAdapter
import com.erionna.eternalreturninfo.presentation.viewmodel.MainListViewModel

class MainFragment : Fragment() {
    companion object {
        fun newInstance() = MainFragment()
    }

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainListViewModel by activityViewModels()

    private val noticeListAdapter by lazy {
        NoticeBannerListAdapter()
    }

    private val videoListAdapter by lazy {
        VideoListAdapter()
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mainRvYoutube.adapter = videoListAdapter
        binding.mainRvYoutube.layoutManager = LinearLayoutManager(requireContext())

        binding.mainBannerNotice.adapter = noticeListAdapter
        val bannerVideoLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.mainBannerNotice.layoutManager = bannerVideoLayoutManager

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.mainBannerNotice)

        val listener = SnapPagerScrollListener(
            snapHelper,
            SnapPagerScrollListener.ON_SCROLL,
            true,
            object : SnapPagerScrollListener.OnChangeListener {
                override fun onSnapped(position: Int) {
                    // position을 받아서 이벤트 처리
                }
            }
        )
        binding.mainBannerNotice.addOnScrollListener(listener)
        binding.mainBannerNotice.addItemDecoration(LinePagerIndicatorDecoration())

        initView()
        initModel()
    }



    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
    private fun initView() = with(binding){


        noticeListAdapter.setOnItemClickListener(object : NoticeBannerListAdapter.OnItemClickListener{
            override fun onItemClick(item: Notice, position:Int) {
                val intent = Intent(requireContext(), WebView::class.java)
                intent.putExtra("url", item.url)
                startActivity(intent)
            }
        })

        videoListAdapter.setOnItemClickListener(object : VideoListAdapter.OnItemClickListener{
            override fun onItemClick(item: VideoModel, position: Int) {
                Log.d("item.id", item.id.toString())
                val videoUrl = "https://www.youtube.com/watch?v=${item.id}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                intent.setPackage("com.google.android.youtube")
                startActivity(intent)
            }

        })

        mainBtnSeeMore.setOnClickListener {
            val intent = Intent(requireContext(), WebView::class.java)
            intent.putExtra("url", "https://dak.gg/er")
            startActivity(intent)
        }

    }

    private fun initModel() = with(mainViewModel){
        noticeList.observe(viewLifecycleOwner){
            noticeListAdapter.submitList(it)
        }

        videoList.observe(viewLifecycleOwner){
            videoListAdapter.submitList(it)
        }

        userRecordList.observe(viewLifecycleOwner){ user ->

            fun percent(number: Float?): Float? {
                return number?.times(100)
            }

            binding.mainTvRp.text = (user?.mmr ?: 0).toString() + " PR"
            binding.mainTvRank.text =
                "${(user?.rank ?: 0)}위 (상위 ${(user?.rankPercent?.times(100) ?: 0)}%)"
            binding.mainTvTotalWin.text = (user?.totalWins ?: 0).toString()
            binding.mainTvTop1.text = (percent(user?.top1) ?: 0).toString()
            binding.mainTvTotalGames.text = (user?.totalGames ?: 0).toString()
            binding.mainTvAverageKill.text = (user?.averageKills ?: 0).toString()
            binding.mainTvTop2.text = (percent(user?.top2) ?: 0).toString()
            binding.mainTvAverageRank.text = (user?.averageRank ?: 0).toString()
            binding.mainTvAverageAssiants.text = (user?.averageAssistants ?: 0).toString()
            binding.mainTvTop3.text = (percent(user?.top3) ?: 0).toString()

            binding.mainPbTotalWin.setProgress(100)
            binding.mainPbTotalGames.setProgress(100)
            binding.mainPbTop1.setProgress(percent(user?.top1)?.toInt() ?: 0)
            binding.mainPbTop2.setProgress(percent(user?.top2)?.toInt() ?: 0)
            binding.mainPbTop3.setProgress(percent(user?.top3)?.toInt() ?: 0)
            binding.mainPbAverageKill.setProgress(user?.averageKills?.toInt() ?: 0)
            binding.mainPbAverageAssiants.setProgress(user?.averageAssistants?.toInt() ?: 0)
            binding.mainPbAverageRank.setProgress(user?.averageRank?.toInt() ?: 0)
        }

    }


}
