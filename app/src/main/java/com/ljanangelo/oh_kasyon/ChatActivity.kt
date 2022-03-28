package com.ljanangelo.oh_kasyon

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ljanangelo.oh_kasyon.model.Messages
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_chat.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : AppCompatActivity() {

    private val key1 = "retrieved_cid"
    private val key2 = "retrieved_bid"
    private val key3 = "retrieved_baid"
    private var cid: String = ""
    private var uid: String = ""
    private var bid: String = ""
    private var baid: String = ""

    private val TAG = "ChatActivity"

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val chatReference = db.collection("chats")
    private val businessReference = db.collection("businesses")

    private var messagesAdapter: MessagesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        uid = currentUser?.uid.toString()

        val bundle = intent.extras
        if(bundle != null){
            cid = bundle.getString(key1).toString()
            bid = bundle.getString(key2).toString()
            baid = bundle.getString(key3).toString()
        }

        Log.d(TAG, "cID: $cid bID: $bid")

        getChatInfo(bid)

        val defaultQuery = chatReference.document(cid)
            .collection("messages")
            .orderBy("date_sent", Query.Direction.DESCENDING)
            .limit(30)

        val firestoreRecyclerOptions: FirestoreRecyclerOptions<Messages> = FirestoreRecyclerOptions.Builder<Messages>()
            .setQuery(defaultQuery, Messages::class.java)
            .build()

        messagesAdapter = MessagesAdapter(firestoreRecyclerOptions)

        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
            reverseLayout = true
        }
        recyclerView.adapter = messagesAdapter
        //toast(recyclerView.adapter!!.itemCount.toString())

        button_back.setOnClickListener {
            finish()
        }

        button_send.setOnClickListener {

            val today: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Calendar.getInstance().time)
            val message = text_send.text.toString().trim()

            if(message.isEmpty()) return@setOnClickListener

            sendMessage(today, message, cid)
        }

        /*text_send.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                Handler().postDelayed({
                    recyclerView.scrollToPosition(0)
                }, 250)
            }
            false
        }*/

        text_send.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                Log.d(TAG, "Has Focus")

            } else
                Log.d(TAG, "Lost Focus")
        }

        text_send.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                chat_length.text = text_send.length().toString() + "/100"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //recyclerView.smoothScrollToPosition(0)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun sendMessage(today: String, message: String, cid: String) {

        val chatData = hashMapOf(
            "receiver" to baid,
            "sender" to uid,
            "message" to message,
            "date_sent" to getDateFromString(today)
        )

        val messageReference = chatReference.document(cid).collection("messages")

        messageReference.add(chatData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Document written with ID: ${documentReference.id}")
                chatReference.document(cid)
                    .update(
                        mapOf(
                            "last_message" to getDateFromString(today),
                            "last_message_sent" to message
                        )
                    )
            }
            .addOnFailureListener {
                Log.w("AppDebug", "Error writing document", it)
            }

        /*Handler().postDelayed({
            recyclerView.scrollToPosition(0)
        }, 250)*/
        text_send.setText("")
    }

    private fun getChatInfo(bid: String) {
        businessReference.document(bid)
            .get()
            .addOnSuccessListener { document ->
                if (document  != null) {
                    val business_image_url = document.getString("business_img_url") ?: ""
                    val business_name = document.getString("business_name")
                    text_name.text = business_name
                    Glide.with(this)
                        .load(Uri.parse(business_image_url))
                        .apply(RequestOptions().override(500, 500))
                        .centerCrop()
                        .placeholder(R.drawable.default_picture)
                        .into(profile_image)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun getDateFromString(datetoSaved: String?): Date? {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

        return try {
            format.parse(datetoSaved)
        } catch (e: ParseException) {
            null
        }
    }

    override fun onStart() {
        super.onStart()
        messagesAdapter!!.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesAdapter!!.stopListening()
    }
}