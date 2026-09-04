package nieto.genm.login_android

import androidx.compose.ui.window.ComposeUIViewController
import nieto.genm.login_android.data.local.DatabaseBuilder

fun MainViewController() = ComposeUIViewController {
    val databaseBuilder = DatabaseBuilder()
    App(builder = databaseBuilder)
}