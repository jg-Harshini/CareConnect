package com.example.careconnect.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import android.graphics.Color as AndroidColor
import androidx.core.content.ContextCompat
import com.example.careconnect.R
import com.example.careconnect.ui.theme.PrimaryBlue

@Composable
fun OSMMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    zoomLevel: Double = 17.0,
    onMapClick: ((GeoPoint) -> Unit)? = null,
    onMapLongClick: ((GeoPoint) -> Unit)? = null,
    circles: List<GeofenceCircle> = emptyList(),
    destinationLat: Double = 0.0,
    destinationLng: Double = 0.0,
    showHomeIcon: Boolean = true
) {
    val context = LocalContext.current
    
    // 1. Configure OSMDroid BEFORE MapView creation
    val initialized = remember {
        val config = Configuration.getInstance()
        config.userAgentValue = context.packageName
        config.load(context, context.getSharedPreferences("osm_pref", Context.MODE_PRIVATE))
        true
    }

    // 2. MapView setup
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isTilesScaledToDpi = true
            controller.setZoom(zoomLevel)
        }
    }

    var hasCentered by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        if (latitude == 0.0 && !hasCentered) {
            // Show loading until we have a real coordinate
            Box(
                modifier = Modifier.matchParentSize().background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }

        AndroidView(
            factory = { mapView },
            modifier = Modifier.matchParentSize(),
            update = { view ->
                if (latitude != 0.0 && longitude != 0.0) {
                    val centerPoint = GeoPoint(latitude, longitude)
                    
                    if (!hasCentered) {
                        view.controller.setCenter(centerPoint)
                        view.controller.setZoom(zoomLevel)
                        hasCentered = true
                    }
                    
                    view.overlays.clear()

                    // Add Safe Zones
                    circles.forEach { circle ->
                        val polygon = Polygon(view)
                        polygon.points = Polygon.pointsAsCircle(GeoPoint(circle.lat, circle.lng), circle.radius.toDouble())
                        polygon.fillPaint.color = AndroidColor.argb(40, 0, 150, 255)
                        polygon.outlinePaint.color = AndroidColor.argb(150, 0, 100, 255)
                        polygon.outlinePaint.strokeWidth = 2f
                        view.overlays.add(polygon)
                    }

                    // Current Location Marker
                    val startMarker = Marker(view)
                    startMarker.position = centerPoint
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    startMarker.title = "Current Position"
                    // Optionally set a different icon for current location
                    // startMarker.icon = ContextCompat.getDrawable(context, R.drawable.ic_location_on)
                    view.overlays.add(startMarker)

                    // Path to Home
                    if (destinationLat != 0.0 && destinationLng != 0.0) {
                        val destPoint = GeoPoint(destinationLat, destinationLng)
                        val line = Polyline(view)
                        line.setPoints(listOf(centerPoint, destPoint))
                        line.outlinePaint.color = AndroidColor.RED
                        line.outlinePaint.strokeWidth = 6f
                        view.overlays.add(line)

                        if (showHomeIcon) {
                            val destMarker = Marker(view)
                            destMarker.position = destPoint
                            destMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            destMarker.title = "Home"
                            // Set the home icon specifically for the home location
                            destMarker.icon = ContextCompat.getDrawable(context, R.drawable.ic_home)
                            view.overlays.add(destMarker)
                        }
                    }
                }

                // Click Overlay
                val mReceive = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        onMapClick?.invoke(p)
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint): Boolean {
                        onMapLongClick?.invoke(p)
                        return true
                    }
                }
                view.overlays.add(MapEventsOverlay(mReceive))
                view.invalidate()
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }
}

data class GeofenceCircle(
    val lat: Double,
    val lng: Double,
    val radius: Float,
    val name: String
)
