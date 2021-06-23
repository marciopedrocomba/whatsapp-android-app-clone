package com.example.whatsappclone.helper

import android.net.Uri
import android.util.Log
import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import java.lang.Exception

class FirebaseUser {

    companion object {

        fun getUserId(): String {
            val user = FirebaseConfig.auth
            val email: String? = user.currentUser?.email
            return Base64Custom.encodeBase64(email.toString())
        }

        fun getActualUser(): FirebaseUser? {
            val user = FirebaseConfig.auth
            return user.currentUser
        }

        fun updateUserPhoto(url: Uri): Boolean {

            try {

                val user = getActualUser()

                val profile = UserProfileChangeRequest.Builder()
                    .setPhotoUri(url)
                    .build()

                user?.updateProfile(profile)?.addOnCompleteListener {

                    if ( !it.isSuccessful ) {
                        Log.d("PROFILE", "Error updating profile image")
                    }

                }

                return true

            }catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        }

        fun updateUserName(name: String): Boolean {

            try {

                val user = getActualUser()

                val profile = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                user?.updateProfile(profile)?.addOnCompleteListener {

                    if ( !it.isSuccessful ) {
                        Log.d("PROFILE", "Error updating profile name")
                    }

                }

                return true

            }catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        }

        fun getLoggedUserData(): User {

            val firebaseUser = getActualUser()

            val user = User()
            user.name = firebaseUser?.displayName
            user.email = firebaseUser?.email

            if (firebaseUser?.photoUrl == null) {
                user.photo = ""
            }else {
                user.photo = firebaseUser?.photoUrl.toString()
            }

            return user

        }

    }

}