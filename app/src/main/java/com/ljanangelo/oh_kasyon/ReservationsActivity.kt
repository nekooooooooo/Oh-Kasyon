package com.ljanangelo.oh_kasyon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_reservations.*
import kotlinx.android.synthetic.main.activity_reservations.tabLayout
import kotlinx.android.synthetic.main.activity_reservations.viewPager

class ReservationsActivity : AppCompatActivity() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservations)

        val adapter = MyViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(OnGoingFragment(), "On-Going")
        adapter.addFragment(HistoryFragment(), "History")
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        button_back.setOnClickListener {
            /*val intent = Intent(this, MainScreen::class.java)
            startActivity(intent)*/
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        /*val intent = Intent(this, MainScreen::class.java)
        startActivity(intent)*/
        finish()
    }

    class MyViewPagerAdapter (manager: FragmentManager) : FragmentPagerAdapter(manager) {

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
            titleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titleList[position]
        }
    }


}