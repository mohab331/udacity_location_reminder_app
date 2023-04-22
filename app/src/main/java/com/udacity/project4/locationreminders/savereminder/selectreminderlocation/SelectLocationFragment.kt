package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use kotlin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private var marker: Marker? = null

    private var selectedPOI: PointOfInterest? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var reminderSelectedLocationStr: String = ""
    private var latitude = 0.0
    private var longitude = 0.0



    companion object {
        private const val REQUEST_PERMISSION_LOCATION_CODE = 1
        private const val DEFAULT_LATITUDE = 43.785294
        private const val DEFAULT_LONGITUDE = -110.698560
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = getFusedLocationProviderClient()

        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapView = childFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment
        mapView.getMapAsync(this)
    }

    private fun getFusedLocationProviderClient(): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle()
        enableUserLocation()
        setMapLongClick()
        setPoiClick()
    }

    private fun setMapStyle() {
        try {
            val success = map.setMapStyle(

                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )

            )
            if (!success) {
                Log.e(TAG, "Map Styling Failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Error: ", e)
        }
    }

    private fun setMapLongClick() {
        map.setOnMapLongClickListener { latLng ->
            latitude = latLng.latitude
            longitude = latLng.longitude
            val titleLat = "%.2f".format(latLng.latitude)
            val titleLng = "%.2f".format(latLng.longitude)
            reminderSelectedLocationStr = "$titleLat,$titleLng"

            selectedPOI =
                PointOfInterest(latLng, reminderSelectedLocationStr, reminderSelectedLocationStr)

            map.clear()
            marker?.remove()
            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(reminderSelectedLocationStr)
            )
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    private fun setPoiClick() {
        map.setOnPoiClickListener { poi ->
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            reminderSelectedLocationStr = poi.name
            selectedPOI = poi
            latitude = poi.latLng.latitude
            longitude = poi.latLng.longitude
            poiMarker?.showInfoWindow()
            binding.saveButton.visibility = View.VISIBLE
        }
    }





    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationClient = getFusedLocationProviderClient()
        fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation ->
            if (lastKnownLocation != null) {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            lastKnownLocation.latitude,
                            lastKnownLocation.longitude
                        ), 15f
                    )
                )
            } else {
                Log.d(TAG, "getUserLocation: IN fail")
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE),
                        15f
                    )
                )
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        return (
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                        == PackageManager.PERMISSION_GRANTED)
    }
    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            getUserLocation()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_LOCATION_CODE,
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && (grantResults.contains(PackageManager.PERMISSION_GRANTED))) {
            enableUserLocation()
        } else {
            showRationale()
        }
    }


    private fun showRationale() {
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(requireActivity())
                .setTitle("Location Permission")
                .setMessage(R.string.permission_denied_explanation)
                .setPositiveButton("OK") { _, _ ->
                    enableUserLocation()
                }
                .create()
                .show()

        } else {
            enableUserLocation()
        }
    }


    private fun onLocationSelected() {
        _viewModel.reminderSelectedLocationStr.value = reminderSelectedLocationStr
        _viewModel.selectedPOI.value = selectedPOI
        _viewModel.latitude.value = latitude
        _viewModel.longitude.value = longitude
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }

        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }

        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        else -> super.onOptionsItemSelected(item)
    }


}
