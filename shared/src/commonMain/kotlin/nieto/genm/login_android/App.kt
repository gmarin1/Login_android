package nieto.genm.login_android

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import nieto.genm.login_android.data.local.DatabaseBuilder
import nieto.genm.login_android.navigation.AppNav

@Composable
fun App(builder: DatabaseBuilder) {
    MaterialTheme {
        AppNav(builder = builder)
    }
}