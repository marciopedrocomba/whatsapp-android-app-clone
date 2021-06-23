package com.example.whatsappclone.model

import com.example.whatsappclone.config.FirebaseConfig

data class Chat(
    var senderId: String?,
    var receiverId: String?,
    var lastMessage: String?,
    var user: User?,
    var group: Group?,
    var _group: String? // to know if the chat belongs to a group or not
) {

    constructor(): this(null, null, null, null, null, "false")

    fun save() {
        val database = FirebaseConfig.database
        val chatRef = database
            .child("chats")
        chatRef
            .child(this.senderId.toString())
            .child(this.receiverId.toString())
            .setValue(this)
    }

}