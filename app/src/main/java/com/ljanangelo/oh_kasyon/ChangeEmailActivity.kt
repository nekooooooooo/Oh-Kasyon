package com.ljanangelo.oh_kasyon

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import android.view.View
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_change_email.*
import kotlinx.android.synthetic.main.activity_change_email.button_back
import kotlinx.android.synthetic.main.activity_change_email.button_cancel
import kotlinx.android.synthetic.main.activity_change_email.button_cancel_2
import kotlinx.android.synthetic.main.activity_change_email.button_confirm
import kotlinx.android.synthetic.main.activity_change_email.button_continue
import kotlinx.android.synthetic.main.activity_change_email.change_password
import kotlinx.android.synthetic.main.activity_change_email.edit_text_email
import kotlinx.android.synthetic.main.activity_change_email.edit_text_password
import kotlinx.android.synthetic.main.activity_change_email.layout_reauth
import kotlinx.android.synthetic.main.activity_change_email.text_alert
import java.util.regex.Pattern

class ChangeEmailActivity : AppCompatActivity() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)

        edit_text_email.setText(currentUser?.email)

        button_continue.setOnClickListener {

            /*showAlertDialog("success", "Confirm", 3)
            change_password.visibility = View.VISIBLE
            layout_reauth.visibility = View.GONE*/

            val email = edit_text_email.text.toString()
            var password = edit_text_password.text.toString()

            if (email.isEmpty()) {
                edit_text_email.error = "Email Required!"
                edit_text_email.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                edit_text_password.error = "Password Required!"
                edit_text_password.requestFocus()
                return@setOnClickListener
            }

            val credential = EmailAuthProvider
                .getCredential(email, password)

            val dialog = ProgressDialog.show(
                this, "Authenticating",
                "Loading. Please wait...", true
            )

            currentUser?.reauthenticate(credential)
                ?.addOnCompleteListener { task ->
                    dialog.dismiss()
                    if(task.isSuccessful) {
                        showAlertDialog(
                            "success",
                            "User credentials confirmed. Successfully authenticated!",
                            3
                        )
                        change_password.visibility = View.VISIBLE
                        layout_reauth.visibility = View.GONE
                    } else {
                        task.exception?.let { it
                            showAlertDialog("failed", it.message!!, 3)
                        }
                    }
                }
        }

        button_confirm.setOnClickListener {

            val newEmail = edit_text_new_email.text.toString().trim()

            if (newEmail.isEmpty()) {
                edit_text_email.error = "Email Required!"
                edit_text_email.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                edit_text_email.error = "Valid Email Required!"
                edit_text_email.requestFocus()
                return@setOnClickListener
            }

            val dialog = ProgressDialog.show(
                this, "Changing Email",
                "Loading. Please wait...", true
            )

            currentUser!!.updateEmail(newEmail)
                .addOnCompleteListener { task ->
                    dialog.dismiss()
                    if (task.isSuccessful) {
                        updateFirestoreEmail(newEmail)
                    } else {
                        AlertDialog.Builder(this).apply {
                            setTitle("An Error Has Occurred!")
                            setMessage("Would you like to retry?")
                            setPositiveButton("Yes") { _, _ ->
                                val intent = intent
                                finish()
                                startActivity(intent)
                            }
                            setNegativeButton("No") { _, _ ->
                                returnToMainMenu()
                            }
                        }.create().show()
                    }
                }
        }

        button_back.setOnClickListener {
            returnToMainMenu()
        }

        button_cancel.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Are you sure?")
                setPositiveButton("Yes") { _, _ ->
                    returnToMainMenu()
                }
                setNegativeButton("No") { _, _ ->
                }
            }.create().show()
        }

        button_cancel_2.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Are you sure?")
                setPositiveButton("Yes") { _, _ ->
                    returnToMainMenu()
                }
                setNegativeButton("No") { _, _ ->
                }
            }.create().show()
        }
    }

    private fun showAlertDialog(type: String, message: String, timeInSeconds: Long) {
        val timeInMilliSeconds = timeInSeconds * 1000

        text_alert.let { alert ->
            when(type) {
                "failed" -> {
                    alert.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#f44336"))
                    alert.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_round_error_outline_24,
                        0,
                        0,
                        0
                    )
                }
                "success" -> {
                    alert.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4caf50"))
                    alert.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_baseline_success_outline_24,
                        0,
                        0,
                        0
                    )
                }
            }
            alert.visibility = View.VISIBLE
            alert.text = message
            Handler().postDelayed({
                alert.visibility = View.GONE
            }, timeInMilliSeconds)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        returnToMainMenu()
    }

    private fun returnToMainMenu(){
/*        val intent = Intent(this, MainScreen::class.java)
        startActivity(intent)*/
        finish()
    }

    private fun updateFirestoreEmail(newEmail: String){
        var dialog = ProgressDialog.show(this, "Updating",
            "Loading. Please wait...", true)

        val updates = hashMapOf(
            "email" to newEmail
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
                            val dialog2 = ProgressDialog.show(
                                this, "Email Change Success",
                                "Logging Out and Returning to Login Screen. Please wait...", true
                            )
                            Handler().postDelayed({
                                dialog.dismiss()
                                dialog2.dismiss()
                                FirebaseAuth.getInstance().signOut()
                                logout()
                            }, 1000)
                        } else {
                            toast("Could not be updated!")
                        }
                    }
            }
        }
    }

}