package com.example.whatsappclone.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.R
import com.example.whatsappclone.adapter.ContactsAdapter
import com.example.whatsappclone.adapter.SelectedGroupAdapter
import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.helper.FirebaseUser
import com.example.whatsappclone.helper.RecyclerItemClickListener
import com.example.whatsappclone.model.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GroupActivity : AppCompatActivity() {

    private lateinit var recyclerViewSelectedGroupMembers: RecyclerView
    private lateinit var recyclerViewMembers: RecyclerView
    private lateinit var toolbar: Toolbar

    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var selectedGroupAdapter: SelectedGroupAdapter

    private var membersList = ArrayList<User>()
    private var selectedMembersList = ArrayList<User>()

    private lateinit var usersRef: DatabaseReference
    private val actualUser = FirebaseUser.getActualUser()

    private var valueEventListenerMembers: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        toolbar = findViewById<Toolbar>(R.id.toolbarGroup)
        toolbar.title = "New Group"
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // init views
        recyclerViewSelectedGroupMembers = findViewById(R.id.recyclerViewSelectedGroupMembers)
        recyclerViewMembers = findViewById(R.id.recyclerViewMembers)
        usersRef = FirebaseConfig.database.child("users")

        // config adapters
        contactsAdapter = ContactsAdapter(applicationContext, membersList)

        // config recyclerView for members
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerViewMembers.layoutManager = layoutManager
        recyclerViewMembers.setHasFixedSize(true)
        recyclerViewMembers.adapter = contactsAdapter

        recyclerViewMembers.addOnItemTouchListener(
            RecyclerItemClickListener(
                applicationContext,
                recyclerViewMembers,
                object: RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        val selectedUser = membersList[position]

                        // remove selected user from the members list
                        membersList.remove(selectedUser)
                        contactsAdapter.notifyDataSetChanged()

                        // add the user to the selected user list
                        selectedMembersList.add(selectedUser)
                        selectedGroupAdapter.notifyDataSetChanged()

                        updateMembersToolbar()

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
                        val selectedUser = membersList[position]
                        Toast.makeText(
                            applicationContext,
                            "${selectedUser.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            )
        )

        // config recyclerView for selected members
        selectedGroupAdapter = SelectedGroupAdapter(applicationContext, selectedMembersList)

        val horizontalLayoutManager = LinearLayoutManager(
            applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerViewSelectedGroupMembers.layoutManager = horizontalLayoutManager
        recyclerViewSelectedGroupMembers.setHasFixedSize(true)
        recyclerViewSelectedGroupMembers.adapter = selectedGroupAdapter

        recyclerViewSelectedGroupMembers.addOnItemTouchListener(
            RecyclerItemClickListener(
                applicationContext,
                recyclerViewSelectedGroupMembers,
                object: RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        val selectedUser = selectedMembersList[position]

                        // remove selected user from the members list
                        selectedMembersList.remove(selectedUser)
                        selectedGroupAdapter.notifyDataSetChanged()


                        // add the user to the selected user list
                        membersList.add(selectedUser)
                        contactsAdapter.notifyDataSetChanged()

                        updateMembersToolbar()

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

        val fab = findViewById<FloatingActionButton>(R.id.fabGroup)
        fab.setOnClickListener { view ->

            if (selectedMembersList.size > 0) {
                val intent = Intent(GroupActivity@this, RegisterGroupActivity::class.java)
                val selectedMembersJson = Json.encodeToString(selectedMembersList)
                intent.putExtra(RegisterGroupActivity.SELECTED_MEMBERS, selectedMembersJson)
                startActivity(intent)
            }

        }
    }

    override fun onStart() {
        super.onStart()
        getContacts()
    }

    override fun onStop() {
        super.onStop()
        if (valueEventListenerMembers != null) {
            usersRef.removeEventListener(valueEventListenerMembers!!)
        }
    }

    // get all the contacts(members) from the firebase realtime database
    private fun getContacts() {

        valueEventListenerMembers = usersRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (data in snapshot.children) {

                    val user = data.getValue(User::class.java)
                    if (user != null) {
                        if(actualUser?.email != user.email) {
                            membersList.add(user!!)
                        }

                    }

                }

                contactsAdapter.notifyDataSetChanged()
                updateMembersToolbar()

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    fun updateMembersToolbar() {

        val totalSelectedMembers = selectedMembersList.size
        val totalUsers = membersList.size + totalSelectedMembers

        toolbar.subtitle = "${totalSelectedMembers} of ${totalUsers} selected"

    }

}