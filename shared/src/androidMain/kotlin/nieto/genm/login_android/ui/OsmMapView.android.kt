package nieto.genm.login_android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
actual fun OsmMapView(
    latitud: Double,
    longitud: Double,
    titulo: String,
    tieneUbicacionValida: Boolean,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier.clipToBounds(),
        factory = { context ->
            Configuration.getInstance().userAgentValue = context.packageName
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(13.0)
            }
        },
        update = { mapView ->
            val nuevoPunto = GeoPoint(latitud, longitud)
            mapView.controller.setCenter(nuevoPunto)
            mapView.controller.setZoom(13.0)

            mapView.overlays.clear()
            if (tieneUbicacionValida) {
                val marker = Marker(mapView).apply {
                    position = nuevoPunto
                    title = titulo
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}