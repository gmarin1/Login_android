package nieto.genm.login_android.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun OsmMapView(
    latitud: Double,
    longitud: Double,
    titulo: String,
    tieneUbicacionValida: Boolean,
    modifier: Modifier = Modifier
)