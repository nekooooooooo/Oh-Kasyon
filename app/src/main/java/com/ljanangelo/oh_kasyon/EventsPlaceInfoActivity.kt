package com.ljanangelo.oh_kasyon

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.vr.sdk.widgets.pano.VrPanoramaView
import kotlinx.android.synthetic.main.activity_events_place_info.*
import kotlinx.android.synthetic.main.activity_events_place_info.profile_image
import kotlinx.android.synthetic.main.activity_events_place_info.text_address
import kotlinx.android.synthetic.main.activity_events_place_info.text_email

private val currentUser = FirebaseAuth.getInstance().currentUser
private val DEFAULT_IMAGE_URL = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"
private val DEMO_PANORAMA_LINK = "http://reznik.lt/wp-content/uploads/2017/09/preview3000.jpg"

@SuppressLint("StaticFieldLeak")
private val db = FirebaseFirestore.getInstance()

class EventsPlaceInfoActivity : AppCompatActivity() {

    private val key1 = "retrieved_bid"
    private val key2 = "retrieved_rating"

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = intent
        val bid = intent.getStringExtra(key1)
        val rating = intent.getDoubleExtra(key2, 0.0)

        val docRef = db.collection("businesses").document(bid)

        val option = VrPanoramaView.Options().also {
            it.inputType = VrPanoramaView.Options.TYPE_MONO
        }

        var latitude = 0.0
        var longitude = 0.0

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events_place_info)

        getUserProfilePicture(currentUser)

        button_show_location.visibility = View.GONE
        progress_bar.visibility = View.VISIBLE

        docRef.get()
            .addOnSuccessListener { result ->
                button_show_location.visibility = View.VISIBLE
                text_name.text = result.getString("business_name")
                text_address.text = result.getString("business_address")

                /*val rating = when {
                    result.get("business_rating").toString().isEmpty() -> "No Data Found"
                    result.get("business_rating") == null -> "No Data Found"
                    else -> result.get("business_rating").toString()
                }
                text_rating_info.text = rating*/
                text_rating_info.text = rating.toString()

                val tel_no = when {
                    result.get("business_tel_no").toString().isEmpty() -> "No Data Found"
                    result.get("business_tel_no") == null -> "No Data Found"
                    else -> result.get("business_tel_no").toString()
                }
                text_telephone.text = tel_no

                val cell_no = when {
                    result.get("business_mobile_no").toString().isEmpty() -> "No Data Found"
                    result.get("business_mobile_no") == null -> "No Data Found"
                    else -> result.get("business_mobile_no").toString()
                }
                text_mobile.text = cell_no

                val email = when {
                    result.get("business_email").toString().isEmpty() -> "No Data Found"
                    result.get("business_email") == null -> "No Data Found"
                    else -> result.get("business_email").toString()
                }
                text_email.text = email

                ratingBar.rating = rating.toFloat()
                if(result.get("business_geopoint") != null) {
                    val geopoint = result.get("business_geopoint") as GeoPoint
                    latitude = geopoint.latitude
                    longitude = geopoint.longitude
                }

                val imageVrUrl = when {
                    result.getString("business_360_image_url") != null -> result.getString("business_360_image_url")
                    else -> null
                }

                if(imageVrUrl != null) {
                    Glide
                        .with(this)
                        .asBitmap()
                        .load(imageVrUrl)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onLoadCleared(placeholder: Drawable?) {

                            }

                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                progress_bar.visibility = View.GONE
                                vrPanoramaView.loadImageFromBitmap(resource, option)
                            }
                        })
                } else {
                    progress_bar.visibility = View.GONE
                    text_no360.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->

            }

        //text_business_id.text = bid

        button_show_location.setOnClickListener {
            //toast("Google Maps Will Open")
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("LONG", longitude)
            intent.putExtra("LATI", latitude)
            intent.putExtra("NAME", text_name.text.toString())
            startActivity(intent)
        }

        button_back.setOnClickListener {
            /*val intent = Intent(this, MainScreen::class.java)
            startActivity(intent)*/
            finish()
        }

        button_reserve.setOnClickListener {
            val intent = Intent(this, ReserveActivity::class.java)
            intent.putExtra("retrieved_bid", bid)
            startActivity(intent)
        }

        button_menu.setOnClickListener {
            val intent = Intent(this, MenusActivity::class.java)
            intent.putExtra("retrieved_bid", bid)
            startActivity(intent)
        }

        button_reviews.setOnClickListener {
            val intent = Intent(this, ReviewsActivity::class.java)
            intent.putExtra("retrieved_bid", bid)
            startActivity(intent)
        }

        copy_mob_no_icon.setOnClickListener {
            val textToCopy = text_mobile.text.toString()
            val clip = ClipData.newPlainText("Mobile No", textToCopy)
            clipboard.setPrimaryClip(clip)
            toast("Copied Mobile #")
        }

        copy_tel_no_icon.setOnClickListener {
            val textToCopy = text_telephone.text.toString()
            val clip = ClipData.newPlainText("Telephone No", textToCopy)
            clipboard.setPrimaryClip(clip)
            toast("Copied Telephone #")
        }

    }

    override fun onPause() {
        super.onPause()
        //overridePendingTransition(R.anim.expand_in, android.R.anim.fade_out)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        /*val intent = Intent(this, MainScreen::class.java)
        startActivity(intent)*/
        finish()
    }

    private fun getUserProfilePicture(currentUser: FirebaseUser?) {
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
        }
    }
}