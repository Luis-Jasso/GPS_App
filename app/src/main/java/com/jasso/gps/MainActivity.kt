package com.jasso.gps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.jasso.gps.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

const val REQUEST_CODE_LOCATION = 1

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var alertDialog: AlertDialog
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        binding.button.setOnClickListener {
            getLocation()
        }

    }


    private fun getLocation() {
        if (checkPermission()) {
            // el permiso fue otorgado
            if (isLocationEnabled()) {
                //Obtener la ubicacion
                CoroutineScope(Dispatchers.IO).launch {


                    mFusedLocationProviderClient.lastLocation.addOnCompleteListener(this@MainActivity) { task ->
                        val location = task.result
                        if (location != null) {

                            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                            val list: List<Address> =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)

                            runOnUiThread {

                                Toast.makeText(
                                    this@MainActivity,
                                    "mostrando datos en la interfaz",
                                    Toast.LENGTH_SHORT
                                ).show()

                                binding.tvCoordenadas.text =
                                    resources.getString(
                                        R.string.location_text,
                                        list[0].latitude,
                                        list[0].longitude,
                                        location.time
                                    )

                                binding.tvAddress.text =
                                    resources.getString(
                                        R.string.address_text,
                                        list[0].countryName,
                                        list[0].adminArea,
                                        list[0].postalCode,
                                        list[0].getAddressLine(0)
                                    )
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "No se puede obtener la ubicación, hay problemas...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }


            } else {
                //la ubicacion debe ser activada, mostrar aviso al usuario
                alertDialog = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.title_aviso))
                    .setMessage(getString(R.string.content_aviso))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .setNegativeButton(getString(R.string.No)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                alertDialog.show()
            }
        } else {
            requestPermission()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), REQUEST_CODE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                }
            }
            else -> {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}
