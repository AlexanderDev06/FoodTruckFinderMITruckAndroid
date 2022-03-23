package com.foodtruckfindermi.truck

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationServices.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_truck.*
import kotlinx.coroutines.runBlocking

class TruckActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var lat: Double = 0.0
    var lon: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_truck)

        val openButton = findViewById<Button>(R.id.openButton)
        val email = intent.getStringExtra("email")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        openButton.setOnClickListener {
            if (email != null) {
                openTruck(email, fusedLocationClient)
            }

        }
    }


    private fun openTruck(email: String?, fusedLocationClient: FusedLocationProviderClient) {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
                )
            }
            return
        }
        val cancellationToken = CancellationTokenSource().token

        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationToken)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude
                }
            }

        Log.i("Lat", lat.toString())
        Log.i("Lon", lon.toString())

        runBlocking {
            val (_request, _response, result) = Fuel.post("http://foodtruckfindermi.com/open-truck", listOf("email" to email, "lat" to lat.toString(), "lon" to lon.toString()))
                .awaitStringResponseResult()

            result.fold({ data ->

                if (data == "opened") {
                    val snackbar = Snackbar.make(
                        openButton, "Opened truck at: ${lat} ${lon}",
                        Snackbar.LENGTH_SHORT
                    ).setAction("Action", null)
                    snackbar.show()

                    openButton.text = "Close Truck"


                } else if (data == "closed") {
                    val snackbar = Snackbar.make(
                        openButton, "Truck Closed",
                        Snackbar.LENGTH_SHORT
                    ).setAction("Action", null)
                    snackbar.show()

                    openButton.text = "Open Truck"

                }


            }, {error -> Log.e("http", "${error}")})

        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

}