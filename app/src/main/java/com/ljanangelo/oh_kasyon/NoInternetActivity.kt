package com.ljanangelo.oh_kasyon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_no_internet.*

class NoInternetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_internet)

        no_internet_refresh.setOnRefreshListener {
            checkConnection()
        }
    }

    private fun checkConnection() {
        no_internet_refresh.isRefreshing = false
        if(internetIsConnected()) {
            val intent = Intent(this, MainScreen::class.java)
            startActivity(intent)
            finish()
        }
    }

}