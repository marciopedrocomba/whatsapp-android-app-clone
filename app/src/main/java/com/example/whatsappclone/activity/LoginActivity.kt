package com.example.whatsappclone.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.whatsappclone.R
import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.model.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    // views
    private lateinit var editEmail: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var buttonLogin: Button

    //firebase
    private val auth: FirebaseAuth = FirebaseConfig.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // set up views
        editEmail = findViewById(R.id.editLoginEmail)
        editPassword = findViewById(R.id.editLoginPassword)
        buttonLogin = findViewById(R.id.button_login)

        // add the login button click listener
        buttonLogin.setOnClickListener {

            // get the values from the fields
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            // check if user filled all the fields
            if(validateUserFields(email, password)) {

                // gets here if all the fields are filled

                // create user instance
                val user = User()
                user.email = email
                user.password = password

                // log the user in
                logUserIn(user)

            }


        }

    }

    override fun onStart() {
        super.onStart()
        isUserLogged()
    }

    // validate if the user is already logged
    private fun isUserLogged() {
        if(auth.currentUser != null) {
            openMainActivity()
        }
    }

    private fun logUserIn(user: User) {

        auth.signInWithEmailAndPassword(
            user.email.toString(),
            user.password.toString()
        ).addOnCompleteListener {

            // check if the user account was created successfully
            if( it.isSuccessful ) {

                // get here if user was registered Successfully
                openMainActivity()
                finish()

            }else {

                // get here if there was any error registering the user account

                var exception: String

                try {
                    throw it.exception!!
                }catch (e: FirebaseAuthInvalidUserException) {
                    exception = "User e-mail or password wrong"
                }catch (e: FirebaseAuthInvalidCredentialsException) {
                    exception = "User e-mail or password wrong"
                }catch (e: Exception) {
                    exception = "Error registering user: ${e.message}"
                    e.printStackTrace()
                }

                Toast.makeText(
                    RegisterActivity@this,
                    exception,
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

    }

    fun openRegisterScreen(view: View) {
        val intent = Intent(LoginActivity@this, RegisterActivity::class.java)
        startActivity(intent)
    }

    fun openMainActivity() {
        val intent = Intent(LoginActivity@this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun validateUserFields(
        email: String,
        password: String
    ): Boolean {

        // check if user filled all the fields
        if(email.isEmpty()) {

            Toast.makeText(
                LoginActivity@this,
                "Please fill the e-mail field",
                Toast.LENGTH_SHORT
            ).show()

            return false

        }else if(password.isEmpty()) {

            Toast.makeText(
                LoginActivity@this,
                "Please fill the password field",
                Toast.LENGTH_SHORT
            ).show()

            return false

        }

        return true

    }

}