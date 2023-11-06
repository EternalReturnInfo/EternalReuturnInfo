package com.erionna.eternalreturninfo.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.erionna.eternalreturninfo.model.Notice
import com.erionna.eternalreturninfo.model.VideoModel
import com.erionna.eternalreturninfo.retrofit.BoardSingletone
import com.erionna.eternalreturninfo.retrofit.RetrofitInstance
import com.erionna.eternalreturninfo.retrofit.UserStats
import com.erionna.eternalreturninfo.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainListViewModel() : ViewModel(){

    private val _noticeList = MutableLiveData<MutableList<Notice>>()
    val noticeList: LiveData<MutableList<Notice>>
        get() = _noticeList

    private val _videoList = MutableLiveData<MutableList<VideoModel>>()
    val videoList: LiveData<MutableList<VideoModel>>
        get() = _videoList

    private val _userRecordList = MutableLiveData<UserStats?>()
    val userRecordList: LiveData<UserStats?>
        get() = _userRecordList



    init {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val call = RetrofitInstance.eternal_api.getNews("locale=ko_KR")
                val response = call.execute()

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody != null) {
                        val articles = responseBody.articles

                        val list = mutableListOf<Notice>()
                        articles.mapNotNull { list.add(Notice(it.thumbnail_url, it.i18ns["ko_KR"]?.title, it.i18ns["ko_KR"]?.summary, it.i18ns["ko_KR"]?.created_at_for_humans, it.url))}

                        _noticeList.postValue(list)

                    }
                } else {
                    println("API 요청 실패: ${response.code()}")
                }


                //전적 검색
                val nickname = fetchNonNullableNickname()

                //수정 : 로그인한 사람 닉네임 가져오기
                val userID_call = RetrofitInstance.search_userID_api.getUserByNickname(Constants.MAIN_APIKEY, nickname)
                val userID_response = userID_call.execute()

                if (userID_response.isSuccessful) {
                    val gameResponse = userID_response.body()
                    val userNum = gameResponse?.user?.userNum.toString()
                    val seasonId = "19"

                    val userstate_call = RetrofitInstance.search_user_state_api.getUserStats(
                        Constants.MAIN_APIKEY, userNum, seasonId)
                    val userstate_response = userstate_call.execute()

                    if (userstate_response.isSuccessful) {
                        val userStateResponse = userstate_response.body()
                        val user = userStateResponse?.userStats?.get(0)

                        _userRecordList.postValue(user)

                    } else {

                    }
                }

            }catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewModelScope.launch {
            fetchItemResults()
        }


    }

    private suspend fun fetchItemResults() {

        val resItems: ArrayList<VideoModel> = ArrayList()

        try {
            val query = "이터널리턴"

            val response = RetrofitInstance.api.getYouTubeVideos(
                query = query,
                maxResults = 10,
                videoOrder = "relevance"
            )

            resItems.clear()

            if (response.isSuccessful) {
                val youtubeVideo = response.body()!!

                youtubeVideo?.items?.forEach { snippet ->
                    val title = snippet.snippet.title
                    val url = snippet.snippet.thumbnails.medium.url
                    resItems.add(VideoModel(id= snippet.id.videoId, title = title, thumbnail = url,  url = "https://www.youtube.com/watch?v=${snippet.id}"))
                }
            }else{
            }

            _videoList.postValue(resItems.toMutableList())

        } catch (e: Exception) {
        }
    }

    private suspend fun fetchNonNullableNickname(): String {
        var nickname: String? = null
        while (nickname == null) {
            nickname = BoardSingletone.LoginUser().name
            if (nickname == null) {
                // 닉네임이 null인 경우 잠시 대기하거나 다른 작업 수행
                delay(1000)
            }
        }
        return nickname
    }

}

class MainListViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainListViewModel::class.java)) {
            return MainListViewModel() as T
        } else {
            throw IllegalArgumentException("Not found ViewModel class.")
        }
    }
}
