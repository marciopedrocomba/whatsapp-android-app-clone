package com.example.whatsappclone.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.helper.FirebaseUser
import com.example.whatsappclone.helper.Permission
import com.example.whatsappclone.model.User
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.lang.Exception

class SettingsActivity : AppCompatActivity() {

    private val permissions: Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    private lateinit var imageButtonCamera: ImageButton
    private lateinit var imageButtonGallery: ImageButton
    private lateinit var circleImageViewProfilePhoto: CircleImageView
    private lateinit var editProfileName: EditText
    private lateinit var imageViewUpdateProfileName: ImageView

    private lateinit var storageRef: StorageReference
    private lateinit var userId: String
    private var loggedUser: User? = null

    // create the take camera photo launcher
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            var image: Bitmap? = null

            try {

                image = result.data?.extras?.get("data") as Bitmap

            }catch (e: Exception) {
                e.printStackTrace()
            }

            if (image != null) {
                circleImageViewProfilePhoto.setImageBitmap(image)

                // save image in firebase
                saveImageFirebase(image!!)
            }

        }
    }

    // create the gallery selection photo launcher
    private val selectPictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {

            var image: Bitmap? = null

            try {

                val selectedImageUri = result.data?.data
                selectedImageUri?.let {
                    image = if (Build.VERSION.SDK_INT < 28) {

                        MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            selectedImageUri
                        )


                    } else {

                        val source =
                            ImageDecoder.createSource(this.contentResolver, selectedImageUri)
                        ImageDecoder.decodeBitmap(source)

                    }

                }

                if (image != null) {
                    circleImageViewProfilePhoto.setImageBitmap(image)

                    // save image in the firebase
                    saveImageFirebase(image!!)
                }


            }catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun saveImageFirebase(image: Bitmap) {

        // image data for firebase
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 70, baos)
        val imageData: ByteArray = baos.toByteArray()

        // image in firebase storage ref
        val imageRef = storageRef
            .child("images")
            .child("profile")
            .child("$userId.jpeg")

        // upload image
        val uploadTask = imageRef.putBytes(imageData)

        uploadTask.addOnFailureListener {

            Toast.makeText(
                applicationContext,
                "Error uploading image",
                Toast.LENGTH_SHORT
            ).show()

        }.addOnSuccessListener {

            Toast.makeText(
                applicationContext,
                "Success uploading image",
                Toast.LENGTH_SHORT
            ).show()

            imageRef.downloadUrl.addOnCompleteListener { task ->
                val url: Uri? = task.getResult()
                if (url != null) {
                    updateUserProfilePhoto(url)
                }
            }

        }

    }

    private fun updateUserProfilePhoto(url: Uri) {
        if(FirebaseUser.updateUserPhoto(url)) {
            loggedUser?.photo = url.toString()
            loggedUser?.update()
            Toast.makeText(
                SettingsActivity@this,
                "Your photo has been updated",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // init firebase storage reference
        storageRef = FirebaseConfig.storage
        userId = FirebaseUser.getUserId()
        loggedUser = FirebaseUser.getLoggedUserData()

        // validate permissions
        Permission.validatePermission(this, permissions, 1)

        val toolbar: Toolbar = findViewById(R.id.mainToolbar)
        toolbar.title = "Settings"
        setSupportActionBar(toolbar)

        // set the back button on the activity
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageButtonCamera = findViewById(R.id.imageButtonCamera)
        imageButtonGallery = findViewById(R.id.imageButtonGallery)
        circleImageViewProfilePhoto = findViewById(R.id.circleImageViewProfilePhoto)
        editProfileName = findViewById(R.id.editProfileName)
        imageViewUpdateProfileName = findViewById(R.id.imageViewUpdateProfileName)

        imageButtonCamera.setOnClickListener {

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if(intent.resolveActivity(packageManager) == null) {
                Toast.makeText(
                    applicationContext,
                    "Cannot open camera device",
                    Toast.LENGTH_SHORT
                ).show()
            }else {
                takePictureLauncher.launch(intent)
            }

        }

        imageButtonGallery.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            if(intent.resolveActivity(packageManager) != null) {
                selectPictureLauncher.launch(intent)
            }

        }

        imageViewUpdateProfileName.setOnClickListener {

            val name = editProfileName.text.toString()
            if (FirebaseUser.updateUserName(name)) {

                loggedUser?.name = name
                loggedUser?.update()

                Toast.makeText(
                    SettingsActivity@this,
                "Name updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        // get user data
        val user = FirebaseUser.getActualUser()
        val url = user?.photoUrl

        if (url == null) {
            circleImageViewProfilePhoto.setImageResource(R.drawable.padrao)
        }else {
            Glide.with(SettingsActivity@this)
                .load(url)
                .into(circleImageViewProfilePhoto)
        }

        editProfileName.setText(user?.displayName)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for (resultPermission in grantResults) {
            if (resultPermission == PackageManager.PERMISSION_DENIED) {
                alertPermissionValidation()
            }
        }

    }

    private fun alertPermissionValidation() {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Denied permissions")
        builder.setMessage("To use this app you need to accept all permissions")
        builder.setCancelable(false)
        builder.setPositiveButton("Confirm"
        ) { dialog, which ->
            finish()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}