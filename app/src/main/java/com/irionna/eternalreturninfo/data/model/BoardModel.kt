package com.irionna.eternalreturninfo.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class BoardModel (
    val id: String = "",
    val category:String = "",
    val title: String = "",
    val content: String = "",
    val author: String? = "",
    val date: Long = 0,
    val comments: Map<String, CommentModel> = mapOf(),
    val views: Int = 0,
    val report: Map<String, ReportModel> = mapOf()
) : Parcelable

@Parcelize
@Keep
data class CommentModel(
    val id: String = "",
    val author: String? = "",
    val content: String = "",
    val date: Long = 0,
    val report: Map<String, ReportModel> = mapOf()
) : Parcelable

@Parcelize
@Keep
data class ReportModel(
    val uid: String = ""
) : Parcelable

@Parcelize
@Keep
data class ReportCommentModel(
    val uid: String = "",
    val key: String = ""
) : Parcelable


