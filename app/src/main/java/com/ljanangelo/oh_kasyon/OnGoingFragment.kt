package com.ljanangelo.oh_kasyon

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.play.core.splitinstall.f
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ljanangelo.oh_kasyon.model.Reservations
import kotlinx.android.synthetic.main.dialog_cancel_reservation.*
import kotlinx.android.synthetic.main.fragment_on_going.*
import kotlinx.android.synthetic.main.fragment_on_going.pullToRefreshEvents
import kotlinx.android.synthetic.main.fragment_on_going.recyclerView


class OnGoingFragment : Fragment() {

    private val TAG = "OnGoingFragment"

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private val collectionReference: CollectionReference = db.collection("reservations")

    private var reservationsAdapter: ReservationsAdapter? = null

    private lateinit var cancelDialog: CancelDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_on_going, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = currentUser?.uid.toString()

        val defaultQuery = collectionReference.whereEqualTo("uid", uid)
            .whereIn("status", listOf("Pending", "Confirmed"))
            .orderBy("date_of_reservation", Query.Direction.DESCENDING)

        cancelDialog = CancelDialog()

        setUpRecyclerView(defaultQuery)

        reservationsAdapter!!.setOnItemClickListener(object : ReservationsAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
                val id = documentSnapshot?.id
                if (id != null) {
                    context?.toast("Item: $id")
                }
                //context?.toast("Slide to Right for options!")
            }
        })

        reservationsAdapter!!.setOnClickListener(object : ReservationsAdapter.OnClickListener {
            override fun onButtonClicked(documentSnapshot: DocumentSnapshot?, position: Int) {
                val id = documentSnapshot?.id
                val name = documentSnapshot?.get("business_name")
                if (id != null) {
                    Log.d(TAG, "Cancel Button for $name. Reservation ID: $id")
                    openCancelDialog(id)
                }
            }

        })

        pullToRefreshEvents.setOnRefreshListener {
            refreshList(defaultQuery)
        }
    }

    private fun openCancelDialog(id: String) {

        val bundle = Bundle()
        bundle.putString("id", id)

        cancelDialog.arguments = bundle
        cancelDialog.show(childFragmentManager, "MyCustomFragment")

        /*cancelDialog.button_close.setOnClickListener {
            cancelDialog.dismiss()
        }*/

    }


    private fun refreshList(query: Query) {
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Reservations> = FirestoreRecyclerOptions.Builder<Reservations>()
            .setQuery(query, Reservations::class.java)
            .build()

        reservationsAdapter = ReservationsAdapter(firestoreRecyclerOptions)

        reservationsAdapter!!.updateOptions(firestoreRecyclerOptions)
        reservationsAdapter!!.notifyDataSetChanged()

        pullToRefreshEvents.isRefreshing = false
    }

    private fun setUpRecyclerView(query: Query) {

        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Reservations> = FirestoreRecyclerOptions.Builder<Reservations>()
            .setQuery(query, Reservations::class.java)
            .build()

        reservationsAdapter = ReservationsAdapter(firestoreRecyclerOptions)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = reservationsAdapter

    }

    override fun onStart() {
        super.onStart()
        reservationsAdapter!!.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        reservationsAdapter!!.stopListening()
    }

}