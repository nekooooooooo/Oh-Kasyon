package com.ljanangelo.oh_kasyon

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.common.reflect.Reflection.getPackageName
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ljanangelo.oh_kasyon.model.Menus
import kotlinx.android.synthetic.main.item_menus.view.*
import kotlin.coroutines.coroutineContext

class MenusAdapter(options: FirestoreRecyclerOptions<Menus>) :
    FirestoreRecyclerAdapter<Menus, MenusAdapter.MenusViewHolder>(options) {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "MenusAdapter"
    private lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenusViewHolder {
        return MenusViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_menus,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MenusViewHolder, position: Int, model: Menus) {
        holder.name.text = model.name
        holder.price.text = "PHP ${model.price}/head"
        holder.dishes.text = ""
        holder.drinks.text = ""
        holder.desserts.text = ""

        val bid = snapshots.getSnapshot(position).reference.parent.parent?.id.toString()
        val mid = snapshots.getSnapshot(position).id

        val businessRef = db.collection("businesses").document(bid)
        val menusRef = businessRef.collection("menus").document(mid)
        val foodsRef = menusRef.collection("foods")

        val images = arrayListOf<ImageView>()
        images.add(holder.image1)
        images.add(holder.image2)
        images.add(holder.image3)
        images.add(holder.image4)

        val cards = arrayListOf<CardView>()
        cards.add(holder.card1)
        cards.add(holder.card2)
        cards.add(holder.card3)

        val circularProgressDrawable = CircularProgressDrawable(holder.name.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        foodsRef
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Number of foods => ${documents.size()}")
                var dishes = ""
                var drinks = ""
                var desserts = ""

                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")

                    if (document.getString("food_type") == "Dish")
                        dishes += "${document.getString("food_name")}, "

                    if (document.getString("food_type") == "Drink")
                        drinks += "${document.getString("food_name")}, "

                    if (document.getString("food_type") == "Dessert")
                        desserts += "${document.getString("food_name")}, "
                }

                dishes   = if(dishes != "") dishes.substring(0, dishes.length - 2) else dishes
                drinks   = if(drinks != "") drinks.substring(0, drinks.length - 2) else drinks
                desserts = if(desserts != "") desserts.substring(0, desserts.length - 2) else desserts

                holder.dishes.text = if(dishes != "") dishes else "No Dishes"
                holder.drinks.text = if(drinks != "") drinks else "No Drinks"
                holder.desserts.text = if(desserts != "") desserts else "No Desserts"

                when {
                    documents.size() >= 3 -> {
                        for(i in 0 until 3) {

                            Log.d(TAG, "$i")

                            val imageUrl = documents.documents[i].getString("image_url") ?: ""

                            Log.d(TAG, imageUrl)

                            Glide.with(holder.itemView)
                                .load(Uri.parse(imageUrl))
                                .apply(RequestOptions().override(500, 500))
                                .centerCrop()
                                .placeholder(circularProgressDrawable)
                                .into(images[i])

                        }
                    }
                    documents.size() == 2 -> {

                        images[3].visibility = View.GONE
                        cards[2].visibility = View.GONE

                        for(i in 0 until 2) {

                            Log.d(TAG, "$i")

                            val imageUrl = documents.documents[i].getString("image_url") ?: ""

                            Log.d(TAG, imageUrl)

                            Glide.with(holder.itemView)
                                .load(Uri.parse(imageUrl))
                                .apply(RequestOptions().override(500, 500))
                                .centerCrop()
                                .placeholder(circularProgressDrawable)
                                .into(images[i])

                        }
                    }
                    documents.size() == 1 -> {

                        images[3].visibility = View.GONE
                        cards[2].visibility = View.GONE
                        cards[1].visibility = View.GONE

                        for(i in 0 until 1) {

                            Log.d(TAG, "$i")

                            val imageUrl = documents.documents[i].getString("image_url") ?: ""

                            Log.d(TAG, imageUrl)

                            Glide.with(holder.itemView)
                                .load(Uri.parse(imageUrl))
                                .apply(RequestOptions().override(500, 500))
                                .centerCrop()
                                .placeholder(circularProgressDrawable)
                                .into(images[i])

                        }
                    }
                }


                /*for(i in 0 until 3) {

                    Log.d(TAG, "$i")

                    val imageUrl = documents.documents[i].getString("image_url") ?: ""

                    Log.d(TAG, imageUrl)

                    Glide.with(holder.itemView)
                        .load(Uri.parse(imageUrl))
                        .apply(RequestOptions().override(500, 500))
                        .centerCrop()
                        .placeholder(R.drawable.default_picture)
                        .into(image[i])

                }*/

                /*Glide.with(holder.itemView)
                    .load(Uri.parse(documents.documents[0].getString("image_url")))
                    .apply(RequestOptions().override(500, 500))
                    .centerCrop()
                    .placeholder(R.drawable.default_picture)
                    .into(image[0])*/


            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }

    }

    inner class MenusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var name: TextView = itemView.text_menu_name
        var price: TextView = itemView.text_price
        var dishes: TextView = itemView.text_dishes
        var drinks: TextView = itemView.text_drinks
        var desserts: TextView = itemView.text_desserts
        var image1: ImageView = itemView.image_food_1
        var image2: ImageView = itemView.image_food_2
        var image3: ImageView = itemView.image_food_3
        var image4: ImageView = itemView.image_food_4
        var card1: CardView = itemView.card_image_1
        var card2: CardView = itemView.card_image_2
        var card3: CardView = itemView.card_image_3

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