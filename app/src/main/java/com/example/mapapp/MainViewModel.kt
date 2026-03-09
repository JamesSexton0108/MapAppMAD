package com.example.mapapp

import android.util.MutableDouble
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.maplibre.android.geometry.LatLng

class MainViewModel: ViewModel() {
    var latLng = LatLng(50.9079, -1.4015)
        set(newValue) {
            field = newValue
            latLngLiveData.value = newValue
        }
    var latLngLiveData = MutableLiveData<LatLng>()

    var zoom: Double = 14.0
        set(newValue) {
            field = newValue
            zoomLiveData.value = newValue
        }
    var zoomLiveData = MutableLiveData<Double>()
}