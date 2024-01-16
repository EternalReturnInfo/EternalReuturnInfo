package com.erionna.eternalreturninfo.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.w3c.dom.Comment
import java.util.Date

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
