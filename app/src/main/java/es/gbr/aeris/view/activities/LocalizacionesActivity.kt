package es.gbr.aeris.view.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.gbr.aeris.R
import es.gbr.aeris.model.DatosCompartidos
import es.gbr.aeris.model.database.entities.CiudadEntidad
import es.gbr.aeris.model.database.relations.CiudadConTiempoActual
import es.gbr.aeris.ui.theme.AerisTheme
import es.gbr.aeris.viewmodel.LocalizacionesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class LocalizacionesActivity : ComponentActivity() {

    private val modeloVista: LocalizacionesViewModel by viewModels()

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

        modeloVista.actualizarCiudadesOcultas(DatosCompartidos.obtenerCiudadesOcultas())

        setContent {
            AerisTheme(darkTheme = temaOscuro) {
                PantallaLocalizaciones(
                    modeloVista = modeloVista,
                    usarFahrenheit = usarFahrenheit,
                    alNavegarAInicio = {
                        val intencion = Intent(this, MainActivity::class.java)
                        val bundle = Bundle()
                        bundle.putBoolean("usarFahrenheit", usarFahrenheit)
                        bundle.putBoolean("usarMph", usarMph)
                        bundle.putBoolean("temaOscuro", temaOscuro)
                        intencion.putExtras(bundle)
                        startActivity(intencion)
                        finish()
                    },
                    alNavegarAAjustes = {
                        val intencion = Intent(this, AjustesActivity::class.java)
                        val bundle = Bundle()
                        bundle.putBoolean("usarFahrenheit", usarFahrenheit)
                        bundle.putBoolean("usarMph", usarMph)
                        bundle.putBoolean("temaOscuro", temaOscuro)
                        intencion.putExtras(bundle)
                        startActivity(intencion)
                        finish()
                    },
                    alSeleccionarCiudad = { ciudad ->
                        val intencion = Intent(this, MapaActivity::class.java)
                        val bundle = Bundle()
                        bundle.putDouble("latitud", ciudad.latitud)
                        bundle.putDouble("longitud", ciudad.longitud)
                        bundle.putString("nombreCiudad", ciudad.nombre)
                        intencion.putExtras(bundle)
                        startActivity(intencion)
                    },
                    alOcultarCiudad = { idCiudad ->
                        DatosCompartidos.ocultarCiudad(idCiudad)
                        modeloVista.actualizarCiudadesOcultas(DatosCompartidos.obtenerCiudadesOcultas())
                    },
                    alMostrarCiudad = { idCiudad ->
                        DatosCompartidos.mostrarCiudad(idCiudad)
                        modeloVista.actualizarCiudadesOcultas(DatosCompartidos.obtenerCiudadesOcultas())
                    },
                    alMostrarMensaje = { mensaje ->
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLocalizaciones(
    modeloVista: LocalizacionesViewModel,
    usarFahrenheit: Boolean,
    alNavegarAInicio: () -> Unit,
    alNavegarAAjustes: () -> Unit,
    alSeleccionarCiudad: (CiudadEntidad) -> Unit,
    alOcultarCiudad: (Int) -> Unit,
    alMostrarCiudad: (Int) -> Unit,
    alMostrarMensaje: (String) -> Unit
) {
    val todasLasCiudadesDB by modeloVista.todasLasCiudadesDB.observeAsState(emptyList())
    val ciudadesFiltradas by modeloVista.ciudadesFiltradas.observeAsState(emptyList())
    
    var textoBusqueda by remember { mutableStateOf("") }
    var modoEliminacion by remember { mutableStateOf(false) }
    var ciudadesSeleccionadas by remember { mutableStateOf(setOf<Int>()) }
    var mostrarDialogoAnadir by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var ciudadAEliminar by remember { mutableStateOf<CiudadEntidad?>(null) }
    var mostrarDialogoConfirmarAnadir by remember { mutableStateOf(false) }
    var ciudadAAnadir by remember { mutableStateOf<CiudadEntidad?>(null) }
    
    val idCiudadPrincipal = DatosCompartidos.idCiudadSeleccionada
    val ciudadesOcultas = DatosCompartidos.obtenerCiudadesOcultas()

    LaunchedEffect(todasLasCiudadesDB) {
        modeloVista.actualizarListaFiltrada(todasLasCiudadesDB)
    }

    LaunchedEffect(textoBusqueda) {
        modeloVista.buscarCiudad(textoBusqueda)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ubicaciones_titulo),
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
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(painterResource(R.drawable.ic_ajustes), contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_ajustes)) },
                    selected = false,
                    onClick = alNavegarAAjustes
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Barra de búsqueda y botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.ubicaciones_buscar_ciudad)) },
                    leadingIcon = {
                        Icon(painterResource(R.drawable.ic_buscar), contentDescription = null)
                    },
                    singleLine = true
                )
                
                IconButton(onClick = { mostrarDialogoAnadir = true }) {
                    Icon(
                        painterResource(R.drawable.ic_anadir),
                        contentDescription = stringResource(R.string.ubicaciones_anadir),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = {
                        if (modoEliminacion) {
                            if (ciudadesSeleccionadas.isNotEmpty()) {
                                mostrarDialogoEliminar = true
                            } else {
                                modoEliminacion = false
                            }
                        } else {
                            modoEliminacion = true
                        }
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.ic_eliminar),
                        contentDescription = stringResource(R.string.ubicaciones_eliminar),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.ubicaciones_gestionar),
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Card usar ubicación actual
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { alMostrarMensaje("Disponible próximamente") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(R.drawable.ic_ubicacion),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.ubicaciones_usar_actual),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de ciudades
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ciudadesFiltradas) { ciudadConTiempo ->
                    ElementoLocalizacion(
                        ciudadConTiempo = ciudadConTiempo,
                        esCiudadPrincipal = ciudadConTiempo.ciudad.idCiudad == idCiudadPrincipal,
                        usarFahrenheit = usarFahrenheit,
                        modoEliminacion = modoEliminacion,
                        estaSeleccionada = ciudadesSeleccionadas.contains(ciudadConTiempo.ciudad.idCiudad),
                        alCambiarSeleccion = { seleccionada ->
                            ciudadesSeleccionadas = if (seleccionada) {
                                ciudadesSeleccionadas + ciudadConTiempo.ciudad.idCiudad
                            } else {
                                ciudadesSeleccionadas - ciudadConTiempo.ciudad.idCiudad
                            }
                        },
                        alPulsar = { alSeleccionarCiudad(ciudadConTiempo.ciudad) },
                        alEliminar = {
                            ciudadAEliminar = ciudadConTiempo.ciudad
                            mostrarDialogoEliminar = true
                        }
                    )
                }
            }
        }
    }
    
    // Diálogo añadir ciudad
    if (mostrarDialogoAnadir) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoAnadir = false },
            title = { Text(stringResource(R.string.ubicaciones_seleccionar_titulo)) },
            text = {
                LazyColumn {
                    items(todasLasCiudadesDB) { ciudadConTiempo ->
                        val estaOculta = ciudadesOcultas.contains(ciudadConTiempo.ciudad.idCiudad)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (estaOculta) {
                                        ciudadAAnadir = ciudadConTiempo.ciudad
                                        mostrarDialogoConfirmarAnadir = true
                                        mostrarDialogoAnadir = false
                                    }
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ciudadConTiempo.ciudad.nombre)
                            if (!estaOculta) {
                                Text("✓", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { mostrarDialogoAnadir = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }
    
    // Diálogo confirmar añadir
    if (mostrarDialogoConfirmarAnadir && ciudadAAnadir != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmarAnadir = false },
            title = { Text(stringResource(R.string.ubicaciones_anadir_titulo)) },
            text = { Text(stringResource(R.string.ubicaciones_anadir_mensaje, ciudadAAnadir!!.nombre)) },
            confirmButton = {
                TextButton(onClick = {
                    alMostrarCiudad(ciudadAAnadir!!.idCiudad)
                    mostrarDialogoConfirmarAnadir = false
                }) {
                    Text(stringResource(R.string.ubicaciones_anadir_btn))
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmarAnadir = false }) {
                    Text(stringResource(R.string.btn_cancelar))
                }
            }
        )
    }
    
    // Diálogo eliminar
    if (mostrarDialogoEliminar) {
        val cantidadAEliminar = if (modoEliminacion && ciudadesSeleccionadas.isNotEmpty()) {
            ciudadesSeleccionadas.size
        } else {
            1
        }
        
        AlertDialog(
            onDismissRequest = { 
                mostrarDialogoEliminar = false
                ciudadAEliminar = null
            },
            title = { Text(stringResource(R.string.ubicaciones_dialogo_eliminar_titulo)) },
            text = {
                if (modoEliminacion && ciudadesSeleccionadas.isNotEmpty()) {
                    Text(stringResource(R.string.ubicaciones_eliminar_multiple_mensaje, cantidadAEliminar))
                } else {
                    Text("${stringResource(R.string.ubicaciones_dialogo_eliminar_mensaje)} ${ciudadAEliminar?.nombre}?")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (modoEliminacion && ciudadesSeleccionadas.isNotEmpty()) {
                        if (ciudadesSeleccionadas.contains(idCiudadPrincipal)) {
                            alMostrarMensaje("No puedes eliminar la ciudad principal")
                        } else {
                            ciudadesSeleccionadas.forEach { id ->
                                alOcultarCiudad(id)
                            }
                        }
                        ciudadesSeleccionadas = emptySet()
                        modoEliminacion = false
                    } else {
                        ciudadAEliminar?.let { ciudad ->
                            if (ciudad.idCiudad == idCiudadPrincipal) {
                                alMostrarMensaje("No puedes eliminar la ciudad principal")
                            } else {
                                alOcultarCiudad(ciudad.idCiudad)
                            }
                        }
                    }
                    mostrarDialogoEliminar = false
                    ciudadAEliminar = null
                }) {
                    Text(stringResource(R.string.ubicaciones_eliminar))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    mostrarDialogoEliminar = false
                    ciudadAEliminar = null
                }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ElementoLocalizacion(
    ciudadConTiempo: CiudadConTiempoActual,
    esCiudadPrincipal: Boolean,
    usarFahrenheit: Boolean,
    modoEliminacion: Boolean,
    estaSeleccionada: Boolean,
    alCambiarSeleccion: (Boolean) -> Unit,
    alPulsar: () -> Unit,
    alEliminar: () -> Unit
) {
    val ciudad = ciudadConTiempo.ciudad
    val tiempo = ciudadConTiempo.tiempoActual
    val contexto = LocalContext.current
    
    val formatoDia = SimpleDateFormat("EEEE", Locale.getDefault())
    val diaSemana = formatoDia.format(Date()).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { alPulsar() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (modoEliminacion) {
                Checkbox(
                    checked = estaSeleccionada,
                    onCheckedChange = alCambiarSeleccion
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ciudad.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = diaSemana,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                tiempo?.let {
                    Text(
                        text = DatosCompartidos.traducirDescripcion(contexto, it.descripcion),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (esCiudadPrincipal) {
                        val tempMin = if (usarFahrenheit) (it.tempBaja * 9/5) + 32 else it.tempBaja
                        val tempMax = if (usarFahrenheit) (it.tempAlta * 9/5) + 32 else it.tempAlta
                        Text(
                            text = "MIN: ${tempMin.toInt()}° / MAX: ${tempMax.toInt()}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            tiempo?.let {
                val temp = if (usarFahrenheit) (it.temperatura * 9/5) + 32 else it.temperatura
                Text(
                    text = "${temp.toInt()}°",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            if (esCiudadPrincipal) {
                Icon(
                    painterResource(R.drawable.ic_home),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                tiempo?.let {
                    val iconoRes = when(it.codigoIcono) {
                        "ic_sol" -> R.drawable.ic_sol
                        "ic_lluvia" -> R.drawable.ic_lluvia
                        "ic_nieve" -> R.drawable.ic_nieve
                        "ic_nublado" -> R.drawable.ic_nublado
                        "ic_parcialmente_nublado" -> R.drawable.ic_parcialmente_nublado
                        "ic_tormenta" -> R.drawable.ic_tormenta
                        else -> R.drawable.ic_sol
                    }
                    Icon(
                        painterResource(iconoRes),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
