package com.ljanangelo.oh_kasyon.model

import com.google.firebase.firestore.GeoPoint

class EventsPlace{
    val business_name: String = ""
    val business_address: String = ""
    val business_rating: Double = 0.0
    val business_capacity: Int = 0
    val business_geopoint: GeoPoint? = null
    val business_img_url: String = ""
    val business_360_img_url: String = ""

    constructor()

}
