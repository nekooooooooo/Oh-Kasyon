package com.ljanangelo.oh_kasyon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ljanangelo.oh_kasyon.model.Reviews
import kotlinx.android.synthetic.main.activity_reviews.button_back
import kotlinx.android.synthetic.main.activity_reviews.pullToRefreshEvents
import kotlinx.android.synthetic.main.activity_reviews.recyclerView

class ReviewsActivity : AppCompatActivity() {

    private val key = "retrieved_bid"
    private var bid: String = ""
    private var uid: String = ""

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private var reviewsAdapter: ReviewsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)

        uid = currentUser?.uid.toString()

        val bundle = intent.extras
        if(bundle != null){
            bid = bundle.getString(key).toString()
        }

        val businessReference = db.collection("businesses")
        val reviewsReference = businessReference.document(bid).collection("reviews")

        val defaultQuery = reviewsReference.orderBy("date_of_review", Query.Direction.DESCENDING)

        setUpRecyclerView(defaultQuery)

        button_back.setOnClickListener {
            finish()
        }

        pullToRefreshEvents.setOnRefreshListener {
            refreshList(defaultQuery)
        }
    }

    private fun refreshList(query: Query) {
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Reviews> = FirestoreRecyclerOptions.Builder<Reviews>()
            .setQuery(query, Reviews::class.java)
            .build()

        reviewsAdapter = ReviewsAdapter(firestoreRecyclerOptions)

        reviewsAdapter!!.updateOptions(firestoreRecyclerOptions)
        reviewsAdapter!!.notifyDataSetChanged()

        pullToRefreshEvents.isRefreshing = false
    }

    private fun setUpRecyclerView(query: Query) {

        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Reviews> = FirestoreRecyclerOptions.Builder<Reviews>()
            .setQuery(query, Reviews::class.java)
            .build()

        reviewsAdapter = ReviewsAdapter(firestoreRecyclerOptions)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = reviewsAdapter

    }

    override fun onStart() {
        super.onStart()
        reviewsAdapter!!.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        reviewsAdapter!!.stopListening()
    }

}