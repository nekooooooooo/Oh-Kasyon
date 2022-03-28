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
import com.ljanangelo.oh_kasyon.model.Chats
import com.ljanangelo.oh_kasyon.model.EventsPlace
import kotlinx.android.synthetic.main.fragment_events_place.*
import kotlinx.android.synthetic.main.fragment_messages.*
import kotlinx.android.synthetic.main.fragment_messages.pullToRefreshEvents
import kotlinx.android.synthetic.main.fragment_messages.recyclerView

class ConversationsFragment : Fragment() {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "MessagesFragment"

    private val collectionReference: CollectionReference = db.collection("chats")

    private var chatsAdapter: ChatsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = currentUser?.uid.toString()
        val defaultQuery = collectionReference.whereEqualTo("user_id", uid).orderBy("last_message", Query.Direction.DESCENDING)


        setUpRecyclerView(defaultQuery)

        chatsAdapter!!.setOnItemClickListener(object : ChatsAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
                val id = documentSnapshot?.id
                val bid = documentSnapshot?.getString("bid")
                val baid = documentSnapshot?.getString("business_account_id")

                if (id != null) {
                    Log.d(TAG, "Doc ID: $id BID: $bid BaID: $baid")
                    context?.openChats(id, bid.toString(), baid.toString())
                }
            }
        })

        pullToRefreshEvents.setOnRefreshListener {
            refreshList(defaultQuery)
        }

    }

    private fun refreshList(query: Query) {

        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Chats> = FirestoreRecyclerOptions.Builder<Chats>()
                .setQuery(query, Chats::class.java)
                .build()

        chatsAdapter = ChatsAdapter(firestoreRecyclerOptions)

        chatsAdapter!!.updateOptions(firestoreRecyclerOptions)
        chatsAdapter!!.notifyDataSetChanged()

        pullToRefreshEvents.isRefreshing = false
    }

    private fun setUpRecyclerView(query: Query) {

        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Chats> = FirestoreRecyclerOptions.Builder<Chats>()
                .setQuery(query, Chats::class.java)
                .build()

        chatsAdapter = ChatsAdapter(firestoreRecyclerOptions)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = chatsAdapter

    }

    override fun onStart() {
        super.onStart()
        chatsAdapter!!.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        chatsAdapter!!.stopListening()
    }

}
