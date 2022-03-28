package com.ljanangelo.oh_kasyon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_food.*

class FoodActivity : AppCompatActivity() {

    private val key1 = "retrieved_fid"
    private val key2 = "retrieved_bid"
    private val key3 = "retrieved_mid"
    private var fid: String = ""
    private var bid: String = ""
    private var mid: String = ""
    private var uid: String = ""

    private val TAG = "FoodsActivity"

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food)

        uid = currentUser?.uid.toString()

        val bundle = intent.extras
        if(bundle != null){
            fid = bundle.getString(key1).toString()
            bid = bundle.getString(key2).toString()
            mid = bundle.getString(key3).toString()
        }

        val businessRef = db.collection("businesses").document(bid)
        val menusRef = businessRef.collection("menus").document(mid)
        val foodsRef = menusRef.collection("foods")

        foodsRef.document(fid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: ${snapshot.data}")
                    text_food_name.text = snapshot.getString("food_name")
                    val desc = snapshot.getString("description")
                    text_food_desc.text = if(!desc.isNullOrEmpty()) desc else "No Description..."
                    val imageUrl = snapshot.getString("image_url") ?: ""

                    Glide.with(this)
                        .load(imageUrl)
                        .apply(RequestOptions().override(1000, 1000))
                        .centerCrop()
                        .placeholder(R.drawable.default_picture)
                        .into(image_food)


                } else {
                    Log.d(TAG, "Current data: null")
                }
            }

        button_back.setOnClickListener {
            finish()
        }

    }
}