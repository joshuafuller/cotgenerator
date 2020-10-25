package com.jon.common.ui.location

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.jon.common.R
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getStringFromPair
import com.jon.common.repositories.GpsRepository
import com.jon.common.variants.Variant

class LocationFragment : Fragment(),
        SensorEventListener {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    private val gpsRepository = GpsRepository.getInstance()
    private val gpsConverter = GpsConverter()
    private var mostRecentLocation: Location? = null
    private lateinit var coordinateFormat: CoordinateFormat
    private lateinit var latitude: TextView
    private lateinit var longitude: TextView
    private lateinit var latLayout: View
    private lateinit var lonLayout: View
    private lateinit var mgrs: TextView
    private lateinit var coordinateFormatButton: Button

    private val compass by lazy { Compass(requireContext()) }
    private lateinit var compassDegrees: TextView
    private lateinit var compassDirection: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = View.inflate(context, R.layout.fragment_location, null)
        /* Lat/lon views */
        latitude = view.findViewById(R.id.coords_lat_degrees)
        longitude = view.findViewById(R.id.coords_lon_degrees)
        mgrs = view.findViewById(R.id.coords_mgrs)
        latLayout = view.findViewById(R.id.latitude_layout)
        lonLayout = view.findViewById(R.id.longitude_layout)
        initialiseCoordinateFormat()
        initialiseCoordinateFormatButton(view)
        observeGpsData()

        /* Compass views */
        compassDegrees = view.findViewById(R.id.compass_degrees)
        compassDirection = view.findViewById(R.id.compass_direction)
        return view
    }

    override fun onResume() {
        super.onResume()
        compass.registerListener(this)
    }

    override fun onPause() {
        super.onPause()
        compass.unregisterListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !compass.shouldRecalculate()) {
            return
        }
        compass.getCompassReading(event).also {
            compassDegrees.text = "%3.0f°".format(it.degrees)
            compassDirection.text = it.direction
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        /* No-op */
    }

    private fun initialiseCoordinateFormat() {
        coordinateFormat = CoordinateFormat.fromString(
                prefs.getStringFromPair(CommonPrefs.LOCATION_COORDINATE_FORMAT)
        )
        showCorrectCoordinateViews()
    }

    private fun initialiseCoordinateFormatButton(view: View) {
        coordinateFormatButton = view.findViewById(R.id.coord_format_button)
        coordinateFormatButton.setBackgroundColor(ContextCompat.getColor(requireContext(), Variant.getAccentColourId()))
        coordinateFormatButton.text = getString(R.string.location_format_button_placeholder, coordinateFormat.name)
        coordinateFormatButton.setOnClickListener {
            coordinateFormat = CoordinateFormat.getNext(coordinateFormat)
            coordinateFormatButton.text = getString(R.string.location_format_button_placeholder, coordinateFormat.name)
            convertAndDisplayCoordinates(mostRecentLocation)
            showCorrectCoordinateViews()
            prefs.edit()
                    .putString(CommonPrefs.LOCATION_COORDINATE_FORMAT.key, coordinateFormat.name)
                    .apply()
        }
    }

    private fun observeGpsData() {
        gpsRepository.getLocation().observe(viewLifecycleOwner) { location ->
            mostRecentLocation = location
            convertAndDisplayCoordinates(location)
        }
    }

    private fun convertAndDisplayCoordinates(location: Location?) {
        gpsConverter.convertCoordinates(location, coordinateFormat).also {
            latitude.text = it.latitude
            longitude.text = it.longitude
            mgrs.text = it.mgrs
        }
    }

    private fun showCorrectCoordinateViews() {
        when (coordinateFormat) {
            CoordinateFormat.MGRS ->
                toggleViewVisibility(
                        visible = listOf(mgrs),
                        hidden = listOf(latLayout, lonLayout)
                )
            else ->
                toggleViewVisibility(
                        visible = listOf(latLayout, lonLayout),
                        hidden = listOf(mgrs)
                )
        }
    }

    private fun toggleViewVisibility(visible: List<View>, hidden: List<View>) {
        visible.forEach { it.visibility = View.VISIBLE }
        hidden.forEach { it.visibility = View.GONE }
    }
}