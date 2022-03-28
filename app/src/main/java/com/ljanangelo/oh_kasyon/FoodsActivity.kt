package com.ljanangelo.oh_kasyon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ljanangelo.oh_kasyon.model.Foods
import kotlinx.android.synthetic.main.activity_foods.*

class FoodsActivity : AppCompatActivity() {

    private val key1 = "retrieved_bid"
    private val key2 = "retrieved_mid"
    private var bid: String = ""
    private var mid: String = ""
    private var uid: String = ""

    private val TAG = "FoodsActivity"

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private var foodsAdapter: FoodsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foods)

        uid = currentUser?.uid.toString()

        val bundle = intent.extras
        if(bundle != null){
            bid = bundle.getString(key1).toString()
            mid = bundle.getString(key2).toString()
        }

        val businessRef = db.collection("businesses").document(bid)
        val menusRef = businessRef.collection("menus").document(mid)
        val foodsRef = menusRef.collection("foods")

        setUpRecyclerView(foodsRef)

        foodsAdapter!!.setOnItemClickListener(object: FoodsAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
                val id = documentSnapshot?.id

                if (id != null) {
                    openFood(id, bid, mid)
                }
            }
        })

        button_back.setOnClickListener {
            finish()
        }

        pullToRefreshEvents.setOnRefreshListener {
            refreshList(foodsRef)
        }

    }

    private fun setUpRecyclerView(query: Query) {
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Foods> = FirestoreRecyclerOptions.Builder<Foods>()
            .setQuery(query, Foods::class.java)
            .build()

        foodsAdapter = FoodsAdapter(firestoreRecyclerOptions)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = foodsAdapter
    }

    private fun refreshList(query: Query) {
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Foods> = FirestoreRecyclerOptions.Builder<Foods>()
            .setQuery(query, Foods::class.java)
            .build()

        foodsAdapter = FoodsAdapter(firestoreRecyclerOptions)

        foodsAdapter!!.updateOptions(firestoreRecyclerOptions)
        foodsAdapter!!.notifyDataSetChanged()

        pullToRefreshEvents.isRefreshing = false
    }

    override fun onStart() {
        super.onStart()
        foodsAdapter!!.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        foodsAdapter!!.stopListening()
    }

}