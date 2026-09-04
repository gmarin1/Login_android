package nieto.genm.login_android.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.russhwolf.settings.Settings
import nieto.genm.login_android.data.UsuarioService
import nieto.genm.login_android.data.local.DatabaseBuilder
import nieto.genm.login_android.data.local.getRoomDatabase
import nieto.genm.login_android.ui.HomeView
import nieto.genm.login_android.ui.LoginView
import nieto.genm.login_android.ui.RegisterView

object Rutas {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

@Composable
fun AppNav(builder: DatabaseBuilder) {
    val navController = rememberNavController()
    val settings = remember { Settings() }
    val database = remember { getRoomDatabase(builder) }

    var alertaExpirado by remember { mutableStateOf(false) }
    var estaCargando by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf(Rutas.LOGIN) }

    LaunchedEffect(Unit) {
        UsuarioService.onSesionExpirada = {
            settings.remove("jwt_token")
            settings.remove("userId")
            alertaExpirado = true
        }
    }

    LaunchedEffect(Unit) {
        val tokenActual = settings.getString("jwt_token", "")
        if (tokenActual.isNotEmpty()) {
            val resultado = UsuarioService.verificarToken(tokenActual)
            resultado.onSuccess {
                startDestination = Rutas.HOME
            }.onFailure {
                settings.remove("jwt_token")
                settings.remove("userId")
                startDestination = Rutas.LOGIN
            }
        } else {
            startDestination = Rutas.LOGIN
        }
        estaCargando = false
    }

    if (alertaExpirado) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Sesión Expirada") },
            text = { Text("Tu sesión ha caducado. Por favor, vuelve a ingresar.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        alertaExpirado = false
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) { Text("Entendido") }
            }
        )
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Rutas.LOGIN) {
            LoginView(
                onLoginSuccess = { token, userId ->
                    settings.putString("jwt_token",token)
                    settings.putLong("userId",userId)
                    navController.navigate(Rutas.HOME) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {navController.navigate(Rutas.REGISTER)}
            )
        }

        composable(Rutas.REGISTER) {
            RegisterView(
                onRegisterSuccess = {navController.popBackStack()}
            )
        }

        composable(Rutas.HOME) {
            HomeView(
                token = settings.getString("jwt_token", ""),
                userId = settings.getLong("userId",0L),
                database = database,
                onLogout = {
                    settings.remove("jwt_token")
                    settings.remove("userId")
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}