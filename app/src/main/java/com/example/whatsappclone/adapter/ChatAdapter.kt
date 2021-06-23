package com.example.whatsappclone.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.model.Chat
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(
    val context: Context,
    private val chatsList: List<Chat>
): RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val photo: CircleImageView = view.findViewById(R.id.imageViewChatPhoto)
        val name: TextView = view.findViewById(R.id.textViewChatName)
        val lastMessage: TextView = view.findViewById(R.id.textViewChatLastMessage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.chats_adapter, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val chat = chatsList[position]

        if (chat._group == "true") {

            val group = chat.group
            holder.name.text = group?.name
            holder.lastMessage.text = chat.lastMessage

            val photo = group?.photo

            if(photo == null) {
                holder.photo.setImageResource(R.drawable.padrao)
            }else {
                val url = Uri.parse(photo)
                Glide
                    .with(context)
                    .load(url)
                    .into(holder.photo)
            }

        }else {

            holder.name.text = chat.user?.name
            holder.lastMessage.text = chat.lastMessage

            val photo = chat.user?.photo

            if(photo == null) {
                holder.photo.setImageResource(R.drawable.padrao)
            }else {
                val url = Uri.parse(photo)
                Glide
                    .with(context)
                    .load(url)
                    .into(holder.photo)
            }

        }

    }

    fun getChats(): List<Chat> = chatsList

    override fun getItemCount() = chatsList.size

}