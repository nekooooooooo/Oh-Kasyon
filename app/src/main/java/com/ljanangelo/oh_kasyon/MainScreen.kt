package com.ljanangelo.oh_kasyon

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_events_place_info.*
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.android.synthetic.main.activity_main_screen.profile_image
import kotlinx.android.synthetic.main.fragment_user_dashboard.*

class MainScreen : AppCompatActivity() {

    private val DEFAULT_IMAGE_URL = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val uid = currentUser.uid
    private val email = "ohkasyon.thesis2021@gmail.com"

    private lateinit var mAuth : FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        val userRef = db.collection("users").document(currentUser.uid)



        userRef.addSnapshotListener { snapshot, e ->

            if (e != null) {
                toast("Listen Failed: $e")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status").toString()

                if (status == "Banned") {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LogInActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("fromBanned", true)
                    }
                    startActivity(intent)
                }
            }

        }

        //if(!internetIsConnected()) return toast("Internet is not connected!")
        //checkInternetConnection()

        getUserProfilePicture(currentUser)

        val image = intArrayOf(
                R.drawable.ic_round_message_24,
                R.drawable.ic_round_home_24,
                R.drawable.ic_round_person_24
        )

        val adapter = ReservationsActivity.MyViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ConversationsFragment(), "")
        adapter.addFragment(EventsPlaceFragment(), "")
        adapter.addFragment(UserDashboardFragment(), "")
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        for (i in 0 until tabLayout.tabCount) {
            tabLayout.getTabAt(i)?.setIcon(image[i])
        }
        val tabIconColor = ContextCompat.getColor(this@MainScreen, R.color.orange)
        tabLayout.getTabAt(1)?.icon?.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
        viewPager.currentItem = 1

        tabLayout.addOnTabSelectedListener(
                object : TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        super.onTabSelected(tab)
                        val tabIconColor = ContextCompat.getColor(this@MainScreen, R.color.orange)
                        tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {
                        super.onTabUnselected(tab)
                        val tabIconColor = ContextCompat.getColor(this@MainScreen, R.color.white)
                        tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
                    }

                    override fun onTabReselected(tab: TabLayout.Tab) {
                        super.onTabReselected(tab)
                    }
                }
        )

        profile_image.setOnClickListener {
            viewPager.currentItem = 2
        }
    }

    override fun onPause() {
        super.onPause()
        //overridePendingTransition(R.anim.expand_in, android.R.anim.fade_out)
    }


    private fun getUserProfilePicture(currentUser: FirebaseUser?) {
        currentUser?.let { user ->
            if (user.photoUrl == null) {
                val image = Uri.parse(DEFAULT_IMAGE_URL)
                Glide.with(this)
                    .load(image)
                    .placeholder(R.drawable.default_picture)
                    .into(profile_image)
            } else {
                Glide.with(this)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.default_picture)
                    .into(profile_image)
            }
        }
    }

    private fun showBannedDialog() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        AlertDialog.Builder(this).apply {
            setTitle("This Account has been Banned")
            setMessage("If this was a mistake, please contact the admin at ohkasyon.thesis2021@gmail.com")
            setPositiveButton("Copy") { _, _ ->
                val textToCopy = email
                val clip = ClipData.newPlainText("Admin Email", textToCopy)
                clipboard.setPrimaryClip(clip)
                toast("Copied Email Address!")
            }
            setOnDismissListener {
                FirebaseAuth.getInstance().signOut()
                logout()
            }
        }.create().show()
    }

    private fun checkInternetConnection() {
        /*if(!internetIsConnected()) {
            viewPager.visibility = View.GONE
            tabLayout.visibility = View.GONE
            no_internet_refresh.visibility = View.VISIBLE
        } else {
            viewPager.visibility = View.VISIBLE
            tabLayout.visibility = View.VISIBLE
            no_internet_refresh.visibility = View.GONE
        }
        no_internet_refresh.isRefreshing = false*/
        if(!internetIsConnected()) {
            val intent = Intent(this, NoInternetActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        //checkInternetConnection()
        //toast("This is on start")


        if(!internetIsConnected()) {
            toast("Could not connect to server! Signing Out")
            FirebaseAuth.getInstance().signOut()
            logout()
        }
    }

//    override fun onStop() {
//        super.onStop()
//        finish()
//    }

    class MyViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

        private val fragmentList : MutableList<Fragment> = ArrayList()
        private val titleList : MutableList<String> = ArrayList()

        override fun getCount(): Int {
            return fragmentList.size

        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            //titleList.add(title)
        }

        /*override fun getPageTitle(position: Int): CharSequence? {
            return titleList[position]
        }*/
    }

}

