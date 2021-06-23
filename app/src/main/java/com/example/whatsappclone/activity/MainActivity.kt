package com.example.whatsappclone.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.example.whatsappclone.R
import com.example.whatsappclone.config.FirebaseConfig
import com.example.whatsappclone.fragment.ChatFragment
import com.example.whatsappclone.fragment.ContactsFragment
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems


class MainActivity : AppCompatActivity() {

    private val auth = FirebaseConfig.auth
    private lateinit var materialSearchView: MaterialSearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // config the app toolbar
        val toolbar: Toolbar = findViewById(R.id.mainToolbar)
        toolbar.title = "WhatsApp"
        setSupportActionBar(toolbar)

        // Tabs configuration
        val adapter = FragmentPagerItemAdapter(
            supportFragmentManager, FragmentPagerItems.with(this)
                .add("Chats", ChatFragment::class.java)
                .add("Contacts", ContactsFragment::class.java)
                .create()
        )

        val viewPager = findViewById<View>(R.id.viewPager) as ViewPager
        viewPager.adapter = adapter

        val viewPagerTab = findViewById<View>(R.id.viewPagerTab) as SmartTabLayout
        viewPagerTab.setViewPager(viewPager)

        // search view configuration
        materialSearchView = findViewById(R.id.materialSearchView)

        // search view listener
        materialSearchView.setOnSearchViewListener(object: MaterialSearchView.SearchViewListener {
            override fun onSearchViewShown() {}

            override fun onSearchViewClosed() {
                val chatsFragment = adapter.getPage(0) as ChatFragment
                chatsFragment.reloadChats()
            }

        })

        // EditText listener
        materialSearchView.setOnQueryTextListener(object: MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { return false }

            override fun onQueryTextChange(newText: String?): Boolean {

                when(viewPager.currentItem) {
                    0 -> {
                        val chatsFragment = adapter.getPage(0) as ChatFragment

                        if (newText?.isNotEmpty() == true) {
                            chatsFragment.searchChats(newText)
                        }else {
                            chatsFragment.reloadChats()
                        }
                    }

                    1 -> {

                        val contactsFragment = adapter.getPage(1) as ContactsFragment

                        if (newText?.isNotEmpty() == true) {
                            contactsFragment.searchContacts(newText)
                        }else {
                            contactsFragment.reloadContacts()
                        }

                    }

                }

                return true
            }

        })


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        // config search button
        val item: MenuItem? = menu?.findItem(R.id.menu_search)
        materialSearchView.setMenuItem(item)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

//            R.id.menu_search -> {
//
//            }

            R.id.menu_settings -> {
                openSettingsScreen()
            }

            R.id.menu_logout -> {
                logout()
                finish()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    // log the user out of the system
    private fun logout() {
        try {
            auth.signOut()
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openSettingsScreen() {
        val intent = Intent(MainActivity@this, SettingsActivity::class.java)
        startActivity(intent)
    }

}