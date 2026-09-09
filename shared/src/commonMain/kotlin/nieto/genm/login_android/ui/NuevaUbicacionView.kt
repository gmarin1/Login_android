package nieto.genm.login_android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nieto.genm.login_android.data.UsuarioService
import nieto.genm.login_android.data.local.DireccionesEntity
import nieto.genm.login_android.data.remote.PlacePrediction
import nieto.genm.login_android.data.remote.PlacesService
import kotlin.time.Duration.Companion.milliseconds

fun extraerMunicipioLimpio(cadena: String): String {
    if (cadena.isBlank()) return ""
    val partes = cadena.split(",").map { it.trim() }
    if (partes.size > 1 && partes[0].all { it.isDigit() })
        return partes[1]
    return partes.firstOrNull() ?: cadena
}
@Composable
fun NuevaUbicacionView(
    navController: NavHostController,
    token: String,
    userId: Long,
    prediccionInicial: PlacePrediction? = null,
    onGuardarDireccion: (DireccionesEntity) -> Unit
) {
    var calle by remember { mutableStateOf(prediccionInicial?.calle ?: "") }
    var numero by remember { mutableStateOf(prediccionInicial?.numero ?: "") }
    var colonia by remember { mutableStateOf(prediccionInicial?.colonia ?: "") }
    var municipioAlcaldia by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf(prediccionInicial?.codigoPostal ?: "") }
//    var latitud by remember { mutableStateOf(prediccionInicial?.latitud ?: 0.0) }
//    var longitud by remember { mutableStateOf(prediccionInicial?.longitud ?: 0.0) }

    val scope = rememberCoroutineScope()

    var sugerenciasCP by remember { mutableStateOf(listOf<PlacePrediction>()) }
    var sugerenciasColonia by remember { mutableStateOf(listOf<PlacePrediction>()) }
    var sugerenciasCalle by remember { mutableStateOf(listOf<PlacePrediction>()) }

    var mostrarMenuCP by remember { mutableStateOf(false) }
    var mostrarMenuColonia by remember { mutableStateOf(false) }
    var mostrarMenuCalle by remember { mutableStateOf(false) }

    var seleccionoCPManualmente by remember { mutableStateOf(false) }
    var seleccionoColoniaManualmente by remember { mutableStateOf(false) }
    var seleccionoCalleManualmente by remember { mutableStateOf(false) }

    LaunchedEffect(codigoPostal) {
        if (seleccionoCPManualmente) {
            seleccionoCPManualmente = false
            return@LaunchedEffect
        }
        if (codigoPostal.length == 5 && codigoPostal.all { it.isDigit() }) {
            delay(300.milliseconds)
            val resultados = PlacesService.buscarPorCodigoPostal(codigoPostal)
            sugerenciasCP = resultados
            mostrarMenuCP = resultados.isNotEmpty()
            resultados.firstOrNull()?.let { lugar ->
                if (lugar.municipio.isNotBlank())
                    municipioAlcaldia = extraerMunicipioLimpio(lugar.municipio)
                else if (lugar.descripcion.isNotBlank())
                    municipioAlcaldia = extraerMunicipioLimpio(lugar.descripcion)
            }
        } else { mostrarMenuCP = false }
    }

    LaunchedEffect(colonia) {
        if (seleccionoColoniaManualmente) {
            seleccionoColoniaManualmente = false
            return@LaunchedEffect
        }
        if (colonia.length >= 3 && !mostrarMenuCP) {
            delay(400.milliseconds)
            val resultados = PlacesService.buscarColonias(colonia)
            sugerenciasColonia = resultados
            mostrarMenuColonia = resultados.isNotEmpty()
        } else { mostrarMenuColonia = false }
    }

    LaunchedEffect(calle) {
        if (seleccionoCalleManualmente) {
            seleccionoCalleManualmente = false
            return@LaunchedEffect
        }
        if (calle.length >= 3 && !mostrarMenuColonia && !mostrarMenuCP) {
            delay(400.milliseconds)
            val resultados = PlacesService.buscarCalles(calleQuery = calle, colonia = colonia, cp = codigoPostal)
            sugerenciasCalle = resultados
            mostrarMenuCalle = resultados.isNotEmpty()
        } else { mostrarMenuCalle = false }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Registro de Ubicación", style = MaterialTheme.typography.titleLarge)
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = codigoPostal,
                onValueChange = { input ->
                    if (input.length <= 5 && input.all { it.isDigit() })
                        codigoPostal = input
                },
                label = { Text("Código Postal") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = mostrarMenuCP,
                onDismissRequest = { mostrarMenuCP = false },
                properties = PopupProperties(focusable = false),
                modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 250.dp)
            ) {
                sugerenciasCP.forEach { lugar ->
                    DropdownMenuItem(
                        text = { Text(lugar.descripcion) },
                        onClick = {
                            seleccionoCPManualmente = true
                            if (lugar.codigoPostal.isNotEmpty())
                                codigoPostal = lugar.codigoPostal
                            if (lugar.colonia.isNotBlank()) {
                                seleccionoColoniaManualmente = true
                                colonia = lugar.colonia
                            }
                            mostrarMenuCP = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = municipioAlcaldia,
            onValueChange = { municipioAlcaldia = it },
            label = { Text("Municipio o Alcaldia") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = colonia,
                onValueChange = { colonia = it },
                label = { Text("Colonia") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = mostrarMenuColonia,
                onDismissRequest = { mostrarMenuColonia = false },
                properties = PopupProperties(focusable = false),
                modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 250.dp)
            ) {
                sugerenciasColonia.forEach { lugar ->
                    DropdownMenuItem(
                        text = {
                            val textoMostrar = if (lugar.colonia.isNotBlank()) {
                                "${lugar.colonia}, ${lugar.municipio}".removeSuffix(", ")
                            } else {
                                lugar.descripcion.split(",").firstOrNull()?.trim() ?: lugar.descripcion
                            }
                            Text(textoMostrar)
                        },
                        onClick = {
                            seleccionoColoniaManualmente = true
                            colonia = lugar.colonia.ifBlank {
                                lugar.descripcion.split(",").firstOrNull()?.trim() ?: ""
                            }
                            if (lugar.codigoPostal.isNotEmpty()) {
                                seleccionoCPManualmente = true
                                codigoPostal = lugar.codigoPostal
                            }
                            if (lugar.municipio.isNotEmpty())
                                municipioAlcaldia = lugar.municipio
                            mostrarMenuColonia = false
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = calle,
                onValueChange = { calle = it },
                label = { Text("Calle o Avenida") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            DropdownMenu(
                expanded = mostrarMenuCalle,
                onDismissRequest = { mostrarMenuCalle = false },
                properties = PopupProperties(focusable = false),
                modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 250.dp)
            ) {
                sugerenciasCalle.forEach { lugar ->
                    DropdownMenuItem(
                        text = { Text(lugar.calle.ifEmpty {lugar.descripcion}) },
                        onClick = {
                            seleccionoCalleManualmente = true
                            calle = lugar.calle.ifEmpty { calle }
                            if (lugar.colonia.isNotEmpty() && colonia.isEmpty()) {
                                seleccionoColoniaManualmente = true
                                colonia = lugar.colonia
                            }
                            if (lugar.codigoPostal.isNotEmpty() && codigoPostal.isEmpty()) {
                                seleccionoCPManualmente = true
                                codigoPostal = lugar.codigoPostal
                            }
                            mostrarMenuCalle = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = numero,
            onValueChange = { numero = it },
            label = { Text("Número Ext / Int") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }

            Button(
                onClick = {
                    scope.launch {
                        val resultado = UsuarioService.verificarToken(token)

                        if (resultado.isSuccess) {
                            val (latFinal, lonFinal) = PlacesService.obtenerCoordenadasFinales(
                                calle = calle,
                                numero = numero,
                                colonia = colonia,
                                cp = codigoPostal
                            )
                            val nuevaDireccion = DireccionesEntity(
                                userId = userId,
                                calle = calle,
                                numero = numero,
                                colonia = colonia,
                                codigoPostal = codigoPostal,
                                latitud = latFinal,
                                longitud = lonFinal
                            )
                            onGuardarDireccion(nuevaDireccion)
                        }
                    }
                },
                enabled = colonia.isNotBlank() && calle.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) { Text("Guardar Dirección") }
        }
    }
}