package com.erionna.eternalreturninfo.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var server: String? = "",
    var gender: String? = "",
    var tier: String? = "",
    var most: String? = "",
    var uid: String? = ""
)