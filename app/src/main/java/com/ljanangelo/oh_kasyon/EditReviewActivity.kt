package com.ljanangelo.oh_kasyon

import android.app.AlertDialog
import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.RatingBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_edit_review.*
import kotlinx.android.synthetic.main.activity_edit_review.button_back
import kotlinx.android.synthetic.main.activity_edit_review.edit_text_review
import kotlinx.android.synthetic.main.activity_edit_review.event_image
import kotlinx.android.synthetic.main.activity_edit_review.ratingBar
import kotlinx.android.synthetic.main.activity_edit_review.review_lenght
import kotlinx.android.synthetic.main.activity_edit_review.text_date
import kotlinx.android.synthetic.main.activity_edit_review.text_event_name
import kotlinx.android.synthetic.main.activity_edit_review.text_no_of_guests
import kotlinx.android.synthetic.main.activity_edit_review.text_rating
import kotlinx.android.synthetic.main.activity_review.*
import java.text.SimpleDateFormat

class EditReviewActivity : AppCompatActivity(), RatingBar.OnRatingBarChangeListener {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private val keyRid = "retrieved_rid"
    private val keyBid = "retrieved_bid"

    private var bid: String = ""
    private var rid: String = ""

    private var uid: String = ""

    private val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {

        val intent = intent
        rid = intent.getStringExtra(keyRid)
        bid = intent.getStringExtra(keyBid)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_review)

        Log.d(TAG, "BID: $bid RID: $rid")

        uid = currentUser?.uid.toString()

        val reviewRef = db.collection("businesses").document(bid).collection("reviews").document(rid)
        val reservationRef = db.collection("reservations").document(rid)

        loadReservation(reservationRef)
        loadReview(reviewRef)

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

        save_button.setOnClickListener {
            val review = edit_text_review.text.toString().trim()
            val rating = ratingBar.rating

            updateReview(reviewRef, review, rating)
        }


    }

    private fun updateReview(reviewRef: DocumentReference, review: String, rating: Float) {
        val loading = ProgressDialog.show(
            this, "Logging Out",
            "Loading. Please wait...", true
        )

        reviewRef
            .update(mapOf(
                "review" to review,
                "rating" to rating.toDouble()
            ))
            .addOnSuccessListener {
                loading.dismiss()
                Log.d(TAG, "DocumentSnapshot successfully updated!")
                AlertDialog.Builder(this).apply {
                    setTitle("Review Updated Succesfully!")
                    setPositiveButton("Okay") { _, _ ->
                        finish()
                    }
                }.create().show()
            }
            .addOnFailureListener { e ->
                loading.dismiss()
                Log.w(TAG, "Error updating document", e)
                AlertDialog.Builder(this).apply {
                    setTitle("Error updating document")
                    setMessage("$e")
                    setPositiveButton("Okay") { _, _ ->
                        finish()
                    }
                }.create().show()
            }
    }

    private fun loadReservation(reservationRef: DocumentReference) {
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
    }

    private fun loadReview(reviewRef: DocumentReference) {
        reviewRef.get()
            .addOnSuccessListener { document ->
                val review = document.getString("review").toString()
                val rating = document.getLong("rating")?.toFloat()
                edit_text_review.setText(review)
                ratingBar.rating = rating!!
            }
    }

    override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
        text_rating.text = rating.toString()
    }


}