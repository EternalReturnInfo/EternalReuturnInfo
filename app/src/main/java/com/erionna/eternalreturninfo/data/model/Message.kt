package com.erionna.eternalreturninfo.data.model

data class Message(
    val id: String? = null,
    var message: String? = null,
    var sendId: String? = null,
    var receiverId: String? = null,
    var time: String? = null,
    var readOrNot: Boolean? = null,
    var whereRU: Boolean? = null
) {
    constructor() : this("", "", "", "", "",null, null)
}
