package com.ljanangelo.oh_kasyon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.ljanangelo.oh_kasyon.model.Messages
import kotlinx.android.synthetic.main.item_chat_left.view.*
import java.text.SimpleDateFormat

class MessagesAdapter(options: FirestoreRecyclerOptions<Messages>) :
    FirestoreRecyclerAdapter<Messages, MessagesAdapter.MessagesViewHolder>(options) {

    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1

    private val user = FirebaseAuth.getInstance()

    private var mRecyclerView: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        return if (viewType == MSG_TYPE_RIGHT)
            MessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_right, parent, false))
        else
            MessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_left, parent, false))
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int, model: Messages) {
        holder.message.text = model.message
        holder.date.text = SimpleDateFormat("MM/dd/yyyy' 'h:mm a").format(model.date_sent?.toDate())
    }

    class MessagesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val message: TextView = itemView.text_message
        val date: TextView = itemView.text_date
    }

    override fun getItemViewType(position: Int): Int {
        return if (snapshots.getSnapshot(position).getString("sender").equals(user.uid))
            MSG_TYPE_RIGHT
        else
            MSG_TYPE_LEFT
    }

    override fun onDataChanged() {
        super.onDataChanged()
        mRecyclerView!!.scrollToPosition(0)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }
}