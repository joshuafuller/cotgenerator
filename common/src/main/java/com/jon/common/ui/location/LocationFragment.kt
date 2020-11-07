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
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.jon.common.R
import com.jon.common.databinding.FragmentLocationBinding
import com.jon.common.di.IUiResources
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getStringFromPair
import com.jon.common.repositories.IGpsRepository
import com.jon.common.ui.viewBinding
import com.jon.common.utils.MinimumVersions.GNSS_CALLBACK
import com.jon.common.utils.Notify
import com.jon.common.utils.VersionUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class LocationFragment : Fragment(R.layout.fragment_location),
        SensorEventListener,
        GnssCallback.IListener {

    private val binding by viewBinding(FragmentLocationBinding::bind)

    private val locationManager by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private val gpsConverter = GpsConverter()
    private val compass by lazy { Compass(requireContext()) }
    private lateinit var coordinateFormat: CoordinateFormat

    private var gnssCallback: GnssCallback? = null
    private var mostRecentLocation: Location? = null

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseCoordinateFormat()
        initialiseCoordinateButtons()
        observeGpsData()
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            binding.numSatellites.text = GPS_NOT_ENABLED
        }
    }

    override fun onResume() {
        super.onResume()
        compass.registerListener(this)
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        if (VersionUtils.isAtLeast(GNSS_CALLBACK)) {
            gnssCallback = GnssCallback(this).also {
                locationManager.registerGnssStatusCallback(it)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (VersionUtils.isAtLeast(GNSS_CALLBACK)) {
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
            binding.compassDegrees.text = "%3.0f° %s".format(it.degrees, it.direction)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        /* No-op */
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onUsefulSatellitesReported(numUsefulSatellites: Int) {
        binding.numSatellites.text = if (numUsefulSatellites == GnssCallback.GNSS_STOPPED) {
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

    private fun initialiseCoordinateButtons() {
        val accent = ContextCompat.getColor(requireContext(), uiResources.accentColourId)
        binding.coordFormatButton.setBackgroundColor(accent)
        binding.coordFormatButton.text = coordinateFormat.name
        binding.coordFormatButton.setOnClickListener {
            coordinateFormat = CoordinateFormat.getNext(coordinateFormat)
            binding.coordFormatButton.text = coordinateFormat.name
            convertAndDisplayCoordinates(mostRecentLocation)
            showCorrectCoordinateViews()
            prefs.edit()
                    .putString(CommonPrefs.LOCATION_COORDINATE_FORMAT.key, coordinateFormat.name)
                    .apply()
        }

        binding.coordCopyButton.setBackgroundColor(accent)
        val tintedIcon = DrawableCompat.wrap(ContextCompat.getDrawable(requireContext(), R.drawable.copy)!!)
        DrawableCompat.setTint(tintedIcon, ContextCompat.getColor(requireContext(), R.color.black))
        binding.coordCopyButton.setCompoundDrawablesWithIntrinsicBounds(tintedIcon, null, null, null)
        binding.coordCopyButton.setOnClickListener {
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
            binding.latDegrees.text = it.latitude
            binding.lonDegrees.text = it.longitude
            binding.mgrs.text = it.mgrs
            binding.positionalError.text = it.positionalError
            binding.altitudeMetres.text = it.altitudeWgs84
            binding.speed.text = it.speedMetresPerSec
            binding.bearing.text = it.bearing
        }
    }

    private fun showCorrectCoordinateViews() {
        when (coordinateFormat) {
            CoordinateFormat.MGRS ->
                toggleViewVisibility(
                        visible = listOf(binding.mgrsLayout),
                        hidden = listOf(binding.latitudeLayout, binding.longitudeLayout)
                )
            else ->
                toggleViewVisibility(
                        visible = listOf(binding.latitudeLayout, binding.longitudeLayout),
                        hidden = listOf(binding.mgrsLayout)
                )
        }
    }

    private fun toggleViewVisibility(visible: List<View>, hidden: List<View>) {
        visible.forEach { it.visibility = View.VISIBLE }
        hidden.forEach { it.visibility = View.GONE }
    }

    private companion object {
        const val GPS_NOT_ENABLED = "GPS NOT ENABLED"
    }
}