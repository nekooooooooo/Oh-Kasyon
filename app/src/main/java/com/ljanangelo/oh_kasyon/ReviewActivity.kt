package com.ljanangelo.oh_kasyon

import android.app.AlertDialog
import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.RatingBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_review.*
import kotlinx.android.synthetic.main.activity_review.button_back
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ReviewActivity : AppCompatActivity(), RatingBar.OnRatingBarChangeListener {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private val key = "retrieved_rid"

    private var bid: String = ""
    private var uid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        val intent = intent
        val rid = intent.getStringExtra(key)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        uid = currentUser?.uid.toString()

        val reservationRef = db.collection("reservations").document(rid)

        reservationRef.get()
            .addOnSuccessListener { data ->
                bid = data.getString("bid").toString()
                val eventName = data.getString("business_name")
                val date: Timestamp? = data.getTimestamp("timestamp")
                val noOfGuests = data.getLong("no_of_guests")
                val dateFormat = "MMMM' 'dd', 'yyyy' at 'HH:mm a' '"
                val convertedDate = if(date == null) "" else SimpleDateFormat(dateFormat).format(date.toDate())
                val businessRef = db.collection("businesses").document(bid)

                businessRef.get()
                    .addOnSuccessListener { data ->
                        val image = data.getString("business_img_url") ?: ""
                        Glide.with(this)
                            .load(Uri.parse(image))
                            .apply(RequestOptions().override(500, 500))
                            .centerCrop()
                            .placeholder(R.drawable.default_picture)
                            .into(event_image)
                    }

                text_event_name.text = eventName
                text_no_of_guests.text = noOfGuests.toString()
                text_date.text = convertedDate
            }

        review_button.setOnClickListener {
            val date_of_review = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Calendar.getInstance().time)
            val review = edit_text_review.text.toString().trim()
            val rating = ratingBar.rating
            toast(bid)
            submitReview(review, rating.toDouble(), date_of_review, rid)
        }

        edit_text_review.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                review_lenght.text = edit_text_review.length().toString() + "/250"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        button_back.setOnClickListener {
            finish()
        }

        ratingBar.onRatingBarChangeListener = this

    }

    private fun submitReview(review: String, rating: Double, date_of_review: String, rid: String) {
        val dialog = ProgressDialog.show(this, "Sending",
            "Loading. Please wait...", true)


        val docData = hashMapOf(
            "review" to review,
            "rating" to rating,
            "uid" to uid,
            "bid" to bid,
            "date_of_review" to getDateFromString(date_of_review)
        )

        db.collection("businesses")
            .document(bid)
            .collection("reviews")
            .document(rid)
            .set(docData)
            .addOnSuccessListener {
                dialog.dismiss()
                db.collection("reservations")
                    .document(rid)
                    .update(mapOf("status" to "Done & Reviewed"))
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            AlertDialog.Builder(this).apply {
                                setTitle("Review has been made!")
                                setPositiveButton("Okay") { _, _ ->
                                    finish()
                                }
                                setOnCancelListener {
                                    finish()
                                }
                            }.create().show()
                        } else {
                            task.exception?.let { e ->
                                toast(e.toString())
                            }

                        }
                    }
                //toast("Added to Firestore Database with ID: ${it.id}!")
            }
            .addOnFailureListener {
                dialog.dismiss()
                Log.w("AppDebug", "Error writing document", it)
            }
    }

    private fun getDateFromString(datetoSaved: String?): Date? {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

        return try {
            format.parse(datetoSaved)
        } catch (e: ParseException) {
            null
        }
    }

    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        text_rating.text = rating.toString()
    }

}