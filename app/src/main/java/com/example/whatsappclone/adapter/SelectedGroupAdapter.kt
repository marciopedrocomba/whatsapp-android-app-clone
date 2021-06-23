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

class SelectedGroupAdapter(
    val context: Context,
    private val selectedContactsList: ArrayList<User>
): RecyclerView.Adapter<SelectedGroupAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val photo: CircleImageView = view.findViewById(R.id.imageViewSelectedMemberPhoto)
        val name: TextView = view.findViewById(R.id.textViewSelectedMemberName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.selected_group_adapter, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = selectedContactsList[position]

        holder.name.text = user.name

        if(user.photo != null) {
            val uri = Uri.parse(user.photo)
            Glide.with(context).load(uri).into(holder.photo)
        }else {
            holder.photo.setImageResource(R.drawable.padrao)
        }
    }

    override fun getItemCount() = selectedContactsList.size


}