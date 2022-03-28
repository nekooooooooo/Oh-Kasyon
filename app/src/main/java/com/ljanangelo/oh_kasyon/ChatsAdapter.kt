package com.ljanangelo.oh_kasyon

import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ljanangelo.oh_kasyon.model.Chats
import kotlinx.android.synthetic.main.item_chats.view.*
import java.text.SimpleDateFormat


class ChatsAdapter(options: FirestoreRecyclerOptions<Chats>) :
    FirestoreRecyclerAdapter<Chats, ChatsAdapter.ChatsViewHolder>(options) {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "ChatsAdapter"
    private lateinit var listener: OnItemClickListener
    private val DEFAULT_IMAGE_LINK = "https://firebasestorage.googleapis.com/v0/b/oh-kasyon.appspot.com/o/pics%2Fdefault-profile-picture1.jpg?alt=media&token=c923dbf2-e004-4ae4-a5f9-5e73ae90348b"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_chats,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: ChatsAdapter.ChatsViewHolder, position: Int, model: Chats) {

        val bid: String = model.bid
        val business_account_id: String = model.user_id
        val last_message: Timestamp? = model.last_message
        val message = model.last_message_sent
        val status: String = model.status
        val user_id: String = model.user_id
        val id = snapshots.getSnapshot(position).id

        db.collection("businesses").document(bid)
                .get()
                .addOnSuccessListener { document  ->
                    if (document  != null) {
                        val business_image_url = document.getString("business_img_url") ?: ""
                        val business_name = document.getString("business_name")
                        holder.textBusinessName.text = business_name
                        Glide.with(holder.itemView)
                                .load(Uri.parse(business_image_url))
                                .apply(RequestOptions().override(500, 500))
                                .centerCrop()
                                .placeholder(R.drawable.default_picture)
                                .into(holder.eventImage)
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }

        holder.textLastMessageDate.text = if (last_message == null) "" else SimpleDateFormat("MMM dd, yyyy' 'h:mm a").format(last_message.toDate())
        holder.textLastMessage.text = if (message == null) "" else message

        /*db.collection("chats").document(id).collection("messages")
                .orderBy("date_sent", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { documents  ->
                    for (document in documents) {
                        Log.d(TAG, "Read ${document.data}")
                        val message = document.getString("message")
                        holder.textLastMessage.text = message
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }*/


        if (status == "unseen") {
            holder.textBusinessName.setTypeface(holder.textBusinessName.typeface, Typeface.BOLD)
            holder.textLastMessage.setTypeface(holder.textLastMessage.typeface, Typeface.BOLD)
            holder.textLastMessageDate.setTypeface(holder.textLastMessageDate.typeface, Typeface.BOLD)
        }

        Log.d(TAG, "Chats Updated")


    }

    inner class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var textBusinessName: TextView = itemView.text_business_name
        var textLastMessage: TextView = itemView.text_last_message
        var textLastMessageDate: TextView = itemView.text_last_message_date
        var eventImage: ImageView = itemView.event_image

        init {
            itemView.setOnClickListener { v: View ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(snapshots.getSnapshot(position), position)
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