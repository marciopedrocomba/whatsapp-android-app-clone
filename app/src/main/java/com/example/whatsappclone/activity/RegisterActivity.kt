package com.example.whatsappclone.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.whatsappclone.R
import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.helper.Base64Custom
import com.example.whatsappclone.helper.FirebaseUser
import com.example.whatsappclone.model.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    // views
    private lateinit var editName: TextInputEditText
    private lateinit var editEmail: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var buttonRegister: Button

    //firebase
    private val auth: FirebaseAuth = FirebaseConfig.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // set up views
        editName = findViewById(R.id.editRegisterName)
        editEmail = findViewById(R.id.editRegisterEmail)
        editPassword = findViewById(R.id.editRegisterPassword)
        buttonRegister = findViewById(R.id.button_register)

        buttonRegister.setOnClickListener {

            // get the values from the fields
            val name = editName.text.toString()
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            // check if user filled all the fields
            if(validateUserFields(name, email, password)) {

                // gets here if all the fields are filled

                // create user instance
                val user = User()
                user.name = name
                user.email = email
                user.password = password

                // register user
                registerUser(user)

            }

        }


    }

    private fun registerUser(user: User) {

        auth.createUserWithEmailAndPassword(
            user.email.toString(),
            user.password.toString()
        ).addOnCompleteListener {

            // check if the user account was created successfully
            if( it.isSuccessful ) {

                try {

                    // encode the user email(id)
                    val userId = Base64Custom.encodeBase64(user.email.toString())
                    user.id = userId

                    // save the data to the firebase database
                    user.save()

                }catch (e: Exception) {
                    e.printStackTrace()
                }


                FirebaseUser.updateUserName(user.name.toString())
                // get here if user was registered Successfully
                finish()

            }else {

                // get here if there was any error registering the user account

                var exception: String

                try {
                    throw it.exception!!
                }catch (e: FirebaseAuthWeakPasswordException) {
                    exception = "Enter a more strong password"
                }catch (e: FirebaseAuthInvalidCredentialsException) {
                    exception = "Please, enter a valid e-mail"
                }catch (e: FirebaseAuthUserCollisionException) {
                    exception = "This e-mail is already registered"
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

    private fun validateUserFields(
        name: String,
        email: String,
        password: String
    ): Boolean {

        // check if user filled all the fields
        if(name.isEmpty()) {

            Toast.makeText(
                RegisterActivity@this,
                "Please fill the name field",
                Toast.LENGTH_SHORT
            ).show()

            return false

        }else if(email.isEmpty()) {

            Toast.makeText(
                RegisterActivity@this,
                "Please fill the e-mail field",
                Toast.LENGTH_SHORT
            ).show()

            return false

        }else if(password.isEmpty()) {

            Toast.makeText(
                RegisterActivity@this,
                "Please fill the password field",
                Toast.LENGTH_SHORT
            ).show()

            return false

        }

        return true

    }

}