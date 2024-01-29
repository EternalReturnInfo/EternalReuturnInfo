package com.irionna.eternalreturninfo.util

import com.irionna.eternalreturninfo.BuildConfig

class Constants {
    companion object {
        const val BASE_URL = "https://www.googleapis.com/youtube/v3/"
        const val AUTH_KEY = BuildConfig.YOUTUBE_APIKEY // 호식
        const val EXTRA_ER_MODEL = "extra_er_model"
        const val EXTRA_ER_POSITION = "extra_er_position"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_TIME = "extra_time"

        const val MAIN_BASE_URL = "https://open-api.bser.io"
        const val MAIN_APIKEY = BuildConfig.MAIN_APIKEY

        const val ETERNAL_RETURN_BASE_URL = "https://playeternalreturn.com"
    }

}