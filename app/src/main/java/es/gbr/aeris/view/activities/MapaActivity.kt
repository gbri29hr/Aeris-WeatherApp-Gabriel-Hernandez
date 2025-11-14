package es.gbr.aeris.view.activities

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import es.gbr.aeris.databinding.ActivityMapaBinding

// Muestra el mapa de una ciudad con OpenStreetMap
class MapaActivity : AppCompatActivity() {

    private lateinit var vinculacion: ActivityMapaBinding
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    private var nombreCiudad: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vinculacion = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(vinculacion.root)

        // Configurar Toolbar
        setSupportActionBar(vinculacion.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Obtener datos del Bundle
        intent.extras?.let { bundle ->
            latitud = bundle.getDouble("latitud", 0.0)
            longitud = bundle.getDouble("longitud", 0.0)
            nombreCiudad = bundle.getString("nombreCiudad", "")
        }

        // Validar datos
        if (latitud == 0.0 && longitud == 0.0) {
            Toast.makeText(this, "Error: coordenadas no válidas", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Actualizar título con el nombre de la ciudad
        vinculacion.toolbar.title = nombreCiudad

        // Configurar botón de volver
        vinculacion.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Configurar y cargar el mapa en WebView
        configurarWebView()
        configurarBotonRetroceso()
    }

    private fun configurarWebView() {
        vinculacion.webView.webViewClient = WebViewClient()

        val webSettings: WebSettings = vinculacion.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        // Construir la URL del mapa con OpenStreetMap
        val zoom = 13
        val urlMapa = "https://www.openstreetmap.org/export/embed.html?" +
                "bbox=${longitud-0.01},${latitud-0.01},${longitud+0.01},${latitud+0.01}" +
                "&layer=mapnik" +
                "&marker=$latitud,$longitud"

        // Cargar directamente la URL del mapa
        vinculacion.webView.loadUrl(urlMapa)
    }

    private fun configurarBotonRetroceso() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (vinculacion.webView.canGoBack()) {
                    vinculacion.webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }
}

