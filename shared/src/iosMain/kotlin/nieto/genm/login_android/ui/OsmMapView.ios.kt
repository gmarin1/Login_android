package nieto.genm.login_android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@Composable
actual fun OsmMapView(
    latitud: Double,
    longitud: Double,
    titulo: String,
    tieneUbicacionValida: Boolean,
    modifier: Modifier,
    esEditable: Boolean,
    onUbicacionCambiada: ((lat: Double, lon: Double) -> Unit)?
) {
    UIKitView(
        modifier = modifier,
        factory = { MKMapView() },
        update = { mapView ->
            val coordinate = CLLocationCoordinate2DMake(latitud, longitud)
            val distance = if (tieneUbicacionValida) 2000.0 else 500000.0
            val region = MKCoordinateRegionMakeWithDistance(coordinate, distance, distance)

            mapView.setRegion(region, animated = true)
            mapView.removeAnnotations(mapView.annotations)

            if (tieneUbicacionValida) {
                val annotation = MKPointAnnotation().apply {
                    setCoordinate(coordinate)
                    setTitle(titulo)
                }
                mapView.addAnnotation(annotation)
            }
        }
    )
}