package com.ljanangelo.oh_kasyon

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import hideKeyboard
import kotlinx.android.synthetic.main.activity_complete_sign_up_google.*
import kotlinx.android.synthetic.main.activity_complete_sign_up_google.button_sign_up
import kotlinx.android.synthetic.main.activity_complete_sign_up_google.edit_text_email
import kotlinx.android.synthetic.main.activity_complete_sign_up_google.edit_text_password
import kotlinx.android.synthetic.main.activity_complete_sign_up_google.text_alert
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern


class CompleteSignUpGoogleActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth
    private val TAG : String = "ERROR"

    private lateinit var imageUri: Uri
    private val GALLERY_REQUEST_CODE = 1234

    private lateinit var dialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_sign_up_google)

        mAuth = FirebaseAuth.getInstance()

        showAlertDialog("success", "Google Sign In complete. Please finish the set up process", 2)

        // Set gender drop down items
        val adapter = ArrayAdapter.createFromResource(this,
            R.array.gender, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_gender.adapter = adapter

        // Check if email is valid
        edit_text_email.afterTextChanged {
            val email = edit_text_email.text.toString().trim()
            if (!Patterns.EMAIL_ADDRESS.matcher(email). matches())
                edit_text_email.error = "Valid Email Required"
        }

        // Check if contact # is valid
        edit_text_contact.afterTextChanged {
            val contact = edit_text_contact.text.toString().trim()
            if (contact.length != 10)
                edit_text_contact.error = "Invalid phone number!"
        }

        // Check if password is valid
        edit_text_password.afterTextChanged {
            val password = edit_text_password.text.toString().trim()
            isValidPassword(password)
        }

        add_profile_photo.setOnClickListener {
            getCameraIntent()
        }

        add_profile_photo_text.setOnClickListener {
            getCameraIntent()
        }


        button_sign_up.setOnClickListener {

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
            val email = edit_text_email.text.toString().trim()
            val password = edit_text_password.text.toString().trim()
            val confirmPassword = edit_text_confirm_password.text.toString().trim()

            hideKeyboard()
            mainScrollView.fullScroll(ScrollView.FOCUS_UP)

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

            if (email.isEmpty()) {
                edit_text_email.error = "Email Required!"
                edit_text_email.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edit_text_email.error = "Valid Email Required!"
                edit_text_email.requestFocus()
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                edit_text_password.error = "Invalid Password!"
                edit_text_password.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                edit_text_confirm_password.error = "Passwords do not match!"
                edit_text_confirm_password.text = null
                edit_text_confirm_password.requestFocus()
                return@setOnClickListener
            }

            registerUser(
                username,
                firstName,
                middleName,
                lastName,
                address,
                gender,
                contact,
                email,
                password
            )

        }


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

    private fun uploadImage(bitmap: Bitmap?) {

        val baos = ByteArrayOutputStream()
        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("pics/${FirebaseAuth.getInstance().currentUser?.uid}-${FirebaseAuth.getInstance().currentUser?.displayName}")
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        val upload = storageRef.putBytes(image)

        val user: FirebaseUser? = mAuth.currentUser

        val dialog1 = ProgressDialog.show(this, "Uploading Image",
            "Loading. Please wait...", true)

        upload.addOnCompleteListener { uploadTask ->
            if(uploadTask.isSuccessful){
                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                    urlTask.result?.let {

                        imageUri = it

                        val updates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(imageUri)
                            .build()

                        val intent = Intent(this, LogInActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }

                        user?.updateProfile(updates)
                            ?.addOnCompleteListener {
                                dialog1.dismiss()
                                if(it.isSuccessful) {
                                    //toast("Registered!")
                                    showAlertDialog("failed", "Registered", 3)

                                    FirebaseAuth.getInstance().signOut()

                                    Handler().postDelayed({
                                        startActivity(intent)
                                    }, 1000)

                                    /*AlertDialog.Builder(this).apply {
                                        setTitle("Verification Required")
                                        setMessage("A verification link will be sent to your email address. Follow the instructions to verify your account!")
                                        setPositiveButton("Okay") { _, _ ->
                                            user.sendEmailVerification()
                                            // Return to Log In screen after successful registration
                                            startActivity(intent)
                                        }
                                        setOnDismissListener {
                                            user.sendEmailVerification()
                                            // Return to Log In screen after successful registration
                                            startActivity(intent)
                                        }
                                    }.create().show()*/
                                } else {
                                    //toast("Could not be registered! Returning to log in screen!")
                                    showAlertDialog("failed", "Could not be registered! Returning to log in screen!", 3)
                                    Handler().postDelayed({
                                        startActivity(intent)
                                    }, 1000)
                                }
                            }

                    }
                }
            } else {
                uploadTask.exception?.let {
                    toast(it.message!!)
                }
            }
        }
    }


    // Register User
    private fun registerUser(
        username: String,
        firstName: String,
        middleName: String,
        lastName: String,
        address: String,
        gender: String,
        contact: String,
        email: String,
        password: String,
    ) {

        dialog = ProgressDialog.show(this, "Signing Up",
            "Loading. Please wait...", true)

        if(!internetIsConnected()) {
            Handler().postDelayed({
                dialog.dismiss()
                showAlertDialog("failed", "You are not connected to the internet", 3)
            }, 1000)
            return
        }

        val drawable = add_profile_photo.drawable as BitmapDrawable
        val imageBitmap = drawable.bitmap


        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                dialog.dismiss()
                if(task.isSuccessful){

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username).build()


                    // Get user id from current user
                    val user: FirebaseUser? = mAuth.currentUser
                    val userId:String = user!!.uid
                    //val userPhotoUrl: Uri? = user.photoUrl


                    user.updateProfile(profileUpdates)
                        .addOnCompleteListener(this) { task ->
                            if(task.isSuccessful) {
                                Log.d(TAG, "User profile updated.");
                            }
                        }

                    uploadImage(imageBitmap)

                    // Initialize firestore database
                    val db = FirebaseFirestore.getInstance()

                    // Get Data to be placed on the database document
                    val docData = hashMapOf(
                        "uid" to userId,
                        "username" to username,
                        "first_name" to firstName,
                        "middle_name" to middleName,
                        "last_name" to lastName,
                        "address" to address,
                        "gender" to gender,
                        "contact_no" to contact,
                        "email" to email,
                        "status" to "Normal",
                        "type" to "User",
                        "google_profile" to "Done"
                    )



                    // Put data into "users" collection with the document id of userId
                    db.collection("users").document(userId)
                        .set(docData)
                        .addOnSuccessListener {
                            toast("Added to Firestore Database!")
                        }
                        .addOnFailureListener {
                            Log.w(TAG, "Error writing document", it)
                        }

                } else {
                    when ((task.exception as FirebaseAuthException?)!!.errorCode) {
                        "ERROR_EMAIL_ALREADY_IN_USE" -> {
                            showAlertDialog("failed", "Email is already in use!", 3)
                        }
                        else -> task.exception?.message?.let { error -> toast(error) }
                    }

                    /*task.exception?.message?.let { error ->
                        toast(error)
                    }*/
                }
            }
    }

    // Password Validity Checker
    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) {
            edit_text_password.error = "Minimum of 8 characters required!"
            return false
        }

        if(!Pattern.compile(".*[a-z].*").matcher(password).matches()){
            edit_text_password.error = "Password must contain a letter"
            return false
        }

        if(!Pattern.compile(".*[0-9].*").matcher(password).matches()){
            edit_text_password.error = "Password must contain a number"
            return false
        }

        if (password.isEmpty()) {
            edit_text_password.error = "Password can't be empty!"
            return false
        }

        return true
    }

    // If user is already logged in, go to home screen
    override fun onStart() {
        super.onStart()
        mAuth.currentUser?.let {

        }
    }

    override fun onPause() {
        super.onPause()
        //overridePendingTransition(R.anim.expand_in, android.R.anim.fade_out)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){

            GALLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }
                } else {
                    Log.e(TAG, "Could not select image from memory: ")
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result.uri?.let { uri ->
                        setImage(uri)
                    }
                }
            }

        }

    }

    private fun setImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .into(add_profile_photo)
    }

    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(500, 500)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(this)
    }

    private fun getCameraIntent() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(500, 500)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(this)
    }

    private fun getPictureIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        intent.type = "image/*"

        val mimeType = arrayOf("image/jpeg", "image/png", "image/jpg")

        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }


    private fun startDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Upload with")
            .setItems(R.array.upload_option) { dialog, which ->
                when(which){
                    0 -> {
                        getCameraIntent()
                    }
                    1 -> {
                        getPictureIntent()
                    }
                }
            }
        builder.create()
        builder.show()
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


}

