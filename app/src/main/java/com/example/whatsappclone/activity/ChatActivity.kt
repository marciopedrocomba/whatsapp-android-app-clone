package com.example.whatsappclone.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.adapter.MessagesAdapter
import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.helper.Base64Custom
import com.example.whatsappclone.helper.FirebaseUser
import com.example.whatsappclone.model.Chat
import com.example.whatsappclone.model.Group
import com.example.whatsappclone.model.Message
import com.example.whatsappclone.model.User
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {

    companion object {
        const val CHAT_CONTACT = "chat_contact"
        const val CHAT_GROUP = "chat_group"
    }

    private lateinit var circleImageViewPhoto: CircleImageView
    private lateinit var textViewName: TextView
    private lateinit var editMessage: EditText
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var imageButtonSendMessagePhoto: ImageButton

    private lateinit var database: DatabaseReference
    private lateinit var messagesRef: DatabaseReference
    private var childEventListenerMessages: ChildEventListener? = null
    private lateinit var storageRef: StorageReference

    private var selectedUser: User? = null
    private var group: Group? = null
    private var loggedUser = FirebaseUser.getLoggedUserData()

    // users config(users that will talk to each other)
    private var loggedUserId: String? = FirebaseUser.getUserId()
    private var selectedUserId: String? = null

    private val messageList = ArrayList<Message>()
    private lateinit var messageAdapter: MessagesAdapter

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {

                var image: Bitmap? = null

                try {

                    image = result?.data?.extras?.get("data") as Bitmap

                }catch (e: Exception) {
                    e.printStackTrace()
                }

                if (image != null) {

                    val baos = ByteArrayOutputStream()
                    image.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                    val imageData: ByteArray = baos.toByteArray()

                    // create unique image name
                    val imageName = UUID.randomUUID()

                    val imageRef = storageRef
                        .child("images")
                        .child("photos")
                        .child("$loggedUserId")
                        .child("$imageName.jpeg")

                    // upload image
                    val uploadTask = imageRef.putBytes(imageData)

                    uploadTask.addOnFailureListener {

                        Toast.makeText(
                            applicationContext,
                            "Error sending image",
                            Toast.LENGTH_SHORT
                        ).show()

                    }.addOnSuccessListener {

                        Toast.makeText(
                            applicationContext,
                            "Success sending image",
                            Toast.LENGTH_SHORT
                        ).show()

                        imageRef.downloadUrl.addOnCompleteListener { task ->
                            val url: Uri? = task.result
                            if (url != null) {

                                if (selectedUser != null) {

                                    val message = Message()
                                    message.userId = loggedUserId
                                    message.message = "image.jpeg"
                                    message.image = url.toString()

                                    // save for sender
                                    saveMessage(loggedUserId!!, selectedUserId!!, message)

                                    // save for destination user
                                    saveMessage(selectedUserId!!, loggedUserId!!, message)

                                }else {

                                    for (member in group?.members!!) {

                                        val groupSenderId = Base64Custom.encodeBase64(member.email.toString())
                                        val loggedUserGroupId = FirebaseUser.getUserId()

                                        val message = Message()
                                        message.userId = loggedUserGroupId
                                        message.name = loggedUser.name
                                        message.message = "image.jpeg"
                                        message.senderImage = loggedUser.photo
                                        message.image = url.toString()

                                        // save chat
                                        saveMessage(groupSenderId, selectedUserId.toString(), message)

                                        saveChat(groupSenderId, selectedUserId.toString(), selectedUser, message, true)

                                    }

                                }

                                Toast.makeText(
                                    ChatActivity@this,
                                    "Image sent successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }

                    }

                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // init firebase storage reference
        storageRef = FirebaseConfig.storage

        // initial configurations
        circleImageViewPhoto = findViewById(R.id.circleChatHeadPhoto)
        textViewName = findViewById(R.id.textViewChatHeadTitle)
        editMessage = findViewById(R.id.editTextChatMessage)
        recyclerViewMessages = findViewById(R.id.recyclerViewChatMessages)
        imageButtonSendMessagePhoto = findViewById(R.id.imageButtonSendMessagePhoto)

        // get the selected chat user data
        val bundle: Bundle = intent?.extras!!
        if (bundle != null) {

            if (bundle.containsKey(CHAT_GROUP)) {

                /*Group chat start*/
                val groupDataJson = bundle.getString(CHAT_GROUP)
                if (groupDataJson != null) {
                    group = Json.decodeFromString(groupDataJson) as Group
                    selectedUserId = group?.id

                    textViewName.text = group?.name.toString()

                    val photo = group?.photo
                    if(photo == null) {

                        circleImageViewPhoto.setImageResource(R.drawable.padrao)

                    }else {

                        val uri = Uri.parse(photo)
                        Glide.with(this)
                            .load(uri)
                            .into(circleImageViewPhoto)
                    }
                }

                /*Group chat end*/

            }else {
                val dataJson = bundle.getString(CHAT_CONTACT)

                if (dataJson != null) {

                    /*Normal Chat start*/
                    selectedUser = Json.decodeFromString(dataJson) as User

                    textViewName.text = selectedUser?.name.toString()

                    val photo = selectedUser?.photo
                    if(photo == null) {

                        circleImageViewPhoto.setImageResource(R.drawable.padrao)

                    }else {

                        val uri = Uri.parse(photo)
                        Glide.with(this)
                            .load(uri)
                            .into(circleImageViewPhoto)

                    }

                    // get the selected user
                    selectedUserId = Base64Custom.encodeBase64(selectedUser?.email.toString())
                    /*Normal Chat end*/

                }
            }
        }

        // config message recycler
        configRecyclerViewMessages()

        // messages
        database = FirebaseConfig.database
        messagesRef = database.child("messages")
            .child(loggedUserId!!)
            .child(selectedUserId!!)

        imageButtonSendMessagePhoto.setOnClickListener {

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

    }

    override fun onStart() {
        super.onStart()
        getMessages()
    }

    override fun onStop() {
        super.onStop()
        if (childEventListenerMessages != null) {
            messagesRef.removeEventListener(childEventListenerMessages!!)
        }
    }

    private fun configRecyclerViewMessages() {

        // config adapter
        messageAdapter = MessagesAdapter(this, messageList)

        // config recyclerView
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerViewMessages.layoutManager = layoutManager
        recyclerViewMessages.setHasFixedSize(true)
        recyclerViewMessages.adapter = messageAdapter

    }

    fun sendMessage(view: View) {

        val messageText = editMessage.text.toString()

        if (messageText.isEmpty()) {

            Toast.makeText(
                ChatActivity@this,
                "Please type a message...",
                Toast.LENGTH_SHORT
            ).show()

        }else {

            if (selectedUser != null) {
                // 1 to 1 message
                val message = Message()
                message.userId = loggedUserId
                message.message = messageText

                // save the message and chat to user
                saveMessage(loggedUserId!!, selectedUserId!!, message)
                saveChat(loggedUserId!!, selectedUserId!!, selectedUser!!, message)

                // save message to selected user
                saveMessage(selectedUserId!!, loggedUserId!!, message)
                saveChat(selectedUserId!!, loggedUserId!!, loggedUser!!, message)


            }else {

                // group message

                for (member in group?.members!!) {

                    val groupSenderId = Base64Custom.encodeBase64(member.email.toString())
                    val loggedUserGroupId = FirebaseUser.getUserId()

                    val message = Message()
                    message.userId = loggedUserGroupId
                    message.name = loggedUser.name
                    message.message = messageText
                    message.senderImage = loggedUser.photo

                    // save chat
                    saveMessage(groupSenderId, selectedUserId.toString(), message)

                    saveChat(groupSenderId, selectedUserId.toString(), selectedUser, message, true)

                }

            }

        }


    }

    private fun saveChat(
        userId: String,
        selectedId: String,
        userReceiver: User?,
        message: Message,
        isGroup:
        Boolean = false
    ) {

        val chatSender = Chat()
        chatSender.senderId = userId
        chatSender.receiverId = selectedId
        chatSender.lastMessage = message.message

        if (isGroup) {

            chatSender._group = "true"
            chatSender.group = group

        }else {

            chatSender.user = userReceiver

//            val chatReceiver = Chat()
//            chatReceiver.senderId = selectedUserId
//            chatReceiver.receiverId = loggedUserId
//            chatReceiver.lastMessage = message.message
//            chatReceiver.user = loggedUser
//
//            chatReceiver.save()

        }

        chatSender.save()

    }

    private fun saveMessage(userId: String, selectedId: String, message: Message) {

        val database = FirebaseConfig.database
        val messageRef = database.child("messages")

        messageRef
            .child(userId)
            .child(selectedId)
            .push()
            .setValue(message)

        // clear text
        editMessage.setText("")

    }

    private fun getMessages() {

        messageList.clear()

        childEventListenerMessages = messagesRef.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    messageList.add(message)
                    messageAdapter.notifyDataSetChanged()
                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

}