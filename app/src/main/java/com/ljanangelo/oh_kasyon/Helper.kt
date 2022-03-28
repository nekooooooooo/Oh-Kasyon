package com.ljanangelo.oh_kasyon

import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast

fun Context.toast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

/*fun Context.verifyEmail(){
    val intent = Intent(this, VerifyActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}

fun Context.login(){
    val intent = Intent(this, MainMenuActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}*/

fun internetIsConnected(): Boolean {
    return try {
        val command = "ping -c 1 google.com"
        Runtime.getRuntime().exec(command).waitFor() == 0
    } catch (e: Exception) {
        false
    }
}

fun Context.login(){
    val intent = Intent(this, MainScreen::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}

fun Context.verify(){
    val intent = Intent(this, VerifyActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}

fun Context.logout() {
    val intent = Intent(this, LogInActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
}

fun Context.openEventsInfo(business_id: String, rating: Double) {
    val intent = Intent(this, EventsPlaceInfoActivity::class.java)
    intent.putExtra("retrieved_bid", business_id)
    intent.putExtra("retrieved_rating", rating)
    startActivity(intent)
}

fun Context.openChats(cid: String, bid: String, baid: String){
    val intent = Intent(this, ChatActivity::class.java)
    intent.putExtra("retrieved_cid", cid)
    intent.putExtra("retrieved_bid", bid)
    intent.putExtra("retrieved_baid", baid)
    startActivity(intent)
}

fun Context.openReview(rid: String) {
    val intent = Intent(this, ReviewActivity::class.java)
    intent.putExtra("retrieved_rid", rid)
    startActivity(intent)
}

fun Context.openEditReview(bid: String, rid: String) {
    val intent = Intent(this, EditReviewActivity::class.java)
    intent.putExtra("retrieved_bid", bid)
    intent.putExtra("retrieved_rid", rid)
    startActivity(intent)
}

fun Context.openMenu(bid: String, mid: String) {
    val intent = Intent(this, FoodsActivity::class.java)
    intent.putExtra("retrieved_bid", bid)
    intent.putExtra("retrieved_mid", mid)
    startActivity(intent)
}

fun Context.openFood(fid: String, bid: String, mid: String) {
    val intent = Intent(this, FoodActivity::class.java)
    intent.putExtra("retrieved_fid", fid)
    intent.putExtra("retrieved_bid", bid)
    intent.putExtra("retrieved_mid", mid)
    startActivity(intent)
}

fun Context.openUserProfile() {
    val intent = Intent(this, UserProfileActivity::class.java)
    startActivity(intent)
}

fun Context.openReservations() {
    val intent = Intent(this, ReservationsActivity::class.java)
    startActivity(intent)
}

fun Context.openProfileEdit() {
    val intent = Intent(this, UpdateUserActivity::class.java)
    startActivity(intent)
}

fun Context.openChangePassword() {
    val intent = Intent(this, ChangePasswordActivity::class.java)
    startActivity(intent)
}

fun Context.openChangeEmail() {
    val intent = Intent(this, ChangeEmailActivity::class.java)
    startActivity(intent)
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
