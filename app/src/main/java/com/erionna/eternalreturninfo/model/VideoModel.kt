package com.erionna.eternalreturninfo.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class VideoModel(
    val id: String?,
    val thumbnail: String?,
    val title: String?,
    val url: String?
):Parcelable