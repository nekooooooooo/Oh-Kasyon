package com.ljanangelo.oh_kasyon

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_change_password.edit_text_confirm_password
import kotlinx.android.synthetic.main.activity_change_password.edit_text_email
import kotlinx.android.synthetic.main.activity_change_password.edit_text_password
import kotlinx.android.synthetic.main.activity_change_password.text_alert
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.regex.Pattern

class ChangePasswordActivity : AppCompatActivity() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

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

            val newPassword = edit_text_new_password.text.toString().trim()
            val confirmPassword = edit_text_confirm_password.text.toString().trim()

            if (!isValidPassword(newPassword)) {
                edit_text_password.error = "Invalid Password!"
                edit_text_password.requestFocus()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                edit_text_confirm_password.error = "Passwords do not match!"
                edit_text_confirm_password.text = null
                edit_text_confirm_password.requestFocus()
                return@setOnClickListener
            }

            val dialog = ProgressDialog.show(
                this, "Changing Password",
                "Loading. Please wait...", true
            )

            currentUser!!.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    dialog.dismiss()
                    if (task.isSuccessful) {
                        val dialog2 = ProgressDialog.show(
                            this, "Password Change Success",
                            "Returning to Main Menu. Please wait...", true
                        )
                        Handler().postDelayed({
                            dialog2.dismiss()
                            returnToMainMenu()
                        }, 1000)
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

    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) {
            edit_text_new_password.error = "Minimum of 8 characters required!"
            return false
        }

        if(!Pattern.compile(".*[a-z].*").matcher(password).matches()){
            edit_text_new_password.error = "Password must contain a letter"
            return false
        }

        if(!Pattern.compile(".*[0-9].*").matcher(password).matches()){
            edit_text_new_password.error = "Password must contain a number"
            return false
        }

        if (password.isEmpty()) {
            edit_text_new_password.error = "Password can't be empty!"
            return false
        }

        return true
    }
}