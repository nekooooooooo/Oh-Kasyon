package com.ljanangelo.oh_kasyon

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
import kotlinx.android.synthetic.main.item_reservations.view.*
import java.text.SimpleDateFormat

class ReservationsAdapter(options: FirestoreRecyclerOptions<Reservations>) :
    FirestoreRecyclerAdapter<Reservations, ReservationsAdapter.ReservationViewHolder>(options) {

    private lateinit var listener: OnItemClickListener
    private lateinit var clickListener: OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        return ReservationViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_reservations,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ReservationViewHolder,
        position: Int,
        model: Reservations
    ) {

        holder.name.text = model.business_name
        val date = when (model.timestamp) {
            null -> ""
            else -> SimpleDateFormat("MMMM' 'dd', 'yyyy' at 'h:mm a' '").format(model.timestamp.toDate())
        }
        holder.date.text = "Date: $date"
        holder.guests.text = "No. of Guests: ${model.no_of_guests}"
        holder.status.text = "Status: ${model.status}"
        holder.dateOfReservation.text = when (model.date_of_reservation) {
            null -> ""
            else -> "${SimpleDateFormat("MM-dd-yyyy' 'h:mm a").format(model.date_of_reservation?.toDate())}"
        }

        holder.status.let { status ->
            when(model.status) {
                "Pending" -> status.setBackgroundResource(R.drawable.status_pending_background)
                "Confirmed" -> status.setBackgroundResource(R.drawable.status_confirmed_background)
                "Cancelled" -> status.setBackgroundResource(R.drawable.status_cancelled_background)
                "Declined" -> status.setBackgroundResource(R.drawable.status_cancelled_background)
                "Done" -> status.setBackgroundResource(R.drawable.status_done_background)
            }
        }
    }

    inner class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var name: TextView = itemView.text_place_name
        var date: TextView = itemView.text_date
        var guests: TextView = itemView.text_guests
        var status: TextView = itemView.text_status
        var dateOfReservation: TextView = itemView.text_date_of_reservation
        var cancelButton: Button = itemView.button_cancel

        init {
            itemView.setOnClickListener { v: View ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(snapshots.getSnapshot(position), position)
                }
            }

            cancelButton.setOnClickListener {
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
