package com.irionna.eternalreturninfo.presentation.fragment.myprofile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.irionna.eternalreturninfo.R
import com.irionna.eternalreturninfo.databinding.MyprofileRecordFragmentBinding
import com.irionna.eternalreturninfo.retrofit.BoardSingletone
import com.irionna.eternalreturninfo.retrofit.CharacterStats
import com.irionna.eternalreturninfo.retrofit.RetrofitInstance
import com.irionna.eternalreturninfo.presentation.adapter.myprofile.MyprofileListAdapter
import com.irionna.eternalreturninfo.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyprofileRecordFragment : Fragment() {
    companion object {
        fun newInstance() = MyprofileRecordFragment()
    }

    private var _binding: MyprofileRecordFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MyprofileRecordFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun initView() = with(binding) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val nickname = BoardSingletone.LoginUser().name.toString()

                val userIdCall = RetrofitInstance.searchUserIDApi.getUserByNickname(
                    Constants.MAIN_APIKEY,
                    nickname
                )
                val userIDResponse = userIdCall.execute()

                if (userIDResponse.isSuccessful) {
                    val gameResponse = userIDResponse.body()

                    if(gameResponse != null){
                        val userNum = gameResponse?.user?.userNum.toString()
                        val seasonId = BoardSingletone.seasonID()

                        val userStateCall = RetrofitInstance.searchUserStateApi.getUserStats(
                            Constants.MAIN_APIKEY, userNum, seasonId
                        )
                        val userStateResponse = userStateCall.execute()

                        if (userStateResponse.isSuccessful) {
                            val userState = userStateResponse.body()

                            if(userStateResponse != null){

                                withContext(Dispatchers.Main) {
                                    val user = userState?.userStats?.get(0)

                                    if(user != null){
                                        binding.myprofileRecordTvGameCount.text = user?.totalGames.toString()
                                        binding.myprofileRecordTvWinning.text = user?.top1?.times(100).toString() + "%"
                                        binding.myprofileRecordTvAverage.text = "#" + user?.averageRank.toString()

                                        binding.myprofileRecordTvTier.text = getRank(user?.mmr, user?.rank)

                                        var dataList = mutableListOf<CharacterStats>()
                                        for(a in 0..2){
                                            dataList.add(CharacterStats(user!!.characterStats[a].characterCode,user.characterStats[a].totalGames,user.characterStats[a].usages,user.characterStats[a].maxKillings,user.characterStats[a].top3,user.characterStats[a].wins,user.characterStats[a].top3Rate,user.characterStats[a].averageRank))
                                        }

                                        val array: Array<String> = resources.getStringArray(R.array.characterName)
                                        val adapter = MyprofileListAdapter(dataList, array)
                                        binding.myprofileCharacterRv.adapter = adapter
                                        binding.myprofileCharacterRv.layoutManager = LinearLayoutManager(requireContext())
                                    }
                                }
                            }

                        } else {
                            Log.d("userStateResponse", "${userStateResponse}")
                        }
                    }

                }

            } catch (e: Exception) {
                // 오류 처리
                e.printStackTrace()
            }
        }
    }

    fun getRank(mmr: Int?, rank: Int?): String {
        return when {
            mmr == null || mmr < 0 -> "-"
            mmr < 1000 -> "아이언"
            mmr < 2000 -> "브론즈"
            mmr < 3000 -> "골드"
            mmr < 5000 -> "플래티넘"
            mmr < 6000 -> "다이아몬드"
            rank != null && mmr >= 6000 -> {
                if(rank <= 200){
                    return "이터너티"
                }else if(rank <= 700){
                    return "데미갓"
                }else{
                    return "미스릴"
                }
            }

            else -> "-"
        }
    }

}