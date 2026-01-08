package es.gbr.aeris.view.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.gbr.aeris.R
import es.gbr.aeris.ui.theme.AerisTheme

class AjustesActivity : ComponentActivity() {

    private var usarFahrenheit: Boolean = false
    private var usarMph: Boolean = false
    private var temaOscuro: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let {
            usarFahrenheit = it.getBoolean("usarFahrenheit", false)
            usarMph = it.getBoolean("usarMph", false)
            temaOscuro = it.getBoolean("temaOscuro", false)
        }

        if (!intent.hasExtra("temaOscuro")) {
            val esTemaOscuroSistema = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            temaOscuro = esTemaOscuroSistema
        }

        setContent {
            var fahrenheit by remember { mutableStateOf(usarFahrenheit) }
            var mph by remember { mutableStateOf(usarMph) }
            var oscuro by remember { mutableStateOf(temaOscuro) }

            AerisTheme(darkTheme = oscuro) {
                PantallaAjustes(
                    usarFahrenheit = fahrenheit,
                    usarMph = mph,
                    temaOscuro = oscuro,
                    alCambiarFahrenheit = { 
                        fahrenheit = it
                        usarFahrenheit = it
                    },
                    alCambiarMph = { 
                        mph = it
                        usarMph = it
                    },
                    alCambiarTema = { 
                        oscuro = it
                        temaOscuro = it
                        aplicarTema(it)
                    },
                    alNavegarAInicio = {
                        val intencion = Intent(this, MainActivity::class.java)
                        val bundle = Bundle()
                        bundle.putBoolean("usarFahrenheit", fahrenheit)
                        bundle.putBoolean("usarMph", mph)
                        bundle.putBoolean("temaOscuro", oscuro)
                        intencion.putExtras(bundle)
                        startActivity(intencion)
                        finish()
                    },
                    alNavegarALocalizaciones = {
                        val intencion = Intent(this, LocalizacionesActivity::class.java)
                        val bundle = Bundle()
                        bundle.putBoolean("usarFahrenheit", fahrenheit)
                        bundle.putBoolean("usarMph", mph)
                        bundle.putBoolean("temaOscuro", oscuro)
                        intencion.putExtras(bundle)
                        startActivity(intencion)
                        finish()
                    }
                )
            }
        }
    }

    private fun aplicarTema(esOscuro: Boolean) {
        if (esOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAjustes(
    usarFahrenheit: Boolean,
    usarMph: Boolean,
    temaOscuro: Boolean,
    alCambiarFahrenheit: (Boolean) -> Unit,
    alCambiarMph: (Boolean) -> Unit,
    alCambiarTema: (Boolean) -> Unit,
    alNavegarAInicio: () -> Unit,
    alNavegarALocalizaciones: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ajustes_titulo),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(painterResource(R.drawable.ic_home), contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_inicio)) },
                    selected = false,
                    onClick = alNavegarAInicio
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(R.drawable.ic_localizacion), contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_localizaciones)) },
                    selected = false,
                    onClick = alNavegarALocalizaciones
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(R.drawable.ic_ajustes), contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_ajustes)) },
                    selected = true,
                    onClick = { }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.ajustes_unidades),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Card Temperatura
            TarjetaAjuste(
                titulo = stringResource(R.string.ajustes_temperatura),
                subtitulo = stringResource(R.string.ajustes_escoge_opcion),
                etiquetaIzquierda = "°C",
                etiquetaDerecha = "°F",
                activado = usarFahrenheit,
                alCambiar = alCambiarFahrenheit
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Card Velocidad viento
            TarjetaAjuste(
                titulo = stringResource(R.string.ajustes_velocidad_viento),
                subtitulo = stringResource(R.string.ajustes_escoge_opcion),
                etiquetaIzquierda = "km/h",
                etiquetaDerecha = "mph",
                activado = usarMph,
                alCambiar = alCambiarMph
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.ajustes_tema_app),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Card Tema
            TarjetaAjuste(
                titulo = stringResource(R.string.ajustes_tema_titulo),
                subtitulo = stringResource(R.string.ajustes_escoge_opcion),
                etiquetaIzquierda = stringResource(R.string.ajustes_claro),
                etiquetaDerecha = stringResource(R.string.ajustes_oscuro),
                activado = temaOscuro,
                alCambiar = alCambiarTema
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TarjetaAjuste(
    titulo: String,
    subtitulo: String,
    etiquetaIzquierda: String,
    etiquetaDerecha: String,
    activado: Boolean,
    alCambiar: (Boolean) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = etiquetaIzquierda,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (!activado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = activado,
                    onCheckedChange = alCambiar
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = etiquetaDerecha,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (activado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
