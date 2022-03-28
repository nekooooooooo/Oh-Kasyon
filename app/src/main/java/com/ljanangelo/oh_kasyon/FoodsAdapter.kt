package com.ljanangelo.oh_kasyon

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ljanangelo.oh_kasyon.model.Foods
import kotlinx.android.synthetic.main.item_foods.view.*

class FoodsAdapter(options: FirestoreRecyclerOptions<Foods>) :
    FirestoreRecyclerAdapter<Foods, FoodsAdapter.FoodsViewHolder>(options) {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "FoodsAdapter"
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FoodsAdapter.FoodsViewHolder {
        return FoodsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_foods,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: FoodsAdapter.FoodsViewHolder,
        position: Int,
        model: Foods
    ) {
        holder.foodName.text = model.food_name
        holder.foodDesc.text = if(model.description != "") model.description else "No description..."
        holder.foodImage.clipToOutline = true
        val imageUrl = model.image_url

        Glide.with(holder.itemView)
            .load(Uri.parse(imageUrl))
            .apply(RequestOptions().override(500, 500))
            .centerCrop()
            .placeholder(R.drawable.default_picture)
            .into(holder.foodImage)
    }

    inner class FoodsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var foodName: TextView = itemView.text_food_name
        var foodDesc: TextView = itemView.text_description
        var foodImage: ImageView = itemView.food_image

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