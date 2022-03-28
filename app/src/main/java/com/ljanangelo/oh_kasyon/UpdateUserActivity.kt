package com.ljanangelo.oh_kasyon

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import hideKeyboard
import kotlinx.android.synthetic.main.activity_update_user.*
import kotlinx.android.synthetic.main.activity_update_user.contact_prefix
import kotlinx.android.synthetic.main.activity_update_user.edit_gender_specify
import kotlinx.android.synthetic.main.activity_update_user.edit_text_address
import kotlinx.android.synthetic.main.activity_update_user.edit_text_contact
import kotlinx.android.synthetic.main.activity_update_user.edit_text_first_name
import kotlinx.android.synthetic.main.activity_update_user.edit_text_last_name
import kotlinx.android.synthetic.main.activity_update_user.edit_text_middle_name
import kotlinx.android.synthetic.main.activity_update_user.edit_text_username
import kotlinx.android.synthetic.main.activity_update_user.gender_divider
import kotlinx.android.synthetic.main.activity_update_user.spinner_gender
import kotlin.collections.hashMapOf as hashMapOf

class UpdateUserActivity : AppCompatActivity() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user)

        picture_progress_bar.visibility = View.GONE

        val adapter = ArrayAdapter.createFromResource(this,
            R.array.gender, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_gender.adapter = adapter

        loadCurrentProfile()

        button_save.setOnClickListener {
            val username = edit_text_username.text.toString().trim()
            val firstName = edit_text_first_name.text.toString().trim()
            val middleName = edit_text_middle_name.text.toString().trim()
            val lastName = edit_text_last_name.text.toString().trim()
            val address = edit_text_address.text.toString().trim()
            val gender = when (val selectedItem = spinner_gender.selectedItem.toString()) {
                "Other" -> "Other: " + edit_gender_specify.text.toString().trim()
                else -> selectedItem
            }
            val prefix = contact_prefix.text.toString().trim()
            val suffix = edit_text_contact.text.toString().trim()
            val contact = prefix + suffix

            hideKeyboard()

            if (username.isEmpty()) {
                edit_text_username.error = "Username Required!"
                edit_text_username.requestFocus()
                return@setOnClickListener
            }

            if (firstName.isEmpty()) {
                edit_text_first_name.error = "First Name Required!"
                edit_text_first_name.requestFocus()
                return@setOnClickListener
            }

            if (lastName.isEmpty()) {
                edit_text_last_name.error = "Last Name Required!"
                edit_text_last_name.requestFocus()
                return@setOnClickListener
            }

            if(gender.isEmpty()) {
                edit_gender_specify.error = "Specific Gender Required!"
                edit_gender_specify.requestFocus()
                return@setOnClickListener
            }

            if (address.isEmpty()) {
                edit_text_address.error = "Address Required!"
                edit_text_address.requestFocus()
                return@setOnClickListener
            }

            if (suffix.isEmpty()){
                edit_text_contact.error = "Contact # Required!"
                edit_text_contact.requestFocus()
                return@setOnClickListener
            }

            updateProfile(username,
                firstName,
                middleName,
                lastName,
                address,
                gender,
                contact)

        }

        button_back.setOnClickListener {
            returnToProfile()
        }

        /*val original_username = edit_text_username.text.toString().trim()
        edit_text_username.setOnFocusChangeListener { view, hasFocus ->
            if(hasFocus) {
                edit_text_username.text.clear()
            } else {
                edit_text_username.setText(original_username)
            }
        }*/

        // Show edit text depending on the selected gender
        spinner_gender?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if(selectedItem == "Other") {
                    val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    gender_divider.visibility = View.VISIBLE
                    edit_gender_specify.visibility = View.VISIBLE
                    spinner_gender.setBackgroundResource(R.drawable.contact_prefix_background)
                    spinner_gender.layoutParams = layoutParams
                }
                else {
                    val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    gender_divider.visibility = View.GONE
                    edit_gender_specify.visibility = View.GONE
                    spinner_gender.setBackgroundResource(R.drawable.edit_text_background)
                    spinner_gender.layoutParams = layoutParams
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        returnToProfile()
    }

    override fun onPause() {
        super.onPause()
        //overridePendingTransition(R.anim.expand_in, android.R.anim.fade_out)
    }


    private fun updateProfile(username: String, firstName: String, middleName: String, lastName: String, address: String, gender: String, contact: String) {
        var dialog = ProgressDialog.show(this, "Updating",
            "Loading. Please wait...", true)

        val updates = hashMapOf(
            "username" to username,
            "first_name" to firstName,
            "middle_name" to middleName,
            "last_name" to lastName,
            "address" to address,
            "gender" to gender,
            "contact_no" to contact,
        )

        if(!internetIsConnected()) {
            Handler().postDelayed({
                dialog.dismiss()
                toast("You are not connected to the internet!")
            }, 1000)
            return
        }

        currentUser?.let { user ->
            user.uid.let {
                db.collection("users").document(it)
                    .update(updates as Map<String, Any>)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful){


                        } else {
                            toast("Could not be updated!")
                        }
                    }
            }

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username).build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful) {
                        dialog.dismiss()
                        returnToProfile()
                        Log.d("AppDebug", "User profile updated.");
                    }
                }
        }

    }

    private fun loadCurrentProfile() {

        currentUser?.let { user ->
            val uid = user.uid
            var username = ""
            var firstName = ""
            var middleName = ""
            var lastName = ""
            var gender = ""
            var address = ""
            var contact = ""
            var new_contact = ""

            val docRef = db.collection("users").document(uid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        firstName = document.getString("first_name").toString()
                        lastName = document.getString("last_name").toString()
                        middleName = document.getString("middle_name").toString()
                        username = user.displayName.toString()
                        gender = document.getString("gender").toString()
                        address = document.getString("address").toString()
                        contact = document.getString("contact_no").toString()
                        new_contact = contact.substring(3)
                    } else {
                        toast("User info not found!")
                    }

                    edit_text_username.setText(username)
                    edit_text_first_name.setText(firstName)
                    edit_text_middle_name.setText(middleName)
                    edit_text_last_name.setText(lastName)
                    edit_text_address.setText(address)
                    edit_text_contact.setText(new_contact)

                }

        }

    }

    private fun returnToProfile(){
        val intent = Intent(this, UserProfileActivity::class.java)/*.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }*/
        startActivity(intent)
        finish()
    }

}
