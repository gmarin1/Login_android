package nieto.genm.login_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import nieto.genm.login_android.data.local.DatabaseBuilder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val databaseBuilder = DatabaseBuilder(applicationContext)

        setContent {
            App(builder = databaseBuilder)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val context = androidx.compose.ui.platform.LocalContext.current
    App(builder = DatabaseBuilder(context.applicationContext))
}