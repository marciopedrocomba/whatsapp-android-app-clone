package com.example.whatsappclone.model

import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.helper.FirebaseUser
import kotlinx.serialization.Serializable

@Serializable
data class User(
    var id: String?,
    var name: String?,
    var email: String?,
    var password: String?,
    var photo: String?
) {

    constructor(): this(null,null, null, null, null)

    // save the user to the firebase database
    fun save() {

        // database reference
        val db = FirebaseConfig.database

        // users node reference
        val usersRef = db.child("users")

        val user = User()
        user.name = this.name
        user.email = this.email

        // save the user object
        usersRef.child(this.id.toString())
            .setValue(user)

    }

    fun update() {

        val userId = FirebaseUser.getUserId()
        val database = FirebaseConfig.database

        val usersRef = database.child("users")
            .child(userId)

        val usersValue: Map<String, Any> = convertToMap()

        usersRef.updateChildren(usersValue)

    }


    private fun convertToMap(): Map<String, Any> {
        val userMap = HashMap<String, Any>()

        userMap["email"] = this.email.toString()
        userMap["name"] = this.name.toString()
        userMap["photo"] = this.photo.toString()

        return userMap
    }

}