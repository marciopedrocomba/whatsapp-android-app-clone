package com.example.whatsappclone.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.adapter.SelectedGroupAdapter
import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.helper.FirebaseUser
import com.example.whatsappclone.helper.RecyclerItemClickListener
import com.example.whatsappclone.model.Group
import com.example.whatsappclone.model.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.lang.Exception

class RegisterGroupActivity : AppCompatActivity() {

    companion object {
        const val SELECTED_MEMBERS = "members"
    }

    private var selectedMembersList = ArrayList<User>()

    private lateinit var textTotalParticipants: TextView
    private lateinit var recyclerViewMembers: RecyclerView
    private lateinit var groupImage: CircleImageView
    private lateinit var editTextGroupName: TextView

    private lateinit var selectedGroupAdapter: SelectedGroupAdapter

    private lateinit var storageRef: StorageReference
    private lateinit var group: Group

    private val imageGroupActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult())
    { result ->

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
                    groupImage.setImageBitmap(image)

                    // save image in the firebase
                    saveImageFirebase(image!!)
                }


            }catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_group)

        val toolbar: Toolbar = findViewById(R.id.toolbarRegisterGroup)
        toolbar.title = "New Group"
        toolbar.subtitle = "Choose a name"
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        storageRef = FirebaseConfig.storage
        textTotalParticipants = findViewById(R.id.textViewTotalCreateGroupParticipants)
        recyclerViewMembers = findViewById(R.id.recyclerViewGroupMembers)
        groupImage = findViewById(R.id.imageViewCreateGroupPhoto)
        editTextGroupName = findViewById(R.id.editTextCreateGroupName)
        group = Group()

        val selectedMembersJson = intent?.extras?.getString(SELECTED_MEMBERS)

        if (selectedMembersJson != null) {
            val members = Json
                .decodeFromString(selectedMembersJson!!) as ArrayList<User>
            selectedMembersList.addAll(members)
            textTotalParticipants.text = "Participants: ${selectedMembersList.size}"
        }

        // click event on the group image icon
        groupImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            if (intent.resolveActivity(packageManager) != null) {
                imageGroupActivityLauncher.launch(intent)
            }

        }

        // config recycler view
        selectedGroupAdapter = SelectedGroupAdapter(applicationContext, selectedMembersList)

        val horizontalLayoutManager = LinearLayoutManager(
            applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerViewMembers.layoutManager = horizontalLayoutManager
        recyclerViewMembers.setHasFixedSize(true)
        recyclerViewMembers.adapter = selectedGroupAdapter

        recyclerViewMembers.addOnItemTouchListener(
            RecyclerItemClickListener(
                applicationContext,
                recyclerViewMembers,
                object: RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {

                    }

                    override fun onItemClick(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        TODO("Not yet implemented")
                    }

                    override fun onLongItemClick(view: View?, position: Int) {
                        val selectedUser = selectedMembersList[position]
                        Toast.makeText(
                            applicationContext,
                            "${selectedUser.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            )
        )

        val fab: FloatingActionButton = findViewById(R.id.fabCreateGroup)
        fab.setOnClickListener {
            val groupName: String = editTextGroupName.text.toString()

            if (groupName.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Please enter the group name...",
                    Toast.LENGTH_SHORT
                ).show()
            }else {

                selectedMembersList.add(FirebaseUser.getLoggedUserData())
                group.members = selectedMembersList
                group.name = groupName
                group.save()

                val selectedGroupDataJson = Json.encodeToString(group)

                val intent = Intent(RegisterGroupActivity@this, ChatActivity::class.java)
                intent.putExtra(ChatActivity.CHAT_GROUP, selectedGroupDataJson)

                startActivity(intent)

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
            .child("groups")
            .child("${group.id}.jpeg")

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
                val url: Uri? = task.result
                if (url != null) {
                    group.photo = url.toString()
                }
            }

        }

    }

}