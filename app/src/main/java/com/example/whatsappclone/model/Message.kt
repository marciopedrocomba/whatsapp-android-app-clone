package com.example.whatsappclone.model

data class Message(
    var userId: String?,
    var name: String?,
    var message: String?,
    var image: String?,
    var senderImage: String?
) {

    constructor(): this(null, "", null, null, null)

}