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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Settings

class MainActivity : ComponentActivity(), LocationListener {

    val viewModel: MainViewModel by viewModels()

    val styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright")

    @OptIn(ExperimentalMaterial3Api::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        setContent {
            MapAppTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            NavigationDrawerItem(
                                label = { Text("Map") },
                                selected = false,
                                onClick = {
                                    navController.navigate("mainScreen")
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                            NavigationDrawerItem(
                                label = { Text("Settings") },
                                selected = false,
                                onClick = {
                                    navController.navigate("settingsScreen")
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Map App") },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                if (drawerState.isClosed) {
                                                    drawerState.open()
                                                } else {
                                                    drawerState.close()
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Menu,
                                            contentDescription = "Menu"
                                        )
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = false,
                                    onClick = {
                                        navController.navigate("mainScreen")
                                    },
                                    icon = {
                                        Icon(
                                            painter = painterResource(R.drawable.map_24dp),
                                            contentDescription = "Map"
                                        )
                                    },
                                    label = { Text("Home") }
                                )

                                NavigationBarItem(
                                    selected = false,
                                    onClick = {
                                        navController.navigate("settingsScreen")
                                    },
                                    icon = {
                                        Icon(
                                            Icons.Filled.Settings,
                                            contentDescription = "Settings"
                                        )
                                    },
                                    label = { Text("Settings") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "mainScreen",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("mainScreen") {
                                MainScreenComposable()
                            }
                            composable("settingsScreen") {
                                SettingsComposable(viewModel) {
                                    navController.popBackStack()
                                }
                            }
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
    fun MainScreenComposable() {
        var pos: LatLng by remember { mutableStateOf(viewModel.latLng) }
        var zoomLevel: Double by remember { mutableStateOf(viewModel.zoom) }

        viewModel.latLngLiveData.observe(this) {
            pos = it
        }

        viewModel.zoomLiveData.observe(this) {
            zoomLevel = it
        }

        Column {
            }

            MapLibre(
                modifier = Modifier.fillMaxWidth(),
                styleBuilder = styleBuilder,
                cameraPosition = CameraPosition(
                    target = pos,
                    zoom = zoomLevel
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

        var latText by remember { mutableStateOf(viewModel.latLng.latitude.toString()) }
        var lngText by remember { mutableStateOf(viewModel.latLng.longitude.toString()) }
        var zoomText by remember { mutableStateOf(viewModel.zoom.toString()) }
        Column {
            Row {
                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    value = latText,
                    onValueChange = { latText = it },
                    label = { Text("Enter Lat") }
                )

                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    value = lngText,
                    onValueChange = { lngText = it },
                    label = { Text("Enter Long") }
                )

                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    value = zoomText,
                    onValueChange = { zoomText = it },
                    label = { Text("Enter Zoom") }
                )
            }
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lng = lngText.toDoubleOrNull()
                    val zoom = zoomText.toDoubleOrNull()

                    if (lat != null && lng != null && zoom != null) {
                        viewModel.latLng = LatLng(lat, lng)
                        viewModel.zoom = zoom
                        returnToMainCallback()
                    }
                }
            ) {
                Text("Return")
            }



        }

    }

