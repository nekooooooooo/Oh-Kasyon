package com.ljanangelo.oh_kasyon

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.ljanangelo.oh_kasyon.model.Reservations
import kotlinx.android.synthetic.main.item_history.view.*
import java.text.SimpleDateFormat


class HistoryAdapter(options: FirestoreRecyclerOptions<Reservations>) :
    FirestoreRecyclerAdapter<Reservations, HistoryAdapter.HistoryViewHolder>(options) {

    private lateinit var listener: OnItemClickListener
    private lateinit var clickListener: OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_history,
                        parent,
                        false
                )
        )
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(
            holder: HistoryViewHolder,
            position: Int,
            model: Reservations,
    ) {

        holder.name.text = model.business_name

        val date = when (model.timestamp) {
            null -> ""
            else -> SimpleDateFormat("MMMM' 'dd', 'yyyy' at 'h:mm a' '").format(model.timestamp.toDate())
        }

        holder.date.text = "Date: $date"
        holder.guests.text = "No. of Guests: ${model.no_of_guests}"
        holder.status.text = "Status: ${model.status}"

        holder.dateOfReservation.text = if (model.date_of_reservation == null) "" else SimpleDateFormat("MM-dd-yyyy' 'h:mm a").format(model.date_of_reservation?.toDate())

        holder.status.let { status ->
            when(model.status) {
                "Pending" -> status.setBackgroundResource(R.drawable.status_pending_background)
                "Confirmed" -> status.setBackgroundResource(R.drawable.status_confirmed_background)
                "Cancelled" -> {
                    status.setBackgroundResource(R.drawable.status_cancelled_background)
                    holder.reviewButton.visibility = View.GONE
                }
                "Declined" -> {
                    status.setBackgroundResource(R.drawable.status_cancelled_background)
                    holder.reviewButton.visibility = View.GONE
                }
                "Done" -> status.setBackgroundResource(R.drawable.status_done_background)
                "Done & Reviewed" -> {
                    status.setBackgroundResource(R.drawable.status_done_background)
                    holder.reviewButton.visibility = View.GONE
                }
            }
        }


    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var name: TextView = itemView.text_place_name
        var date: TextView = itemView.text_date
        var guests: TextView = itemView.text_guests
        var status: TextView = itemView.text_status
        var dateOfReservation: TextView = itemView.text_date_of_reservation
        var reviewButton: Button = itemView.review_button
        var editReviewButton: Button = itemView.edit_review_button

        init {
            itemView.setOnClickListener { v: View ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(snapshots.getSnapshot(position), position)
                }
            }

            reviewButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onButtonClicked(snapshots.getSnapshot(position), position)
                }
            }

            editReviewButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onButtonClicked(snapshots.getSnapshot(position), position)
                }
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnClickListener {
        fun onButtonClicked(documentSnapshot: DocumentSnapshot?, position: Int)
    }

    fun setOnClickListener(clickListener: OnClickListener) {
        this.clickListener = clickListener
    }

}