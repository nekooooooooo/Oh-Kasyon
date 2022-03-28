package com.ljanangelo.oh_kasyon

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import hideKeyboard
import kotlinx.android.synthetic.main.activity_events_place_info.*
import kotlinx.android.synthetic.main.activity_reserve.*
import kotlinx.android.synthetic.main.activity_reserve.button_back
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.item_history.*
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ReserveActivity : AppCompatActivity() {

    private val key = "retrieved_bid"
    private var bid: String = ""
    private var uid: String = ""

    private val TAG = "ReserveActivity"

    private lateinit var startCalendar: Calendar
    private lateinit var endCalender: Calendar

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserve)

        uid = currentUser?.uid.toString()

        var fullName = ""
        var firstName : String
        var middleName : String
        var lastName : String
        var business_name = ""
        var minCapacity = 1
        var maxCapacity: Int

        custom_time.visibility = View.GONE
        hours_time.visibility = View.GONE
        half_day_time.visibility = View.GONE
        edit_text_time.visibility = View.GONE

        edit_text_hours.setText("0")

        val usersRef = db.collection("users").document(uid)
        usersRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        firstName = document.getString("first_name").toString()
                        lastName = document.getString("last_name").toString()
                        middleName = document.getString("middle_name").toString()
                        fullName = if(middleName.isBlank()) "$firstName $lastName" else "$firstName $middleName $lastName"
                    } else {
                        toast("User info not found!")
                    }
                    edit_text_name.setText(fullName)
                }


        startCalendar = Calendar.getInstance()
        endCalender = Calendar.getInstance()
        val picker = DatePickerDialog(this)
        picker.datePicker.minDate = System.currentTimeMillis() - 1000
        picker.datePicker.maxDate = startCalendar.let {
            it.add(Calendar.MONTH, 2)
            it.timeInMillis
        }


        var startDate: String
        var endDate: String

        val bundle = intent.extras
        if(bundle != null){
            bid = bundle.getString(key).toString()
        }

        val businessRef = db.collection("businesses").document(bid)
        businessRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    business_name = document.getString("business_name").toString()
                    maxCapacity = (document.getDouble("business_capacity") as Double).toInt()
                    Log.d(TAG, "Max Capacity => $maxCapacity")

                    edit_text_guests.filters = arrayOf<InputFilter>(MinMaxFilter(minCapacity.toString(), maxCapacity.toString()))

                    text_max_capacity.text = "Max Capacity: $maxCapacity"
                }
            }

        val menusRef = businessRef.collection("menus")
        menusRef.orderBy("name")
                .get()
                .addOnSuccessListener { documents ->

                    if(documents.isEmpty) {
                        spinner_menu.visibility = View.GONE
                        text_menu.visibility = View.GONE
                        return@addOnSuccessListener
                    }

                    val menus: MutableList<String> = ArrayList()

                    for(document in documents) {
                        val name = document.getString("name").toString()
                        val price = document.getDouble("price")
                        val priceFormatted = String.format("%.2f", price)
                        menus.add("$name - PHP$priceFormatted/head")
                    }

                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, menus)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner_menu.adapter = adapter

                }


        button_view_menus.setOnClickListener {
            val intent = Intent(this, MenusActivity::class.java)
            intent.putExtra("retrieved_bid", bid)
            startActivity(intent)
        }

        setWarnings()

        // Check if email is valid
        edit_text_guests.afterTextChanged {
            /*val guests = when {
                edit_text_guests.text.toString().isEmpty() -> 0
                else -> Integer.parseInt(edit_text_guests.text.toString())
            }
            if(guests <= 0) {
                edit_text_guests.error = "Invalid Number"
            }*/

            if(spinner_menu.visibility != View.GONE) {
                val selectedMenu = spinner_menu.selectedItem.toString()
                val splitSelected = selectedMenu.split(" - ").toTypedArray()
                val name = splitSelected[0]

                val priceSplitSelected = splitSelected[1].split("PHP", "/head").toTypedArray()
                val price = priceSplitSelected[1].toDouble()
                val guests = if (edit_text_guests.text.toString().isNotEmpty()) edit_text_guests.text.toString().toDouble() else 0.0

                val total = guests * price

                val format = NumberFormat.getCurrencyInstance()
                format.maximumFractionDigits = 2
                format.currency = Currency.getInstance("PHP")

                edit_text_total.setText(format.format(total))
                text_debug.text = total.toString()
            }

            if (edit_text_guests.text.toString().isEmpty()) {
                edit_text_guests.error = "No. of Guests can't be empty!"
            }
        }

        edit_text_date.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (edit_text_date.text.toString().isEmpty()) {
                    edit_text_date.error = "Date can't be empty!"
                } else {
                    edit_text_date.error = null
                }
            }
        })

        edit_text_time.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (edit_text_time.text.toString().isEmpty()) {
                    edit_text_time.error = "Time can't be empty!"
                } else {
                    edit_text_time.error = null
                }
            }

        })

        edit_text_type.afterTextChanged {
            if(edit_text_type.text.toString().isEmpty()) {
                edit_text_type.error = "Theme can't be empty!"
            }
        }

        edit_text_theme.afterTextChanged {
            if(edit_text_theme.text.toString().isEmpty()) {
                edit_text_theme.error = "Theme can't be empty!"
            }
        }

        button_submit_reservation.setOnClickListener {
            startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(startCalendar.time)
            endDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(endCalender.time)
            val date = edit_text_date.text.toString().trim()
            val time = edit_text_time.text.toString().trim()
            val fullname = edit_text_name.text.toString().trim()
            // val type = edit_text_type.text.toString().trim()
            val theme = edit_text_theme.text.toString().trim()
            val type = when (val selectedItem = spinner_type.selectedItem.toString()) {
                "Other" -> "Other: " + edit_type_specify.text.toString().trim()
                else -> selectedItem
            }
            val menu = if(spinner_menu.selectedItem.toString() != "") {
                val selectedMenu = spinner_menu.selectedItem.toString()
                val splitSelected = selectedMenu.split(" - ").toTypedArray()
                splitSelected[0]
            } else {
                "No Menu Selected"
            }
            val rawPrice = edit_text_total.text.toString().trim()
            val splitSelected = rawPrice.split("PHP").toTypedArray()
            val splitComma = splitSelected[1].split(",").toTypedArray()
            Log.d(TAG, "$splitComma")
            val removedComma  = splitComma[0] + splitComma[1]
            val price = removedComma.toDouble()
            val time_preset = when (val selectedItem = spinner_time.selectedItem.toString()) {
                "Half Day" -> "Half Day: " + spinner_half_day.selectedItem.toString()
                else -> selectedItem
            }

            hideKeyboard()

            if (edit_text_guests.text.toString().isEmpty()) {
                edit_text_guests.error = "No. of Guests can't be empty!"
                edit_text_guests.requestFocus()
                return@setOnClickListener
            }

            if (date.isEmpty()) {
                edit_text_date.error = "Date can't be empty!"
                edit_text_date.requestFocus()
                return@setOnClickListener
            }

           /* if (time.isEmpty()) {
                edit_text_time.error = "Time can't be empty!"
                edit_text_time.requestFocus()
                return@setOnClickListener
            }*/

            /*if (type.isEmpty()) {
                edit_text_type.error = "Type can't be empty!"
                edit_text_type.requestFocus()
                return@setOnClickListener
            }*/

            if (type.isEmpty()) {
                edit_type_specify.error = "Type can't be empty!"
                edit_type_specify.requestFocus()
                return@setOnClickListener
            }

            if (theme.isEmpty()) {
                edit_text_theme.error = "Theme can't be empty!"
                edit_text_theme.requestFocus()
                return@setOnClickListener
            }

            val date_of_reservation = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Calendar.getInstance().time)
            val guests = if(edit_text_guests.text.toString() != "0") Integer.parseInt(edit_text_guests.text.toString()) else 0

            submitReservation(
                guests,
                startDate,
                endDate,
                fullname,
                menu,
                type,
                theme,
                business_name,
                date_of_reservation,
                price,
                time_preset
            )

        }

        button_back.setOnClickListener {
            finish()
        }

        edit_text_date.setOnClickListener {
            picker.setOnDateSetListener { _, year, month, day ->
                startCalendar[Calendar.YEAR] = year
                startCalendar[Calendar.MONTH] = month
                startCalendar[Calendar.DAY_OF_MONTH] = day
                endCalender[Calendar.YEAR] = year
                endCalender[Calendar.MONTH] = month
                endCalender[Calendar.DAY_OF_MONTH] = day
                val myFormat = "MM/dd/yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                edit_text_date.setText(sdf.format(startCalendar.time))
            }
            picker.show()
            /*picker = DatePickerDialog(
                this,
                { datePicker: DatePicker, year: Int, month: Int, day: Int ->
                    calendar[Calendar.YEAR] = year
                    calendar[Calendar.MONTH] = month
                    calendar[Calendar.DAY_OF_MONTH] = day
                    val myFormat = "MM/dd/yyyy"
                    val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                    edit_text_date.setText(sdf.format(calendar.time))
                    //edit_text_date.setText("${month+1}/$day/$year")
                },
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )
            picker.show()*/
        }

        // Show edit text depending on the selected gender
        spinner_type?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if(selectedItem == "Other") {
                    val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    type_divider.visibility = View.VISIBLE
                    edit_type_specify.visibility = View.VISIBLE
                    spinner_type.setBackgroundResource(R.drawable.contact_prefix_background)
                    spinner_type.layoutParams = layoutParams
                }
                else {
                    val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    type_divider.visibility = View.GONE
                    edit_type_specify.visibility = View.GONE
                    spinner_type.setBackgroundResource(R.drawable.edit_text_background)
                    spinner_type.layoutParams = layoutParams
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        spinner_menu?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedMenu = spinner_menu.selectedItem.toString()
                val splitSelected = selectedMenu.split(" - ").toTypedArray()
                val name = splitSelected[0]

                val priceSplitSelected = splitSelected[1].split("PHP", "/head").toTypedArray()
                val price = priceSplitSelected[1].toDouble()
                val guests = if(edit_text_guests.text.toString().isNotEmpty()) edit_text_guests.text.toString().toDouble() else 0.0

                val total = guests * price

                val format = NumberFormat.getCurrencyInstance()
                format.maximumFractionDigits = 2
                format.currency = Currency.getInstance("PHP")

                edit_text_total.setText(format.format(total))
                text_debug.text = total.toString()

                Log.d(TAG, "$name $price")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        spinner_time?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                when(selectedItem) {
                    "Half Day" -> {
                        custom_time.visibility = View.GONE
                        hours_time.visibility = View.GONE
                        half_day_time.visibility = View.VISIBLE
                    }
                    "Hours" -> {
                        custom_time.visibility = View.GONE
                        hours_time.visibility = View.VISIBLE
                        half_day_time.visibility = View.GONE
                    }
                    "Custom" -> {
                        custom_time.visibility = View.VISIBLE
                        hours_time.visibility = View.GONE
                        half_day_time.visibility = View.GONE
                    }
                    else -> {
                        custom_time.visibility = View.GONE
                        hours_time.visibility = View.GONE
                        half_day_time.visibility = View.GONE
                        startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                        startCalendar.set(Calendar.MINUTE, 0)
                        startCalendar.set(Calendar.SECOND, 0)
                        endCalender.set(Calendar.HOUR_OF_DAY, 24)
                        endCalender.set(Calendar.MINUTE, 0)
                        startCalendar.set(Calendar.SECOND, 0)

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        spinner_half_day?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(parent?.getItemAtPosition(position).toString()) {
                    "AM" -> {
                        toast("AM")
                        startCalendar.set(Calendar.HOUR_OF_DAY, 8)
                        startCalendar.set(Calendar.MINUTE, 0)
                        startCalendar.set(Calendar.SECOND, 0)
                        endCalender.set(Calendar.HOUR_OF_DAY, 12)
                        endCalender.set(Calendar.MINUTE, 0)
                    }
                    "PM" -> {
                        toast("PM")
                        startCalendar.set(Calendar.HOUR_OF_DAY, 1)
                        startCalendar.set(Calendar.MINUTE, 0)
                        startCalendar.set(Calendar.SECOND, 0)
                        endCalender.set(Calendar.HOUR_OF_DAY, 5)
                        endCalender.set(Calendar.MINUTE, 0)
                    }
                    else -> {
                        startCalendar.set(Calendar.HOUR_OF_DAY, 8)
                        startCalendar.set(Calendar.MINUTE, 0)
                        startCalendar.set(Calendar.SECOND, 0)
                        endCalender.set(Calendar.HOUR_OF_DAY, 12)
                        endCalender.set(Calendar.MINUTE, 0)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                startCalendar.set(Calendar.HOUR_OF_DAY, 8)
                startCalendar.set(Calendar.MINUTE, 0)
                startCalendar.set(Calendar.SECOND, 0)
                endCalender.set(Calendar.HOUR_OF_DAY, 12)
                endCalender.set(Calendar.MINUTE, 0)
                endCalender.set(Calendar.SECOND, 0)
            }

        }


        val timeSetListener = TimePickerDialog.OnTimeSetListener { view: TimePicker?, hourOfDay: Int, minute: Int ->
            startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            startCalendar.set(Calendar.MINUTE, minute)
            val isPM = hourOfDay >= 12
            edit_text_time.setText(
                String.format(
                    "%02d:%02d %s",
                    if (hourOfDay === 12 || hourOfDay === 0) 12 else hourOfDay % 12,
                    minute,
                    if (isPM) "PM" else "AM"
                )
            )
        }

        val customTimeStartListener = TimePickerDialog.OnTimeSetListener { view: TimePicker?, hourOfDay: Int, minute: Int ->
            startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            startCalendar.set(Calendar.MINUTE, minute)
            val isPM = hourOfDay >= 12
            custom_text_start_time.setText(
                String.format(
                    "%02d:%02d %s",
                    if (hourOfDay === 12 || hourOfDay === 0) 12 else hourOfDay % 12,
                    minute,
                    if (isPM) "PM" else "AM"
                )
            )
        }

        val customTimeEndListener = TimePickerDialog.OnTimeSetListener { view: TimePicker?, hourOfDay: Int, minute: Int ->
            endCalender.set(Calendar.HOUR_OF_DAY, hourOfDay)
            endCalender.set(Calendar.MINUTE, minute)
            val isPM = hourOfDay >= 12
            custom_text_end_time.setText(
                String.format(
                    "%02d:%02d %s",
                    if (hourOfDay === 12 || hourOfDay === 0) 12 else hourOfDay % 12,
                    minute,
                    if (isPM) "PM" else "AM"
                )
            )
        }

        val hoursTimeListener = TimePickerDialog.OnTimeSetListener { view: TimePicker?, hourOfDay: Int, minute: Int ->
            val hours = edit_text_hours.text.toString().toInt()
            startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            startCalendar.set(Calendar.MINUTE, minute)
            endCalender.set(Calendar.HOUR_OF_DAY, hourOfDay + hours)
            endCalender.set(Calendar.MINUTE, minute)
            val isPM = hourOfDay >= 12
            hours_text_start_time.setText(
                String.format(
                    "%02d:%02d %s",
                    if (hourOfDay === 12 || hourOfDay === 0) 12 else hourOfDay % 12,
                    minute,
                    if (isPM) "PM" else "AM"
                )
            )
            val endTime = hourOfDay + hours
            hours_text_edit_time.text = String.format("%02d:%02d %s", if (endTime === 12 || endTime === 0) 12 else endTime % 12, minute, if (isPM) "PM" else "AM")
        }

        edit_text_time.setOnClickListener {
            TimePickerDialog(
                this,
                timeSetListener,
                startCalendar.get(Calendar.HOUR_OF_DAY),
                startCalendar.get(
                    Calendar.MINUTE
                ), false
            ).show()
        }

        custom_text_start_time.setOnClickListener {
            TimePickerDialog(
                this,
                customTimeStartListener,
                startCalendar.get(Calendar.HOUR_OF_DAY),
                startCalendar.get(
                    Calendar.MINUTE
                ), false
            ).show()
        }

        custom_text_end_time.setOnClickListener {
            TimePickerDialog(
                this,
                customTimeEndListener,
                endCalender.get(Calendar.HOUR_OF_DAY),
                endCalender.get(
                    Calendar.MINUTE
                ), false
            ).show()
        }

        hours_text_start_time.setOnClickListener {
            TimePickerDialog(
                this,
                hoursTimeListener,
                startCalendar.get(Calendar.HOUR_OF_DAY),
                startCalendar.get(
                    Calendar.MINUTE
                ), false
            ).show()
        }

        button_time_test.setOnClickListener {
            startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(startCalendar.time)
            endDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(endCalender.time)
            toast("$startDate $endDate")
        }

    }

    private fun submitReservation(
            guests: Int,
            startTime: String,
            endTime: String,
            fullName: String,
            menu: String,
            type: String,
            theme: String,
            business_name: String,
            date_of_reservation: String,
            price: Double,
            time_preset: String
    ) {
        val dialog = ProgressDialog.show(this, "Sending",
                "Loading. Please wait...", true)


        val docData = hashMapOf(
                "no_of_guests" to guests,
                "timestamp" to getDateFromString(startTime),
                "start_time" to getDateFromString(startTime),
                "end_time" to getDateFromString(endTime),
                "fullname" to fullName,
                "menu" to menu,
                "type" to type,
                "theme" to theme,
                "uid" to uid,
                "bid" to bid,
                "business_name" to business_name,
                "status" to "Pending",
                "date_of_reservation" to getDateFromString(date_of_reservation),
                "price" to price,
                "time_preset" to time_preset
        )

        db.collection("reservations").add(docData)
                .addOnSuccessListener {
                    dialog.dismiss()
                    AlertDialog.Builder(this).apply {
                        setTitle("Reservation has been made!")
                        setPositiveButton("Okay") { _, _ ->
                            finish()
                        }
                        setOnCancelListener {
                            finish()
                        }
                    }.create().show()
                    toast("Added to Firestore Database with ID: ${it.id}!")
                }
                .addOnFailureListener {
                    dialog.dismiss()
                    Log.w("AppDebug", "Error writing document", it)
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

    private fun warningMessage(warning: String) {
        when(warning) {
            "guests" -> edit_text_guests.error = "No. of Guests can't be empty!"
            "date" -> edit_text_date.error = "Date can't be empty!"
            "time" -> edit_text_time.error = "Time can't be empty!"
            "theme" -> edit_text_theme.error = "Theme can't be empty!"
            "type" -> edit_text_type.error = "Type can't be empty!"
        }
    }

    private fun setWarnings() {
        if(edit_text_guests.text.toString().isEmpty()) {
            edit_text_guests.error = "No. of Guests can't be empty!"
        }

        if(edit_text_date.text.toString().isEmpty()) {
            edit_text_date.error = "Date can't be empty!"
        }

        if(edit_text_time.text.toString().isEmpty()) {
            edit_text_time.error = "Time can't be empty!"
        }

        if(edit_text_theme.text.toString().isEmpty()) {
            edit_text_theme.error = "Theme can't be empty!"
        }

        if(edit_text_type.text.toString().isEmpty()) {
            edit_text_type.error = "Type can't be empty!"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        startCalendar.clear()
    }

    inner class MinMaxFilter() : InputFilter {
        private var intMin: Int = 0
        private var intMax: Int = 0
        constructor(minValue: String, maxValue: String) : this() {
            this.intMin = Integer.parseInt(minValue)
            this.intMax = Integer.parseInt(maxValue)
        }
        override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dStart: Int,
                dEnd: Int
        ): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(intMin, intMax, input)) {
                    return null
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }
        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }
}

