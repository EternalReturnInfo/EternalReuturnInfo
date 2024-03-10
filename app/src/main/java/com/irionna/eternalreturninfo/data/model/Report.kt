package com.irionna.eternalreturninfo.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Report(
    val userUid: String?,
    val userName: String?,
    val whyReport: String
) : Parcelable
