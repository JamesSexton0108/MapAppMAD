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
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.MapLibre
import org.ramani.compose.CameraPosition
import org.ramani.compose.Circle

class MainActivity : ComponentActivity(), LocationListener {

    val viewModel : MainViewModel by viewModels()

    val styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        setContent {
            MapAppTheme {
                val pos = remember {  mutableStateOf(LatLng(50.9079, -1.4015)) }
                viewModel.latLngLiveData.observe(this) {
                    pos.value = it
                }
                MapLibre(modifier = Modifier.fillMaxSize(),
                    styleBuilder = styleBuilder,
                    cameraPosition = CameraPosition(
                        target = pos.value,
                        zoom = 14.0)
                )
                {
                    Circle(center = LatLng(50.9079, -1.4015),
                        radius = 100.0f,
                        color = "Red")
                }
            }

        }
    }

    fun checkPermissions() {
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                startGPS()
            } else {
                Toast.makeText(this, "GPS permission not granted", Toast.LENGTH_LONG).show()
            }
        }
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    fun startGPS() {
        val mgr = getSystemService(LOCATION_SERVICE) as LocationManager
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

    }

    override fun onLocationChanged(location: Location) {
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
