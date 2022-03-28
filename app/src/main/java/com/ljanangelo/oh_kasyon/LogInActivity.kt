package com.ljanangelo.oh_kasyon

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import hideKeyboard
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_user_dashboard.*


class LogInActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 120
    }

    private lateinit var mAuth : FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val TAG = javaClass.simpleName
    private var pressed = false
    private lateinit var googleSignInClient: GoogleSignInClient
    private val email = "ohkasyon.thesis2021@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val fromBanned = intent.getBooleanExtra("fromBanned", false)
        Log.d(TAG, "fromBanned => $fromBanned")

        if(fromBanned) openBannedDialog()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInClient.signOut()

        mAuth = FirebaseAuth.getInstance()

        button_sign_in.setOnClickListener {
            val email = edit_text_email.text.toString().trim()
            val password = edit_text_password.text.toString().trim()

            hideKeyboard()

            if(email.isEmpty()){
                edit_text_email.error = "Email can't be empty!"
                edit_text_email.requestFocus()
                return@setOnClickListener
            }

            if(password.isEmpty()){
                edit_text_password.error = "Password can't be empty!"
                edit_text_password.requestFocus()
                return@setOnClickListener
            }

            loginUser(email, password)

        }

        button_sign_up.setOnClickListener {
            hideKeyboard()
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        button_forgot_password.setOnClickListener {
            onForgotPasswordClicked()
        }

        button_forgot_password_back.setOnClickListener {
            onForgotPasswordClicked()
        }

        button_sign_facebook.setOnClickListener {
            toast("Log in with Facebook")
        }

        button_sign_google.setOnClickListener {
            toast("Log in with Google")
            //signInWithGoogle()
        }

        button_reset_password.setOnClickListener {
            hideKeyboard()
            val dialog = ProgressDialog.show(this, "Sending Password Reset Link",
                    "Loading. Please wait...", true)
            val email = edit_text_email.text.toString().trim()
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->

                        if(task.isSuccessful) {
                            dialog.dismiss()
                            onForgotPasswordClicked()
                            //toast("Password Reset has been sent to $email")
                            showAlertDialog("success", "Password Reset has been sent to $email", 3)
                        } else {
                            dialog.dismiss()
                            when((task.exception as FirebaseAuthException?)!!.errorCode) {
                                //"ERROR_USER_NOT_FOUND" -> toast("User with $email is not found!")
                                "ERROR_USER_NOT_FOUND" -> showAlertDialog("failed", "User with $email is not found!", 3)
                            }
                        }
                    }
        }

        var counter = 10
        ohKasyonSplash.setOnClickListener {
            if(counter > 0) {
                if(counter <= 3) {
                    toast("Click $counter more times for a secret!")
                }
                counter--
            }
            else {
                AlertDialog.Builder(this).apply {
                    setTitle("Programmed By")
                    setMessage(getString(R.string.programmer))
                    setPositiveButton("POGGERS") { _, _ ->

                    }
                    setNegativeButton("POG") { _, _ ->

                    }
                }.create().show()
                counter = 10
            }
        }

    }

    private fun onForgotPasswordClicked() {
        showForgotPasswordForm(pressed)
        pressed = !pressed
    }

    private fun showForgotPasswordForm(clicked: Boolean) {
        hideKeyboard()
        if(!clicked){
            text_forgot_password.visibility = View.VISIBLE
            button_reset_password.visibility = View.VISIBLE
            edit_text_password.visibility = View.GONE
            text_login.visibility = View.GONE
            button_forgot_password.visibility = View.GONE
            button_sign_in.visibility = View.GONE
            or.visibility = View.GONE
            button_sign_google.visibility = View.GONE
            button_sign_facebook.visibility = View.GONE
            question.visibility = View.GONE
            button_sign_up.visibility = View.GONE
        } else {
            text_forgot_password.visibility = View.GONE
            button_reset_password.visibility = View.GONE
            edit_text_password.visibility = View.VISIBLE
            text_login.visibility = View.VISIBLE
            button_forgot_password.visibility = View.VISIBLE
            button_sign_in.visibility = View.VISIBLE
            or.visibility = View.VISIBLE
            button_sign_google.visibility = View.VISIBLE
            button_sign_facebook.visibility = View.VISIBLE
            question.visibility = View.VISIBLE
            button_sign_up.visibility = View.VISIBLE
        }
    }

    private fun loginUser(email: String, password: String) {

        val dialog = ProgressDialog.show(this, "Signing In",
                "Loading. Please wait...", true)

        if(!internetIsConnected()) {
            Handler().postDelayed({
                dialog.dismiss()
                showAlertDialog("failed", "You are not connected to the internet", 3)
            }, 1000)
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    mAuth.currentUser?.let {
                        val uid = it.uid
                        db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if(it.isEmailVerified) {
                                    if (document != null) {
                                        dialog.dismiss()
                                        if(document.get("status").toString() == "Normal" && document.get("type").toString() == "User") {
                                            login()
                                        } else if(document.get("status").toString() == "Deactivated" && document.get("type").toString() == "User") {
                                            openDeactivatedDialog(uid)
                                        } else if(document.get("status").toString() == "Banned") {
                                            openBannedDialog()
                                        } else if(document.get("type").toString() != "User") {
                                            //toast("This account is not a User!")
                                            showAlertDialog("failed", "This account is not a User!", 3)
                                            FirebaseAuth.getInstance().signOut()
                                        }
                                        /*when (document.get("status").toString()) {
                                            "Deactivated" -> openDeactivatedDialog(uid)
                                            "Normal" -> login()
                                        }*/
                                    }
                                } else verify()
                            }
                    }
                } else {
                    task.exception?.message?.let { e ->
                        //toast(e)
                        dialog.dismiss()
                        //showAlertDialog("failed", e, 3)
                        if(task.exception is FirebaseTooManyRequestsException) {
                            showAlertDialog("failed",
                                    "Too many suspicious attempts were detected. Try again later",
                                    3)
                        } else {
                            when ((task.exception as FirebaseAuthException?)!!.errorCode) {
                                "ERROR_WRONG_PASSWORD" -> showAlertDialog("failed", "Password is invalid.", 3)
                                "ERROR_USER_NOT_FOUND" -> showAlertDialog("failed", "User with Email: $email is not found.", 3)
                                "ERROR_INVALID_EMAIL" -> showAlertDialog("failed", "Invalid Email", 3)
                            }
                        }
                    }
                }
            }

    }

    private fun openDeactivatedDialog(uid: String) {
        AlertDialog.Builder(this).apply {
            setTitle("This Account has been Deactivated")
            setMessage("Would you like to reactivate your account?")
            setPositiveButton("Yes") { _, _ ->
                db.collection("users").document(uid)
                    .update(mapOf(
                        "status" to "Normal"
                    ))
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            toast("Account Activated!")
                            login()
                        }
                    }
            }
            setNegativeButton("Cancel") { _, _ ->
                mAuth.signOut()
            }
            setOnDismissListener {
                mAuth.signOut()
            }
        }.create().show()
    }

    private fun openBannedDialog() {

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
                mAuth.signOut()
            }
        }.create().show()
    }

    override fun onStart() {
        super.onStart()
        mAuth.currentUser?.let { user ->

            if(user.isEmailVerified)
                login()
            else
                verify()
        }
    }

    override fun onPause() {
        super.onPause()
        //overridePendingTransition(R.anim.expand_in, android.R.anim.fade_out)
    }

    private fun showAlertDialog(type: String, message: String, timeInSeconds: Long) {
        val timeInMilliSeconds = timeInSeconds * 1000

        text_alert.let { alert ->
            when(type) {
                "failed"  -> {
                    alert.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#f44336"))
                    alert.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_round_error_outline_24, 0, 0, 0)
                }
                "success" -> {
                    alert.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4caf50"))
                    alert.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_success_outline_24, 0, 0, 0)
                }
            }
            alert.visibility = View.VISIBLE
            alert.text = message
            Handler().postDelayed({
                alert.visibility = View.GONE
            }, timeInMilliSeconds)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if(task.isSuccessful) {
                try {
                    // Google Sign In was successful, mAuthenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("LoginActivity", "Google sign in failed", e)
                }
            } else {
                Log.w("LoginActivity", exception.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val dialog = ProgressDialog.show(this, "Signing In",
            "Loading. Please wait...", true)

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                dialog.dismiss()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LoginActivity", "signInWithCredential:success")
                    mAuth.currentUser?.let {
                        val uid = it.uid
                        db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if(it.isEmailVerified) {
                                    if (document != null) {
                                        dialog.dismiss()
                                        if(document.get("status").toString() == "Normal" && document.get("type").toString() == "User") {
                                            login()
                                        } else if(document.get("status").toString() == "Deactivated" && document.get("type").toString() == "User") {
                                            openDeactivatedDialog(uid)
                                        } else if(document.get("type").toString() != "User") {
                                            //toast("This account is not a User!")
                                            showAlertDialog("failed", "This account is not a User!", 3)
                                            FirebaseAuth.getInstance().signOut()
                                        }
                                        /*when (document.get("status").toString()) {
                                            "Deactivated" -> openDeactivatedDialog(uid)
                                            "Normal" -> login()
                                        }*/
                                    }
                                } else {
                                    val intent = Intent(this, CompleteSignUpGoogleActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                }
            }
    }



}