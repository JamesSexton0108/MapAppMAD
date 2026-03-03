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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.ComposeNodeLifecycleCallback
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.MapLibre
import org.ramani.compose.CameraPosition
import org.ramani.compose.Circle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity(), LocationListener {

    val viewModel: MainViewModel by viewModels()

    val styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        setContent {
            MapAppTheme {
                val navController = rememberNavController()

                Surface {
                    NavHost(navController = navController, startDestination = "mainScreen") {
                        composable("mainScreen") {
                            MainScreenComposable(settingsCallback = {
                                navController.navigate("settingsScreen")
                            })
                        }
                        composable("settingsScreen") {
                            SettingsComposable(viewModel, returnToMainCallback = {
                                //navController.navigate("MainScreenComposable") { popUpTo("MainScreenComposable") }
                                navController.popBackStack()
                            })
                        }
                    }

                }
            }


        }
    }

    fun checkPermissions() {
        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
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
        var latLngVal: LatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
        viewModel.latLngLiveData.observe(this) {
            latLngVal = it
        }
        Text("${latLngVal}")
    }

    @Composable
    fun MainScreenComposable(settingsCallback: () -> Unit) {
        var pos: LatLng by remember { mutableStateOf(LatLng(50.9079, -1.4015)) }
        viewModel.latLngLiveData.observe(this) {
            pos = it
        }
        Column {
            Button(onClick = { settingsCallback() }) {
                Text("Settings")
            }

            MapLibre(
                modifier = Modifier.fillMaxWidth(),
                styleBuilder = styleBuilder,
                cameraPosition = CameraPosition(
                    target = pos,
                    zoom = 14.0
                )
            )
            {
                Circle(
                    center = LatLng(50.9079, -1.4015),
                    radius = 100.0f,
                    color = "Red"
                )
            }
        }

    }

    @Composable
    fun SettingsComposable(viewModel: MainViewModel, returnToMainCallback: () -> Unit) {

        var lat: Double by remember { mutableStateOf(0.0) }
        var lang: Double by remember { mutableStateOf(0.0) }
        var zoom: Double by remember { mutableStateOf(0.0) }
        var latLng: LatLng by remember { mutableStateOf(LatLng(lat,lang)) }
        Column {
            Row {
                TextField(modifier = Modifier.weight(1.0f).padding(8.dp), value = lat.toString(), onValueChange = {lat = it.toDouble()}, label = {Text("Enter Lat")})
                TextField(modifier = Modifier.weight(1.0f).padding(8.dp), value = lang.toString(), onValueChange = {lang = it.toDouble()}, label = {Text("Enter Lang")})
                TextField(modifier = Modifier.weight(1.0f).padding(8.dp), value = zoom.toString(), onValueChange = {zoom = it.toDouble()}, label = {Text("Enter Zoom")})

            }
            Button(onClick = { returnToMainCallback()  viewModel.latLng = latLng}) {
                Text("Return")
        }


        }

    }
}
