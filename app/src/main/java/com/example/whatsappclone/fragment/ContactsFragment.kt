package com.example.whatsappclone.fragment

import android.content.Intent
import android.os.Bundle
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
import com.example.whatsappclone.activity.GroupActivity
import com.example.whatsappclone.adapter.ChatAdapter
import com.example.whatsappclone.adapter.ContactsAdapter
import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.helper.FirebaseUser
import com.example.whatsappclone.helper.RecyclerItemClickListener
import com.example.whatsappclone.model.Chat
import com.example.whatsappclone.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class ContactsFragment : Fragment() {

    private lateinit var recyclerViewContacts: RecyclerView
    private lateinit var contactsAdapter: ContactsAdapter
    private val contactsList = ArrayList<User>()

    private lateinit var usersRef: DatabaseReference
    private val actualUser = FirebaseUser.getActualUser()


    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_contacts, container, false)

        // initial configurations
        recyclerViewContacts = view.findViewById(R.id.recyclerViewContacts)
        usersRef = FirebaseConfig.database.child("users")

        // config adapter
        contactsAdapter = ContactsAdapter(requireActivity(), contactsList)

        // config recyclerView
        val layoutManager = LinearLayoutManager(activity)
        recyclerViewContacts.layoutManager = layoutManager
        recyclerViewContacts.setHasFixedSize(true)
        recyclerViewContacts.adapter = contactsAdapter

        // config click event on a recycler view
        recyclerViewContacts.addOnItemTouchListener(
            RecyclerItemClickListener(
                activity,
                recyclerViewContacts,
                object: RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        val updatedContactsList = contactsAdapter.getContacts()
                        val selectedUser = updatedContactsList[position]
                        val header: Boolean = selectedUser.email?.isEmpty() == true

                        if (header) {
                            val intent = Intent(activity, GroupActivity::class.java)
                            startActivity(intent)
                        }else {
                            val selectUserDataJson = Json.encodeToString(selectedUser)

                            val intent = Intent(activity, ChatActivity::class.java)
                            intent.putExtra(ChatActivity.CHAT_CONTACT, selectUserDataJson)

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
                        val user = contactsList[position]
                        Toast.makeText(
                            activity,
                            "${user.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            )
        )

        return view
    }


    override fun onStart() {
        super.onStart()
        getContacts()
    }

    override fun onStop() {
        super.onStop()
        if (valueEventListener != null) {
            usersRef.removeEventListener(valueEventListener!!)
        }
    }

    // get all the contacts from the firebase realtime database
    private fun getContacts() {

        valueEventListener = usersRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                clearContactsLis()

                for (data in snapshot.children) {

                    val user = data.getValue(User::class.java)
                    if (user != null) {
                        if(actualUser?.email != user.email) {
                            contactsList.add(user!!)
                        }

                    }

                }

                contactsAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    fun clearContactsLis() {
        contactsList.clear()
        addMenuNewGroup()
    }

    fun addMenuNewGroup() {

        /*
            Define an empty email for the user
            case the user e-mail is empty then this will
            be used as the contacts headers(the create group item)
         */

        val itemGroup = User()
        itemGroup.name = "New Group"
        itemGroup.email = ""

        contactsList.add(itemGroup)
    }

    fun searchContacts(text: String) {

        val searchText = text.lowercase()

        val contactsSearchList = ArrayList<User>()

        for (user in contactsList) {

            if (user != null) {

                val name = user.name.toString().lowercase()
                val email = user.email.toString().lowercase()

                if(name.contains(searchText) || email.contains(searchText)) {
                    contactsSearchList.add(user)
                }

            }

        }

        contactsAdapter = ContactsAdapter(requireActivity(), contactsSearchList)
        recyclerViewContacts.adapter = contactsAdapter
        contactsAdapter.notifyDataSetChanged()


    }

    fun reloadContacts() {
        contactsAdapter = ContactsAdapter(requireActivity(), contactsList)
        recyclerViewContacts.adapter = contactsAdapter
        contactsAdapter.notifyDataSetChanged()
    }

}