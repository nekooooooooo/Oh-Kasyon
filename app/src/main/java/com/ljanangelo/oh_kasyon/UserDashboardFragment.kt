package com.ljanangelo.oh_kasyon

import android.app.AlertDialog
import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_user_dashboard.*


class UserDashboardFragment : Fragment() {

    private val DEFAULT_IMAGE_URL = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"

    private lateinit var imageUri: Uri
    private val TAG = "UserDashboardFragment"
    private val GALLERY_REQUEST_CODE = 1234

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private lateinit var listenerRegistration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        text_fullName.text = "No Name Found"

        loadUserInfo()

        var fullName: String = "No Name Found"
        var firstName : String
        var middleName : String
        var lastName : String

        var textFullName = text_fullName as TextView

        val uid = currentUser.uid

        val userRef = db.collection("users").document(uid)
        /*userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        firstName = document.getString("first_name").toString()
                        lastName = document.getString("last_name").toString()
                        middleName = document.getString("middle_name").toString()
                        fullName = "$firstName $lastName"
                    } else {
                        fullName = "No Name Found"
                    }

                    textFullName.text = fullName
                }*/

        userRef.addSnapshotListener { snapshot, e ->

            if (e != null) {
                context?.toast("Listen Failed: $e")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                firstName = if(!snapshot.getString("first_name").isNullOrEmpty()) snapshot.getString("first_name").toString() else ""
                lastName = if(!snapshot.getString("last_name").isNullOrEmpty()) snapshot.getString("last_name").toString() else ""
                middleName = if(!snapshot.getString("middle_name").isNullOrEmpty()) snapshot.getString("middle_name").toString() else ""
                fullName = "$firstName $lastName"
            } else {
                fullName = "No Name Found"
            }

            textFullName.text = fullName
        }

        // Log.d(TAG, "${listenerRegistration::class.simpleName}")

        button_profile.setOnClickListener {
            context?.openUserProfile()
        }

        button_change_password.setOnClickListener {
            context?.openChangePassword()
        }

        button_change_email.setOnClickListener {
            context?.openChangeEmail()
        }

        button_deactivate_account.setOnClickListener {
            val uid = currentUser?.uid.toString()

            AlertDialog.Builder(context).apply {
                setTitle("Deactivate Account")
                setMessage("Deactivating your account means your account will temporarily be removed from the system")
                setPositiveButton("Confirm") { _, _ ->
                    AlertDialog.Builder(activity).apply {
                        setTitle("Are you sure?")
                        setPositiveButton("Yes") { _, _ ->
                            db.collection("users").document(uid)
                                .update(mapOf("status" to "Deactivated"))
                                .addOnCompleteListener { task ->
                                    if(task.isSuccessful) {
                                        context?.toast("Account Deactivated!")
                                        FirebaseAuth.getInstance().signOut()
                                        context?.logout()
                                    }
                                }
                        }
                        setNegativeButton("Cancel") { _, _ ->

                        }
                    }.create().show()
                }
                setNegativeButton("Cancel") { _, _ ->

                }
            }.create().show()

        }

        button_reservations.setOnClickListener {
            context?.openReservations()
        }

        button_logout.setOnClickListener {
            AlertDialog.Builder(activity).apply {
                setTitle("Are you sure?")
                setPositiveButton("Yes") { _, _ ->

                    val dialog = ProgressDialog.show(
                        context, "Logging Out",
                        "Loading. Please wait...", true
                    )

                    Handler().postDelayed({
                        dialog.dismiss()
                        FirebaseAuth.getInstance().signOut()
                        context?.logout()
                    }, 1000)


                }

                setNegativeButton("Cancel") { _, _ ->

                }
            }.create().show()
        }
    }

    private fun loadUserInfo() {
        currentUser?.let { user ->

            if(user.photoUrl == null){
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

            text_email.text = user.email

            if(user.isEmailVerified){
                text_verify.visibility = View.INVISIBLE
            } else {
                text_verify.visibility = View.VISIBLE
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
    }

}