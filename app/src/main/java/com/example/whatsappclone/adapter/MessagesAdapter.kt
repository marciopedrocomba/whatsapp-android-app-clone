package com.example.whatsappclone.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.helper.FirebaseUser
import com.example.whatsappclone.model.Message
import de.hdodenhof.circleimageview.CircleImageView

class MessagesAdapter(
    val context: Context,
    private val messageList: List<Message>
): RecyclerView.Adapter<MessagesAdapter.MyViewHolder>() {

    companion object {
        const val  SENT_MESSAGE_TYPE = 0
        const val  RECEIVED_MESSAGE_TYPE = 1
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val message: TextView = view.findViewById(R.id.textViewChatMessageText)
        val name: TextView = view.findViewById(R.id.textViewChatMessageSenderName)
        val senderImage: CircleImageView? = view.findViewById(R.id.imageViewMessageSenderPhoto)
        val image: ImageView = view.findViewById(R.id.imageViewChatMessagePhoto)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val view: View = when (viewType) {
            SENT_MESSAGE_TYPE -> {
                LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.logged_user_message_adapter,
                    parent,
                    false
                )
            }
            else -> {
                LayoutInflater
                    .from(parent.context)
                    .inflate(
                        R.layout.destination_user_message_adapter,
                        parent, false
                    )
            }
        }

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val message = messageList[position]

        val msg = message.message
        val image = message.image
        val senderImage = message.senderImage

        if(holder.senderImage != null && senderImage != null) {
            val senderImageUrl = Uri.parse(senderImage)
            Glide.with(context)
                .load(senderImageUrl)
                .into(holder.senderImage)
            holder.senderImage.visibility = View.VISIBLE
        }

        if(image != null) {
            val url = Uri.parse(image)

            Glide.with(context)
                .load(url)
                .into(holder.image)

            val name = message.name.toString()
            if (name.isNotEmpty()) {
                holder.name.text = name
            }else {
                holder.name.visibility = View.GONE
            }

            // hide message text
            holder.message.visibility = View.GONE
            //holder.message.text = msg
            holder.image.visibility = View.VISIBLE

        }else {

            holder.message.text = msg

            val name = message.name.toString()
            if (name.isNotEmpty()) {
                holder.name.text = name
                if(holder.senderImage != null) holder.senderImage.visibility = View.VISIBLE
            }else {
                holder.name.visibility = View.GONE
                if(holder.senderImage != null) holder.senderImage.visibility = View.GONE
            }

            // hide message image
            holder.image.visibility = View.GONE
            holder.message.visibility = View.VISIBLE

        }


    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        val userId = FirebaseUser.getUserId()

        if (userId == message.userId) {
            return SENT_MESSAGE_TYPE
        }

        return RECEIVED_MESSAGE_TYPE

    }

    override fun getItemCount() = messageList.size

}