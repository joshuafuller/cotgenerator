package com.jon.common.ui.location

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.jon.common.R
import com.jon.common.di.IUiResources
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getStringFromPair
import com.jon.common.repositories.IGpsRepository
import com.jon.common.utils.Notify
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class LocationFragment : Fragment(),
        SensorEventListener,
        GnssCallback.IListener {

    private val locationManager by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    private val gpsConverter = GpsConverter()
    private var mostRecentLocation: Location? = null
    private lateinit var coordinateFormat: CoordinateFormat
    private lateinit var latitude: TextView
    private lateinit var longitude: TextView
    private lateinit var latLayout: View
    private lateinit var lonLayout: View
    private lateinit var mgrsLayout: View
    private lateinit var positionalError: TextView
    private lateinit var numSatelliteFixes: TextView
    private lateinit var mgrs: TextView
    private lateinit var coordinateFormatButton: Button
    private lateinit var coordinateCopyButton: Button

    private val compass by lazy { Compass(requireContext()) }
    private lateinit var compassDegrees: TextView

    private lateinit var altitude: TextView
    private lateinit var speed: TextView
    private lateinit var bearing: TextView

    private var gnssCallback: GnssCallback? = null

    @Inject
    lateinit var gpsRepository: IGpsRepository

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var uiResources: IUiResources

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = View.inflate(context, R.layout.fragment_location, null)
        /* Coordinate views */
        latitude = view.findViewById(R.id.coords_lat_degrees)
        longitude = view.findViewById(R.id.coords_lon_degrees)
        mgrs = view.findViewById(R.id.coords_mgrs)
        positionalError = view.findViewById(R.id.coords_positional_error)
        latLayout = view.findViewById(R.id.latitude_layout)
        lonLayout = view.findViewById(R.id.longitude_layout)
        mgrsLayout = view.findViewById(R.id.mgrs_layout)
        initialiseCoordinateFormat()
        initialiseCoordinateButtons(view)
        observeGpsData()
        numSatelliteFixes = view.findViewById(R.id.gps_useful_satellites)
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            numSatelliteFixes.text = GPS_NOT_ENABLED
        }

        /* Other views */
        compassDegrees = view.findViewById(R.id.compass_degrees)
        bearing = view.findViewById(R.id.bearing)
        speed = view.findViewById(R.id.speed_m_per_s)
        altitude = view.findViewById(R.id.altitude_metres)
        return view
    }

    override fun onResume() {
        super.onResume()
        compass.registerListener(this)
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        if (sdkOver24()) {
            gnssCallback = GnssCallback(this).also {
                locationManager.registerGnssStatusCallback(it)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (sdkOver24()) {
            gnssCallback?.let { locationManager.unregisterGnssStatusCallback(it) }
        }
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
            compassDegrees.text = "%3.0f° %s".format(it.degrees, it.direction)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        /* No-op */
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onUsefulSatellitesReported(numUsefulSatellites: Int) {
        numSatelliteFixes.text = if (numUsefulSatellites == GnssCallback.GNSS_STOPPED) {
            GPS_NOT_ENABLED
        } else {
            numUsefulSatellites.toString()
        }
    }

    private fun initialiseCoordinateFormat() {
        coordinateFormat = CoordinateFormat.fromString(
                prefs.getStringFromPair(CommonPrefs.LOCATION_COORDINATE_FORMAT)
        )
        showCorrectCoordinateViews()
    }

    private fun initialiseCoordinateButtons(view: View) {
        val accent = ContextCompat.getColor(requireContext(), uiResources.accentColourId)
        coordinateFormatButton = view.findViewById(R.id.coord_format_button)
        coordinateFormatButton.setBackgroundColor(accent)
        coordinateFormatButton.text = coordinateFormat.name
        coordinateFormatButton.setOnClickListener {
            coordinateFormat = CoordinateFormat.getNext(coordinateFormat)
            coordinateFormatButton.text = coordinateFormat.name
            convertAndDisplayCoordinates(mostRecentLocation)
            showCorrectCoordinateViews()
            prefs.edit()
                    .putString(CommonPrefs.LOCATION_COORDINATE_FORMAT.key, coordinateFormat.name)
                    .apply()
        }

        coordinateCopyButton = view.findViewById(R.id.coord_copy_button)
        coordinateCopyButton.setBackgroundColor(accent)
        val tintedIcon = DrawableCompat.wrap(ContextCompat.getDrawable(requireContext(), R.drawable.copy)!!)
        DrawableCompat.setTint(tintedIcon, ContextCompat.getColor(requireContext(), R.color.black))
        coordinateCopyButton.setCompoundDrawablesWithIntrinsicBounds(tintedIcon, null, null, null)
        coordinateCopyButton.setOnClickListener {
            /* Convert the displayed coordinates to a string and place it in the clipboard */
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val coordsString = gpsConverter.coordinatesToCopyableString(mostRecentLocation, coordinateFormat)
            val clip = ClipData.newPlainText("Copied ${coordinateFormat.name} coordinates", coordsString)
            clipboard.setPrimaryClip(clip)
            Notify.green(requireView(), "Copied \"${coordsString}\" to the clipboard")
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
            positionalError.text = it.positionalError
            altitude.text = it.altitudeWgs84
            speed.text = it.speedMetresPerSec
            bearing.text = it.bearing
        }
    }

    private fun showCorrectCoordinateViews() {
        when (coordinateFormat) {
            CoordinateFormat.MGRS ->
                toggleViewVisibility(
                        visible = listOf(mgrsLayout),
                        hidden = listOf(latLayout, lonLayout)
                )
            else ->
                toggleViewVisibility(
                        visible = listOf(latLayout, lonLayout),
                        hidden = listOf(mgrsLayout)
                )
        }
    }

    private fun toggleViewVisibility(visible: List<View>, hidden: List<View>) {
        visible.forEach { it.visibility = View.VISIBLE }
        hidden.forEach { it.visibility = View.GONE }
    }

    private companion object {
        const val GPS_NOT_ENABLED = "GPS NOT ENABLED"

        fun sdkOver24(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        }
    }
}