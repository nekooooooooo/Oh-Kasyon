package com.ljanangelo.oh_kasyon

import android.net.Uri
import android.util.EventLog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ljanangelo.oh_kasyon.model.EventsPlace
import kotlinx.android.synthetic.main.item_events_places.view.*
import java.lang.Exception

class EventsAdapter(options: FirestorePagingOptions<EventsPlace>) :
    FirestorePagingAdapter<EventsPlace, EventsAdapter.EventViewHolder>(options) {

    private lateinit var listener: OnItemClickListener
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "EventsAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {

        return EventViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_events_places,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int, model: EventsPlace) {
        holder.name.text = model.business_name
        holder.address.text = model.business_address
        //holder.rating.text = model.business_rating.toString()
        //holder.rating_bar.rating = model.business_rating.toFloat()
        //val bid = snapshots.getSnapshot(position).id
        val bid = getItem(position)!!.id
        val image = Uri.parse(model.business_img_url)
        Glide.with(holder.itemView)
            .load(image)
            .apply(RequestOptions().override(500, 500))
            .centerCrop()
            .placeholder(R.drawable.default_picture)
            .into(holder.image)

        var sumRating: Double = 0.0
        var avgRating: Double

        db.collection("businesses").document(bid).collection("reviews")
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "No of Reviews: ${result.size()}")
                val documentSize = result.size()
                for (document in result) {
                    Log.d(TAG, "Rating of ${document.reference.parent.parent?.id}: ${document.getDouble("rating").toString()}")
                    val rating = document.getDouble("rating")!!
                    sumRating += rating
                }
                if(documentSize != 0) {
                    avgRating = sumRating / documentSize
                    holder.rating.text = String.format("%.1f", avgRating)
                    holder.rating_bar.rating = avgRating.toFloat()
                    Log.d(TAG, "Average Rating: $avgRating")
                } else {
                    holder.rating.text = "No Ratings Found"
                }
                Log.d(TAG, "Total rating: $sumRating")
                /*db.collection("businesses").document(bid)
                        .update("business_rating", sumRating)
                        .addOnSuccessListener { Log.d(TAG, "${bid} successfully updated!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }*/
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    override fun onError(e: Exception) {
        super.onError(e)
        Log.e(TAG, e.message)

        retry()
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var name: TextView = itemView.text_place_name
        var address = itemView.text_address!!
        var rating = itemView.text_rating!!
        var rating_bar = itemView.rating!!
        var image = itemView.event_image!!

        init {
            itemView.setOnClickListener { v: View ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    //listener.onItemClick(snapshots.getSnapshot(position), position)
                    listener.onItemClick(getItem(adapterPosition), position)
                }
            }

        }

    }

    interface OnItemClickListener {
        fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }


}