package com.example.whatsappclone.config

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirebaseConfig {
    companion object {
        val database: DatabaseReference = Firebase.database.reference
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val storage: StorageReference = FirebaseStorage.getInstance().reference
    }
}