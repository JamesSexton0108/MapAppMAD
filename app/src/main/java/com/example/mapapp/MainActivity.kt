package com.example.mapapp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mapapp.ui.theme.MapAppTheme
import android.Manifest
import android.location.Location
import android.location.LocationListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.MapLibre
import org.ramani.compose.CameraPosition

class MainActivity : ComponentActivity(), LocationListener {

    val viewModel : MainViewModel by viewModels()

    val styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MapAppTheme {
                checkPermissions()
                Column{
                    UI()


                }


            }
        }
    }

    fun checkPermissions() {
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            if(isGranted) {
                startGPS()
            } else {
                Toast.makeText(this, "GPS permission not granted", Toast.LENGTH_LONG).show()
            }
        }
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET))
    }

    @SuppressLint("MissingPermission")
    fun startGPS() {
        val mgr = getSystemService(LOCATION_SERVICE) as LocationManager
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

    }

    override fun onLocationChanged(location: Location) {
        Toast.makeText(this, "Latitude: ${location.latitude}, Longitude: ${location.longitude}", Toast.LENGTH_SHORT).show()
        viewModel.latLng = LatLng(location.latitude, location.longitude)
    }
    @Composable
    fun UI() {
        val latLngVal = remember { mutableStateOf(LatLng(0.0,0.0))}
        viewModel.latLngLiveData.observe(this) {
            latLngVal.value = it
        }
        Text("${latLngVal.value}")
    }

}
