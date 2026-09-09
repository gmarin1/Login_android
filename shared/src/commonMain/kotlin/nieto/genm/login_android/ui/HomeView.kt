package nieto.genm.login_android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nieto.genm.login_android.data.UsuarioService
import nieto.genm.login_android.data.local.AppDB
import nieto.genm.login_android.data.local.DireccionesEntity
import nieto.genm.login_android.data.remote.PlacePrediction
import nieto.genm.login_android.data.remote.PlacesService
import kotlin.time.Duration.Companion.milliseconds
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(token: String, userId: Long, database: AppDB, onLogout: () -> Unit, onNuevaUbicacion: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Direcciones", "Mapa")
    var direccionParaMapa by remember { mutableStateOf<DireccionesEntity?>(null) }

    val scope = rememberCoroutineScope()
    fun validarTokenAccion(accion: suspend () -> Unit) {
        scope.launch {
            val resultado = UsuarioService.verificarToken(token)
            if (resultado.isSuccess) { accion() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { validarTokenAccion {onNuevaUbicacion()} }
            ){ Icon(imageVector = Icons.Default.Add, contentDescription = "Nueva Ubicacion") }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { validarTokenAccion{selectedTab = index} },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> DireccionesTab(
                    token = token,
                    userId = userId,
                    database = database,
                    onDireccionSeleccionada = { direccion ->
                        validarTokenAccion {
                            direccionParaMapa = direccion
                            selectedTab = 1
                        }
                    }
                )
                1 -> MapaTab(
                    direccionSeleccionada = direccionParaMapa,
                    onGuardarUbicacionAjustada = { nuevaLat, nuevaLon ->
                        validarTokenAccion {
                            direccionParaMapa?.let { dirActual ->
                                val direccionActualizada = dirActual.copy(
                                    latitud = nuevaLat,
                                    longitud = nuevaLon
                                )
                                database.direccionDao().actualizar(direccionActualizada)
                                direccionParaMapa = direccionActualizada
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DireccionesTab(token: String,userId: Long, database: AppDB,onDireccionSeleccionada: (DireccionesEntity) ->Unit) {
    var query by remember { mutableStateOf("") }
    var sugerencias by remember { mutableStateOf(listOf<PlacePrediction>()) }
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val dao = database.direccionDao()
    val direccionesGuardadas by dao
        .obtenerPorUsuario(userId)
        .collectAsState(initial = emptyList())

    fun validarTokenAccion(accion: suspend () -> Unit) {
        scope.launch {
            val resultado = UsuarioService.verificarToken(token)
            if (resultado.isSuccess) {
                accion()
            }
        }
    }

    LaunchedEffect(query) {
        if (query.length >= 3) {
            delay(500.milliseconds)
            sugerencias = PlacesService.buscarDirecciones(query)
            expanded = sugerencias.isNotEmpty()
        } else {
            sugerencias = emptyList()
            expanded = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Registrar Direcciones",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = { text -> query = text },
                label = {Text("Buscar Dirección")},
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    clippingEnabled = false
                ),
                modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 350.dp)
            ) {
                sugerencias.forEach { seleccion ->
                    DropdownMenuItem(
                        text = { Text(seleccion.descripcion, maxLines = 2) },
                        onClick = {
                            expanded = false
                            query = ""
                            validarTokenAccion {
                                dao.insertar(
                                    DireccionesEntity(
                                        userId = userId,
                                        calle = seleccion.descripcion,
                                        numero = "",
                                        colonia = "",
                                        codigoPostal = seleccion.codigoPostal,
                                        latitud = seleccion.latitud,
                                        longitud = seleccion.longitud
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Mis Direcciones Guardadas",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn {
            items(direccionesGuardadas, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable{onDireccionSeleccionada(item)}
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.calle, style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(
                            onClick = {
                                validarTokenAccion { dao.eliminar(item.id, userId) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapaTab(
    direccionSeleccionada: DireccionesEntity?,
    onGuardarUbicacionAjustada: (nuevaLat: Double, nuevaLon: Double) -> Unit
) {
    val tieneUbicacionValida = direccionSeleccionada != null

    val latOriginal = direccionSeleccionada?.latitud ?: 19.432608
    val lonOriginal = direccionSeleccionada?.longitud ?: -99.133209

    var latMod by remember(direccionSeleccionada) { mutableStateOf(direccionSeleccionada?.latitud ?: 19.432608) }
    var lonMod by remember(direccionSeleccionada) { mutableStateOf(direccionSeleccionada?.longitud ?: -99.133209) }
    var fueEditado by remember(direccionSeleccionada) { mutableStateOf(false) }

    val titulo = direccionSeleccionada?.calle ?: "No se ha seleccionado ninguna ubicacion"

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ubicación en el mapa:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(text = titulo, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                }

                if (fueEditado && direccionSeleccionada != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                latMod = latOriginal
                                lonMod = lonOriginal
                                fueEditado = false
                            }
                        ) { Text("Cancelar") }
                        Button(
                            onClick = {
                                onGuardarUbicacionAjustada(latMod, lonMod)
                                fueEditado = false
                            }
                        ) { Text("Guardar Pin") }
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds()){
            OsmMapView(
                latitud = latMod,
                longitud = lonMod,
                titulo = titulo,
                tieneUbicacionValida = tieneUbicacionValida,
                esEditable = true,
                onUbicacionCambiada = { nuevaLat, nuevaLon ->
                    latMod = nuevaLat
                    lonMod = nuevaLon
                    fueEditado = true
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}