package com.ljanangelo.oh_kasyon

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ljanangelo.oh_kasyon.model.EventsPlace
import kotlinx.android.synthetic.main.fragment_events_place.*
import kotlinx.android.synthetic.main.item_events_places.*
import kotlinx.android.synthetic.main.item_events_places.view.*


class EventsPlaceFragment : Fragment() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private val collectionReference:CollectionReference = db.collection("businesses")

    private var eventsAdapter: EventsAdapter? = null

    private val ascending = Query.Direction.ASCENDING
    private val descending = Query.Direction.DESCENDING

    private val TAG = "EventsPlaceFragment"

    private val rotateMainOpen: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_open_main_fab_anim) }
    private val rotateMainClose: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_close_main_fab_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim) }
    private val fromRight: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_right_anim) }
    private val toRight: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_right_anim) }

    private var mainFabClicked = false
    private var sortFabClicked = false

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events_place, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val defaultQuery = collectionReference
                .whereEqualTo("business_status", "Approved")
                .orderBy("business_name", Query.Direction.ASCENDING)

        setUpRecyclerView(defaultQuery)

        eventsAdapter!!.setOnItemClickListener(object : EventsAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
                val id = documentSnapshot?.id
                val rating = (recyclerView.findViewHolderForAdapterPosition(position)!!.itemView.text_rating).text.toString()

                val ratingToMove = if(rating == "No Ratings Found") 0.0 else rating.toDouble()

                Log.d(TAG, "Rating: $ratingToMove")

                /*val name = documentSnapshot?.get("business_name")
                val address = documentSnapshot?.get("business_address")
                val rating = documentSnapshot?.get("business_rating").toString()
                val geopoint = documentSnapshot?.get("business_geopoint").toString()
                AlertDialog.Builder(activity).apply {
                    setTitle("$name")
                    setMessage(
                        "Id: $id \n" +
                                "Address: $address \n" +
                                "Rating: $rating \n" +
                                geopoint
                    )
                    setPositiveButton("Ok") { _, _ ->

                    }
                }.create().show()*/
                if (id != null) {
                    context?.openEventsInfo(id, ratingToMove)
                }
            }
        })

        /*recyclerView.adapter = adapter
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager*/

        //getBusiness()

        /*button_test.setOnClickListener {
            sortBy("business_rating", direction)
        }*/

        floatingActButton.setOnClickListener {
            onMainButtonClicked()
        }

        btn_search.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val edittext = EditText(activity)
            builder.setTitle("Search")
            builder.setView(edittext)
            builder.setPositiveButton("Ok") { dialog, whichButton -> //What ever you want to do with the value
                    val search = edittext.text.toString()
                    context?.toast(search)
                    searchEvents("business_name", search)
                }

            builder.setNegativeButton("Cancel")  { dialog, whichButton ->

                }
            builder.show()
        }

        btn_sort.setOnClickListener {
            onSortButtonClicked()
        }

        btn_sort_rating.setOnClickListener {
            showDirectionDialog(context, "Rating", "business_rating", R.array.direction_rating)
        }

        btn_sort_alpha.setOnClickListener {
            showDirectionDialog(context, "Alphabetical", "business_name", R.array.direction)
        }

        pullToRefreshEvents.setOnRefreshListener {
            //refreshList(defaultQuery)
            eventsAdapter!!.refresh()
            pullToRefreshEvents.isRefreshing = false
        }

    }

    private fun onMainButtonClicked() {
        setVisibilityChildren(mainFabClicked)
        setAnimationChildren(mainFabClicked)
        mainFabClicked = !mainFabClicked
        if(sortFabClicked) {
            onSortButtonClicked()
        }
    }

    private fun setVisibilityChildren(clicked: Boolean) {
        if(!clicked){
            btn_sort.visibility = View.VISIBLE
            // btn_search.visibility = View.VISIBLE
        } else {
            btn_sort.visibility = View.GONE
            // btn_search.visibility = View.GONE
        }
    }

    private fun setAnimationChildren(clicked: Boolean) {
        if(!clicked){
            btn_sort.startAnimation(fromBottom)
            btn_search.startAnimation(fromBottom)
            floatingActButton.startAnimation(rotateMainOpen)
        } else {
            btn_sort.startAnimation(toBottom)
            btn_search.startAnimation(toBottom)
            floatingActButton.startAnimation(rotateMainClose)
        }
    }

    private fun onSortButtonClicked() {
        setVisibilitySortChildren(sortFabClicked)
        setAnimationSortChildren(sortFabClicked)
        sortFabClicked = !sortFabClicked
    }

    private fun setVisibilitySortChildren(clicked: Boolean) {
        if(!clicked){
            btn_sort_rating.visibility = View.VISIBLE
            btn_sort_alpha.visibility = View.VISIBLE
        } else {
            btn_sort_rating.visibility = View.GONE
            btn_sort_alpha.visibility = View.GONE
        }
    }

    private fun setAnimationSortChildren(clicked: Boolean) {
        if(!clicked){
            btn_sort_rating.startAnimation(fromRight)
            btn_sort_alpha.startAnimation(fromRight)
            //btn_sort.startAnimation(rotateSortOpen)
        } else {
            btn_sort_rating.startAnimation(toRight)
            btn_sort_alpha.startAnimation(toRight)
            //btn_sort.startAnimation(rotateSortClose)
        }
    }

    private fun setUpRecyclerView(query: Query) {

        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(2)
            .setPageSize(10)
            .build()


        val firestoreRecyclerOptions: FirestorePagingOptions<EventsPlace> = FirestorePagingOptions.Builder<EventsPlace>()
            .setLifecycleOwner(this)
            .setQuery(query, config, EventsPlace::class.java)
            .build()

        eventsAdapter = EventsAdapter(firestoreRecyclerOptions)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = eventsAdapter



    }

    private fun sortBy(field: String, direction: Query.Direction) {
        val query = collectionReference.orderBy(field, direction)
        refreshList(query)
    }

    private fun searchEvents(field: String, value: Any?) {
        val query = collectionReference.whereEqualTo(field, value)
        refreshList(query)
    }

    fun refreshList(query: Query) {

        val config: PagedList.Config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(2)
            .setPageSize(10)
            .build()

        val firestoreRecyclerOptions: FirestorePagingOptions<EventsPlace> = FirestorePagingOptions.Builder<EventsPlace>()
            .setLifecycleOwner(this)
            .setQuery(query, config, EventsPlace::class.java)
            .build()

        eventsAdapter!!.updateOptions(firestoreRecyclerOptions)
        eventsAdapter!!.notifyDataSetChanged()

        pullToRefreshEvents.isRefreshing = false
    }

    override fun onStart() {
        super.onStart()
        eventsAdapter!!.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        eventsAdapter!!.stopListening()
    }

    private fun showDirectionDialog(
            context: Context?,
            sortTitle: String,
            field: String?,
            items: Int,
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Sort by $sortTitle")
            .setItems(items) { dialog, which ->
                when(which){
                    0 -> {
                        field?.let { sortBy(it, ascending) }
                    }
                    1 -> {
                        field?.let { sortBy(it, descending) }
                    }
                }
            }
        builder.create()
        builder.show()
    }


    /*private fun getBusiness() {
        var name : String

        val uid = currentUser?.uid

        val docRef = db.collection("users").document(uid.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                }
            }

        db.collection("businesses")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    name = document.getString("business_name").toString()
                }
            }
            .addOnFailureListener { exception ->

            }
    }*/

    /*fun orderBy(query: Query) {
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<EventsPlace> = FirestoreRecyclerOptions.Builder<EventsPlace>()
            .setQuery(query, EventsPlace::class.java)
            .build()

        eventsAdapter!!.updateOptions(firestoreRecyclerOptions)
        eventsAdapter!!.notifyDataSetChanged()
    }*/

}

