package com.ljanangelo.oh_kasyon

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ljanangelo.oh_kasyon.model.Reviews
import kotlinx.android.synthetic.main.activity_reviews.*
import kotlinx.android.synthetic.main.item_reviews.view.*
import java.text.SimpleDateFormat
import kotlin.coroutines.coroutineContext

class ReviewsAdapter(options: FirestoreRecyclerOptions<Reviews>) :
    FirestoreRecyclerAdapter<Reviews, ReviewsAdapter.ReviewsViewHolder>(options) {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "ReviewsAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsAdapter.ReviewsViewHolder {
        return ReviewsViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_reviews,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: ReviewsAdapter.ReviewsViewHolder, position: Int, model: Reviews) {
        holder.review.text = model.review
        holder.rating_bar.rating = model.rating.toFloat()

        val date = model.date_of_review!!
        val formattedDate = SimpleDateFormat("MMMM' 'dd', 'yyyy' at 'h:mm a' '").format(model.date_of_review.toDate()) ?: ""

        holder.date.text = formattedDate

        db.collection("users").document(model.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document  != null) {
                    val firstName = document.getString("first_name") ?: ""
                    val middleName = document.getString("middle_name") ?: ""
                    val lastName = document.getString("last_name") ?: ""

                    var fullName = "$firstName $lastName"

                    holder.name.text = fullName

                    val userPhotoUrl = document.get("photoUrl") as String
                    Glide.with(holder.itemView)
                        .load(userPhotoUrl)
                        .apply(RequestOptions().override(500, 500))
                        .centerCrop()
                        .placeholder(R.drawable.default_picture)
                        .into(holder.image)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

        /*val formattedDate = if (model.date_of_review == null) ""
            else SimpleDateFormat("MMMM' 'dd', 'yyyy' at 'HH:mm a' '").format(model.date_of_review.toDate())*/

    }

    override fun onDataChanged() {
        super.onDataChanged()
        Log.d(TAG, "Count: $itemCount")
    }

    inner class ReviewsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var name = itemView.text_user_name!!
        var review = itemView.text_review!!
        var image = itemView.user_image!!
        var rating_bar = itemView.rating!!
        var date = itemView.text_date
    }

}