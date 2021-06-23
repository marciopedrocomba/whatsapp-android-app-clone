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
import com.example.whatsappclone.model.User
import de.hdodenhof.circleimageview.CircleImageView

class ContactsAdapter(
    val context: Context,
    private val contactsList: List<User>
): RecyclerView.Adapter<ContactsAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val contactPhoto: CircleImageView = view.findViewById(R.id.imageViewContactPhoto)
        val contactName: TextView = view.findViewById(R.id.textViewContactName)
        val contactEmail: TextView = view.findViewById(R.id.textViewContactEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.contacts_adapter, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = contactsList[position]
        val header: Boolean = user.email?.isEmpty() == true

        holder.contactName.text = user.name
        holder.contactEmail.text = user.email

        if(user.photo != null) {
            val uri = Uri.parse(user.photo)
            Glide.with(context).load(uri).into(holder.contactPhoto)
        }else {
            if (header) {
                holder.contactPhoto.setImageResource(R.drawable.icone_grupo)
                holder.contactEmail.visibility = View.GONE
            }else {
                holder.contactPhoto.setImageResource(R.drawable.padrao)
            }

        }

    }

    fun getContacts(): List<User> = this.contactsList

    override fun getItemCount() = contactsList.size

}