package com.ljanangelo.oh_kasyon

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ljanangelo.oh_kasyon.model.Reservations
import kotlinx.android.synthetic.main.fragment_history.*
//import kotlinx.android.synthetic.main.fragment_history.pullToRefreshEvents
import kotlinx.android.synthetic.main.fragment_history.recyclerView


class HistoryFragment : Fragment() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private val collectionReference: CollectionReference = db.collection("reservations")

    private var historyAdapter: HistoryAdapter? = null

    private val TAG = javaClass.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = currentUser?.uid.toString()

        val defaultQuery = collectionReference.whereEqualTo("uid", uid)
            .whereIn("status", listOf("Cancelled", "Done", "Done & Reviewed", "Declined"))
            .orderBy("date_of_reservation", Query.Direction.DESCENDING)


        setUpRecyclerView(defaultQuery)

        historyAdapter!!.setOnItemClickListener(object : HistoryAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
                val id = documentSnapshot?.id
                val bid = documentSnapshot?.getString("bid").toString()
                val status = documentSnapshot?.get("status")
                if (id != null) {
                    when(status) {
                        "Done & Reviewed" -> {
                            context?.toast("Edit Review: $id")
                            Log.d(TAG, "BID: $bid RID: $id")
                            context?.openEditReview(bid, id)
                        }
                        else -> context?.toast("Item: $id")
                    }
                    //context?.toast("Item: $id")
                }
                //context?.toast("Slide to Right for options!")
            }

        })

        historyAdapter!!.setOnClickListener(object : HistoryAdapter.OnClickListener {
            override fun onButtonClicked(documentSnapshot: DocumentSnapshot?, position: Int) {
                val id = documentSnapshot?.id
                val name = documentSnapshot?.get("business_name")
                if (id != null) {
                    context?.toast("Review Button for $name. Reservation ID: $id")
                    context?.openReview(id)
                }
            }

        })

        pullToRefreshEvents.setOnRefreshListener {
            refreshList(defaultQuery)
        }
    }

    private fun refreshList(query: Query) {
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Reservations> = FirestoreRecyclerOptions.Builder<Reservations>()
            .setQuery(query, Reservations::class.java)
            .build()

        historyAdapter = HistoryAdapter(firestoreRecyclerOptions)

        historyAdapter!!.updateOptions(firestoreRecyclerOptions)
        historyAdapter!!.notifyDataSetChanged()

        pullToRefreshEvents.isRefreshing = false
    }

    private fun setUpRecyclerView(query: Query) {

        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Reservations> = FirestoreRecyclerOptions.Builder<Reservations>()
            .setQuery(query, Reservations::class.java)
            .build()

        historyAdapter = HistoryAdapter(firestoreRecyclerOptions)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = historyAdapter

    }

    override fun onStart() {
        super.onStart()
        historyAdapter!!.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        historyAdapter!!.stopListening()
    }

}