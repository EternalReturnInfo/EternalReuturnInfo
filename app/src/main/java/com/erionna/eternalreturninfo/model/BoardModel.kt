package com.erionna.eternalreturninfo.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
@Parcelize
data class BoardModel (
    val id: String = "",
    val category:String = "",
    val title: String = "",
    val content: String = "",
    val author: String? = "",
    val date: Long = 0,
    val comments: Map<String, CommentModel> = mapOf(),
    val views: Int = 0
) : Parcelable

@Parcelize
data class CommentModel(
    val id: String = "",
    val author: String? = "",
    val content: String = "",
    val date: Long = 0
) : Parcelable
