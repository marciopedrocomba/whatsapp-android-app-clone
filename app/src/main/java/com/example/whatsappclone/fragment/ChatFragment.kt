package com.example.whatsappclone.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.activity.ChatActivity
import com.example.whatsappclone.adapter.ChatAdapter
import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.helper.Base64Custom
import com.example.whatsappclone.helper.FirebaseUser
import com.example.whatsappclone.helper.RecyclerItemClickListener
import com.example.whatsappclone.model.Chat
import com.example.whatsappclone.model.User
import com.google.firebase.database.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.collections.ArrayList

class ChatFragment : Fragment() {

    private lateinit var recyclerViewChats: RecyclerView
    private lateinit var chatsAdapter: ChatAdapter
    private var chatsList = ArrayList<Chat>()

    private lateinit var chatsRef: DatabaseReference
    private val actualUser = FirebaseUser.getActualUser()
    private lateinit var actualUserId: String

    private var childEventListener: ChildEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerViewChats = view.findViewById(R.id.recyclerViewChats)
        configRecyclerView()

        actualUserId = Base64Custom.encodeBase64(actualUser?.email.toString())

        chatsRef = FirebaseConfig.database
            .child("chats")
            .child("$actualUserId")

        return view

    }

    override fun onStart() {
        super.onStart()
        getChats()
    }

    override fun onStop() {
        super.onStop()
        if(childEventListener != null) {
            chatsRef.removeEventListener(childEventListener!!)
        }
    }

    private fun configRecyclerView() {

        chatsAdapter = ChatAdapter(requireActivity(), chatsList)

        val layoutManager = LinearLayoutManager(activity)
        recyclerViewChats.layoutManager = layoutManager
        recyclerViewChats.setHasFixedSize(true)
        recyclerViewChats.adapter = chatsAdapter

        // config click event on a recycler view
        recyclerViewChats.addOnItemTouchListener(
            RecyclerItemClickListener(
                activity,
                recyclerViewChats,
                object: RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {

                        val updatedChatsList = chatsAdapter.getChats()
                        val selectedChat = updatedChatsList[position]

                        if (selectedChat._group == "true") {

                            val selectedGroupDataJson = Json.encodeToString(selectedChat.group)

                            val intent = Intent(activity, ChatActivity::class.java)
                            intent.putExtra(ChatActivity.CHAT_GROUP, selectedGroupDataJson)

                            startActivity(intent)

                        }else {
                            val selectedUser = selectedChat.user

                            val selectedUserDataJson = Json.encodeToString(selectedUser)

                            val intent = Intent(activity, ChatActivity::class.java)
                            intent.putExtra(ChatActivity.CHAT_CONTACT, selectedUserDataJson)

                            startActivity(intent)
                        }

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
                        val chat = chatsList[position]
                        var name: String = ""

                        name = when (chat._group) {
                            "true" -> {
                                "${chat.group?.name}"
                            }
                            else -> {
                                "${chat.user?.name}"
                            }
                        }

                        Toast.makeText(
                            activity,
                            name,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            )
        )

    }

    fun searchChats(text: String) {

        val searchText = text.lowercase()

        val chatsSearchList = ArrayList<Chat>()

        for (chat in chatsList) {

            if (chat.user != null) {

                val name = chat.user?.name.toString().lowercase()
                val lastMessage = chat.lastMessage.toString().lowercase()

                if(name.contains(searchText) || lastMessage.contains(searchText)) {
                    chatsSearchList.add(chat)
                }

            }else {

                val name = chat.group?.name.toString().lowercase()
                val lastMessage = chat.lastMessage.toString().lowercase()

                if(name.contains(searchText) || lastMessage.contains(searchText)) {
                    chatsSearchList.add(chat)
                }

            }

        }

        chatsAdapter = ChatAdapter(requireActivity(), chatsSearchList)
        recyclerViewChats.adapter = chatsAdapter
        chatsAdapter.notifyDataSetChanged()


    }

    fun reloadChats() {
        chatsAdapter = ChatAdapter(requireActivity(), chatsList)
        recyclerViewChats.adapter = chatsAdapter
        chatsAdapter.notifyDataSetChanged()
    }

    private fun getChats() {

        chatsList.clear()

        childEventListener = chatsRef.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val chat = snapshot.getValue(Chat::class.java)

                if(chat != null) {
                    chatsList.add(chat)
                }

                chatsAdapter.notifyDataSetChanged()
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
                Log.e("APP_ERROR", error.message)
            }

        })

    }

}