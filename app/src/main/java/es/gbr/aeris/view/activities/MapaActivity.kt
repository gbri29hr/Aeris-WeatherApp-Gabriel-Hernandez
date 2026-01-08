package es.gbr.aeris.view.activities

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import es.gbr.aeris.ui.theme.AerisTheme


class MapaActivity : ComponentActivity() {

    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    private var nombreCiudad: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let { bundle ->
            latitud = bundle.getDouble("latitud", 0.0)
            longitud = bundle.getDouble("longitud", 0.0)
            nombreCiudad = bundle.getString("nombreCiudad", "")
        }

        if (latitud == 0.0 && longitud == 0.0) {
            Toast.makeText(this, "Error: coordenadas no válidas", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            AerisTheme {
                PantallaMapa(
                    nombreCiudad = nombreCiudad,
                    latitud = latitud,
                    longitud = longitud,
                    alVolver = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMapa(
    nombreCiudad: String,
    latitud: Double,
    longitud: Double,
    alVolver: () -> Unit
) {
    var vistaWeb: WebView? by remember { mutableStateOf(null) }
    
    BackHandler {
        if (vistaWeb?.canGoBack() == true) {
            vistaWeb?.goBack()
        } else {
            alVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = nombreCiudad,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        AndroidView(
            factory = { contexto ->
                WebView(contexto).apply {
                    webViewClient = WebViewClient()
                    
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                    }
                    
                    val urlMapa = "https://www.openstreetmap.org/export/embed.html?" +
                            "bbox=${longitud-0.01},${latitud-0.01},${longitud+0.01},${latitud+0.01}" +
                            "&layer=mapnik" +
                            "&marker=$latitud,$longitud"
                    
                    loadUrl(urlMapa)
                    vistaWeb = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

