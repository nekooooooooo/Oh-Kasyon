package com.ljanangelo.oh_kasyon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ljanangelo.oh_kasyon.model.Menus
import kotlinx.android.synthetic.main.activity_menus.*
import kotlinx.android.synthetic.main.activity_menus.button_back
import kotlinx.android.synthetic.main.activity_menus.pullToRefreshEvents
import kotlinx.android.synthetic.main.activity_menus.recyclerView

class MenusActivity : AppCompatActivity() {

    private val key = "retrieved_bid"
    private var bid: String = ""
    private var uid: String = ""

    private val TAG = "MenusActivity"

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    private var menusAdapter: MenusAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menus)

        uid = currentUser?.uid.toString()

        val bundle = intent.extras
        if(bundle != null){
            bid = bundle.getString(key).toString()
        }

        val businessReference = db.collection("businesses")
        val menusReference = businessReference.document(bid).collection("menus").orderBy("name", Query.Direction.ASCENDING)

        setUpRecyclerView(menusReference)


        menusAdapter!!.setOnItemClickListener(object : MenusAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
                val id = documentSnapshot?.id

                if (id != null) {
                    openMenu(bid, id)
                }
            }
        })

        button_back.setOnClickListener {
            finish()
        }

        pullToRefreshEvents.setOnRefreshListener {
            refreshList(menusReference)
        }

    }

    private fun setUpRecyclerView(query: Query) {

        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Menus> = FirestoreRecyclerOptions.Builder<Menus>()
            .setQuery(query, Menus::class.java)
            .build()

        menusAdapter = MenusAdapter(firestoreRecyclerOptions)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = menusAdapter

    }

    private fun refreshList(query: Query) {
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Menus> = FirestoreRecyclerOptions.Builder<Menus>()
            .setQuery(query, Menus::class.java)
            .build()

        menusAdapter = MenusAdapter(firestoreRecyclerOptions)

        menusAdapter!!.updateOptions(firestoreRecyclerOptions)
        menusAdapter!!.notifyDataSetChanged()

        pullToRefreshEvents.isRefreshing = false
    }

    override fun onStart() {
        super.onStart()
        menusAdapter!!.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        menusAdapter!!.stopListening()
    }

}