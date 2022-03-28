package com.ljanangelo.oh_kasyon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_verify.*

class VerifyActivity : AppCompatActivity() {

    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)



        currentUser?.let { user ->

        }


        currentUser?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        toast("Verification email sent to ${currentUser.email}")
                    } else {
                        toast("There was an error when sending the email!")
                    }
                }

        button_log_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            logout()
        }

        button_verify.setOnClickListener {
            currentUser?.let { user ->
                user.reload()
                if(user.isEmailVerified){
                    toast("Email has been verified. Welcome!")
                    login()
                } else {
                    toast("Error: Email has not been verified yet, please check your inbox!")
                }
            }
        }

        text_view_resend.setOnClickListener {
            currentUser?.let { user ->
                user.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            toast("Verification email sent to ${currentUser.email}")
                        } else {
                            toast("There was an error when sending the email!")
                        }
                    }
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}