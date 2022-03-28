package com.ljanangelo.oh_kasyon

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {

    private val DEFAULT_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/oh-kasyon.appspot.com/o/pics%2Fdefault-profile-picture1.jpg?alt=media&token=c923dbf2-e004-4ae4-a5f9-5e73ae90348b"

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        pullToRefresh.setOnRefreshListener {
            test.text = null
            getBusiness()
            pullToRefresh.isRefreshing = false
        }

        currentUser?.let { user ->
            if(user.photoUrl == null){
                val image = Uri.parse(DEFAULT_IMAGE_URL)
                Glide.with(this)
                        .load(image)
                        .into(profile_image)
            } else {
                Glide.with(this)
                        .load(user.photoUrl)
                        .into(profile_image)
            }

        }

        getBusiness()

        button_log_out.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Are you sure?")
                setPositiveButton("Yes") { _, _ ->

                    FirebaseAuth.getInstance().signOut()
                    logout()

                }
                setNegativeButton("Cancel") { _, _ ->
                }
            }.create().show()
        }

        button_test.setOnClickListener {
            val intent = Intent(this, MainScreen::class.java)
            startActivity(intent)
        }

    }

    private fun getBusiness() {
        var username : String

        val uid = currentUser?.uid

        progressbar.visibility = View.VISIBLE

        val docRef = db.collection("users").document(uid.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                progressbar.visibility = View.GONE
                if (document != null) {
                    username = document.getString("username").toString()
                }
            }

        db.collection("businesses")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    test.text = test.text.toString() + "${document.getString("business_name")}" + "\n"
                }
            }
            .addOnFailureListener { exception ->

            }
    }

}