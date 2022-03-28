package com.ljanangelo.oh_kasyon

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_map.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener  {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var name: String = ""
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mapboxMap: MapboxMap

    private var mapView: MapView? = null

    private val mapTilerKey = "Klgj5jnWlMSGtm8yTuuU"

    private val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=${mapTilerKey}"

    private val SOURCE_ID = "SOURCE_ID"
    private val ICON_ID = "ICON_ID"
    private val LAYER_ID = "LAYER_ID"

    private lateinit var alert: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = intent.extras

        if(bundle != null){
            longitude = bundle.getDouble("LONG")
            latitude = bundle.getDouble("LATI")
            name = bundle.getString("NAME").toString()
        }

        // Get the MapBox context
        Mapbox.getInstance(this, mapTilerKey)

        setContentView(R.layout.activity_map)


        // Create map view
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        button_find_me.setOnClickListener {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                toast("Centering Your Location")
                mapboxMap.locationComponent.apply {
                    cameraMode = CameraMode.TRACKING
                    zoomWhileTracking(15.0)
                }
            } else {
                val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
                alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                        .setCancelable(false)
                        .setPositiveButton("Goto Settings Page To Enable GPS") { dialog, id ->
                            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivity(callGPSSettingIntent)
                        }
                        .setNegativeButton("Cancel") { dialog, id ->
                            dialog.cancel()
                        }
                alert = alertDialogBuilder.create()
                alert.show()
            }
        }

        button_find_event_place.setOnClickListener {
            toast("Centering Events Place")
            val position = CameraPosition.Builder()
                .target(LatLng(latitude, longitude))
                .zoom(15.0)
                .build()
            val millisecondSpeed = 750
            mapboxMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(position),
                    millisecondSpeed
            )
        }

        button_back.setOnClickListener {
            finish()
        }

        button_get_direction.setOnClickListener {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                toast("Showing Both")
                val latLngBounds = LatLngBounds.Builder()
                        .include(LatLng(latitude, longitude))
                        .include(LatLng(mapboxMap.locationComponent.lastKnownLocation!!.latitude, mapboxMap.locationComponent.lastKnownLocation!!.longitude))
                        .build()
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 250))
            } else {
                val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
                alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                        .setCancelable(false)
                        .setPositiveButton("Goto Settings Page To Enable GPS") { dialog, id ->
                            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivity(callGPSSettingIntent)
                        }
                        .setNegativeButton("Cancel") { dialog, id ->
                            dialog.cancel()
                        }
                alert = alertDialogBuilder.create()
                alert.show()
            }


        }
        
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.orange))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(
                    this,
                    loadedMapStyle
            )
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.NONE

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }


    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        //overridePendingTransition(R.anim.expand_in, android.R.anim.fade_out)
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)

    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }



    override fun onMapReady(mapboxMap: MapboxMap) {

        this.mapboxMap = mapboxMap

        val symbolLayerIconFeatureList: MutableList<Feature> = ArrayList()
        symbolLayerIconFeatureList.add(
                Feature.fromGeometry(
                        Point.fromLngLat(longitude, latitude)
                )
        )

        mapboxMap.setStyle(
                Style.Builder().fromUri(styleUrl)
                        .withImage(
                                ICON_ID, BitmapFactory.decodeResource(
                                this.resources, R.drawable.mapbox_marker_icon_default
                        )
                        )
                        .withSource(
                                GeoJsonSource(
                                        SOURCE_ID,
                                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)
                                )
                        )
                        .withLayer(
                                SymbolLayer(LAYER_ID, SOURCE_ID)
                                        .withProperties(
                                                iconImage(ICON_ID),
                                                iconAllowOverlap(true),
                                                iconIgnorePlacement(true),
                                                textField(name),
                                                textColor(Color.BLACK),
                                                textTranslate(arrayOf(0f, -40f))
                                        )
                        )
        ) {
            mapboxMap.uiSettings.let { uiSettings ->
                uiSettings.setAttributionMargins(15, 0, 0, 15)
                uiSettings.areAllGesturesEnabled()
                uiSettings.isCompassEnabled = true
            }
            // Set the map view center
            mapboxMap.cameraPosition = CameraPosition.Builder()
                .target(LatLng(latitude, longitude))
                .zoom(15.0)
                .build()
            enableLocationComponent(it)
        }


    }

}
