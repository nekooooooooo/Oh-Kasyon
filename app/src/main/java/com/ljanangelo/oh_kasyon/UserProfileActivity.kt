package com.ljanangelo.oh_kasyon

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.MenuInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.ByteArrayOutputStream


class UserProfileActivity : AppCompatActivity() {

    private val openSettingsAnim: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_settings_anim) }
    private val closeSettingsAnim: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_settings_close_anim) }


    private val DEFAULT_IMAGE_URL = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"

    private lateinit var imageUri: Uri
    private val GALLERY_REQUEST_CODE = 1234
    private val CAMERA_REQUEST = 1233

    private val TAG = "AppDebug"

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        button_save.visibility = View.GONE
        picture_progress_bar.visibility = View.GONE

        loadProfile()

        button_back.setOnClickListener {
            /*val intent = Intent(this, MainScreen::class.java)
            startActivity(intent)*/
            finish()
        }

        button_settings.setOnClickListener { v ->
            button_settings.startAnimation(openSettingsAnim)
            showPopup(v)
        }

        button_save.setOnClickListener {
            val drawable = profile_image.drawable as BitmapDrawable
            val imageBitmap = drawable.bitmap

            uploadImage(imageBitmap)

            it.visibility = View.GONE
        }

        pullToRefreshUser.setOnRefreshListener {
            loadProfile()
        }
    }

    override fun onPause() {
        super.onPause()
        //overridePendingTransition(R.anim.expand_in, android.R.anim.fade_out)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        /*val intent = Intent(this, MainScreen::class.java)*//*.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }*//*
        startActivity(intent)*/
        finish()
    }

    private fun showPopup(v: View){
        val popup = PopupMenu(this, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.settings_menu, popup.menu)
        popup.gravity = Gravity.END
        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {
                R.id.edit -> {
                    openProfileEdit()
                    finish()
                }
                R.id.change_picture -> {
                    getCameraIntent()
                }
            }
            true
        }
        popup.setOnDismissListener {
            button_settings.startAnimation(closeSettingsAnim)
        }
        popup.show()
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
                        button_save.visibility = View.VISIBLE
                    }
                }
            }

        }

    }

    private fun setImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .into(profile_image)
    }

    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(500, 500)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(this)
    }

    private fun getCameraIntent() {
        /*val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        startActivityForResult(intent, CAMERA_REQUEST)*/
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

    private fun uploadImage(bitmap: Bitmap?) {
        val baos = ByteArrayOutputStream()
        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("pics/${FirebaseAuth.getInstance().currentUser?.uid}")
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        val upload = storageRef.putBytes(image)

        picture_progress_bar.visibility = View.VISIBLE

        upload.addOnCompleteListener { uploadTask ->
            if(uploadTask.isSuccessful){
                storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                    urlTask.result?.let {
                        imageUri = it

                        val updates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(imageUri)
                            .build()

                        currentUser?.updateProfile(updates)
                            ?.addOnCompleteListener {
                                if(it.isSuccessful) {
                                    picture_progress_bar.visibility = View.GONE
                                    toast("Image Uploaded!")

                                    val db = FirebaseFirestore.getInstance()
                                    val photoUrl = currentUser.photoUrl
                                    val userId = currentUser.uid

                                    db.collection("users").document(userId)
                                        .update("photoUrl", photoUrl.toString())
                                        .addOnSuccessListener {
                                            // add alert here
                                        }
                                        .addOnFailureListener { e -> toast(e.toString()) }

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

    private fun loadProfile(){

        progressbar.visibility = View.VISIBLE
        hideText()

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

            var fullName = ""
            var firstName : String
            var middleName : String
            var lastName : String
            var email = user.email.toString().trim()
            var username = ""
            var gender = ""
            var address = ""
            var contact = ""

            val uid = user.uid

            val docRef = db.collection("users").document(uid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        progressbar.visibility = View.GONE
                        showText()
                        firstName = document.getString("first_name").toString()
                        lastName = document.getString("last_name").toString()
                        middleName = document.getString("middle_name").toString()
                        fullName = when(middleName.isBlank()) {
                            true -> "$firstName $lastName"
                            false -> "$firstName $middleName $lastName"
                        }
                        username = user.displayName.toString()
                        gender = document.getString("gender").toString()
                        address = document.getString("address").toString()
                        contact = document.getString("contact_no").toString()
                    } else {
                        toast("User info not found!")
                    }

                    text_full_name.text = fullName
                    text_username.text = username
                    text_email.text = email
                    text_gender.text = gender
                    text_address.text = address
                    text_contact.text = contact

                }

            pullToRefreshUser.isRefreshing = false

        }
    }

    private fun showText() {
        text_full_name.visibility = View.VISIBLE
        text_username.visibility = View.VISIBLE
        text_email.visibility = View.VISIBLE
        text_gender.visibility = View.VISIBLE
        text_address.visibility = View.VISIBLE
        text_contact.visibility = View.VISIBLE
    }

    private fun hideText() {
        text_full_name.visibility = View.INVISIBLE
        text_username.visibility = View.INVISIBLE
        text_email.visibility = View.INVISIBLE
        text_gender.visibility = View.INVISIBLE
        text_address.visibility = View.INVISIBLE
        text_contact.visibility = View.INVISIBLE
    }
}
