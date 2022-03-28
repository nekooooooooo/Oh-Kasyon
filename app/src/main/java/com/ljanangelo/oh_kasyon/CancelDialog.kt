package com.ljanangelo.oh_kasyon

import android.app.AlertDialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.dialog_cancel_reservation.*
import kotlinx.android.synthetic.main.dialog_cancel_reservation.view.*

class CancelDialog: DialogFragment() {

    private val TAG = "CancelDialog"
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.dialog_cancel_reservation, container, false)

        val id = arguments?.getString("id").toString()



        view.button_close.setOnClickListener {
            dismiss()
        }

        view.button_confirm.setOnClickListener {
            Log.d(TAG, id)

            val reason = view.edit_text_reason.text.toString().trim()
            if(reason.isEmpty()) {
                edit_text_reason.error = "Reason Required!"
                edit_text_reason.requestFocus()
                return@setOnClickListener
            }

            val dialog = ProgressDialog.show(
                    context, "Sending",
                    "Loading. Please wait...", true
            )

            edit_text_reason.setText("")

            db.collection("reservations")
                    .document(id)
                    .update(mapOf(
                            "status" to "Cancelled",
                            "cancellation_reason" to reason))
                    .addOnCompleteListener { task ->
                        dialog.dismiss()
                        if(task.isSuccessful) {
                            AlertDialog.Builder(context).apply {
                                setTitle("Cancelation has been succesful!")
                                setPositiveButton("Okay") { _, _ ->
                                    dismiss()
                                }
                                setOnCancelListener {

                                }
                            }.create().show()
                        } else {
                            task.exception?.let { e ->
                                context?.toast(e.toString())
                            }

                        }
                    }

            //context?.toast("Id: $id Reason: $reason")
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onStart() {
        super.onStart()
    }



}