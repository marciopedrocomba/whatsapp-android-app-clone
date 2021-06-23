package com.example.whatsappclone.model

import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.helper.Base64Custom
import kotlinx.serialization.Serializable

@Serializable
class Group(
    var id: String?,
    var name: String?,
    var photo: String?,
    var members: List<User>?
) {
    constructor():
            this(null, null, null, null) {
        val database = FirebaseConfig.database
        val groupRef = database.child("groups")

        val firebaseGroupId = groupRef.push().key
        this.id = firebaseGroupId
    }

    fun save() {
        val database = FirebaseConfig.database
        val groupRef = database.child("groups")
        groupRef.child(id!!)
            .setValue(this)

        // save the group chat
        for (member in this.members!!) {

            val chat = Chat()
            chat.senderId = Base64Custom.encodeBase64(member.email.toString())
            chat.receiverId = this.id
            chat.lastMessage = ""
            chat._group = "true"
            chat.group = this
            chat.save()
        }
    }
}