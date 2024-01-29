package com.irionna.eternalreturninfo.retrofit

import com.irionna.eternalreturninfo.util.Constants
import com.irionna.eternalreturninfo.util.Constants.Companion.BASE_URL
import com.irionna.eternalreturninfo.util.Constants.Companion.ETERNAL_RETURN_BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api : YoutubeApiService by lazy {
        retrofit.create(YoutubeApiService::class.java)
    }

    private val eternalRetrofit by lazy {
        val okHttpClient = OkHttpClient.Builder().addInterceptor(
            HttpLoggingInterceptor(logger = HttpLoggingInterceptor.Logger.DEFAULT).apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        ).build()

        Retrofit.Builder()
            .baseUrl(ETERNAL_RETURN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val eternalApi by lazy {
        eternalRetrofit.create(ApiService::class.java)
    }

    private val userRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.MAIN_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val searchUserIDApi by lazy {
        userRetrofit.create(UserService::class.java)
    }

    val searchUserStateApi by lazy {
        userRetrofit.create(UserStatsService::class.java)
    }

}
