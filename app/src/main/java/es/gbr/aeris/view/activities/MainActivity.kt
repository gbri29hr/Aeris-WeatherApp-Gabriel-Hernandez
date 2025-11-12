package es.gbr.aeris.view.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import es.gbr.aeris.R
import es.gbr.aeris.databinding.ActivityMainBinding
import es.gbr.aeris.model.DatosCompartidos
import es.gbr.aeris.model.database.entities.TiempoActualEntidad
import es.gbr.aeris.view.adapters.PrediccionDiasAdapter
import es.gbr.aeris.view.adapters.PrediccionHorasAdapter
import es.gbr.aeris.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity principal de la aplicación que muestra el clima actual y pronósticos.
 * Implementa el patrón MVVM con ViewBinding y LiveData.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var vinculacion: ActivityMainBinding
    private val modeloVista: MainViewModel by viewModels()
    private lateinit var adaptadorHoras: PrediccionHorasAdapter
    private lateinit var adaptadorDias: PrediccionDiasAdapter
    private var latitudActual: Double = 0.0
    private var longitudActual: Double = 0.0

    // Preferencias del usuario recibidas mediante Bundle
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

        AppCompatDelegate.setDefaultNightMode(
            if (temaOscuro) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        vinculacion = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vinculacion.root)

        modeloVista.cambiarCiudadSeleccionada(DatosCompartidos.idCiudadSeleccionada)

        configurarRecyclerViews()
        observarDatos()
        configurarBotonSeleccionCiudad()
        configurarBotonScrollAbajo()

        vinculacion.bottomNavigation.selectedItemId = R.id.nav_home

        vinculacion.bottomNavigation.setOnItemSelectedListener { elemento ->
            when (elemento.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_localizaciones -> {
                    val intencion = Intent(this, LocalizacionesActivity::class.java)
                    val bundle = Bundle()
                    bundle.putBoolean("usarFahrenheit", usarFahrenheit)
                    bundle.putBoolean("usarMph", usarMph)
                    bundle.putBoolean("temaOscuro", temaOscuro)
                    intencion.putExtras(bundle)
                    startActivity(intencion)
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    val intencion = Intent(this, AjustesActivity::class.java)
                    val bundle = Bundle()
                    bundle.putBoolean("usarFahrenheit", usarFahrenheit)
                    bundle.putBoolean("usarMph", usarMph)
                    bundle.putBoolean("temaOscuro", temaOscuro)
                    intencion.putExtras(bundle)
                    startActivity(intencion)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Configura los RecyclerViews para mostrar pronósticos horarios y diarios.
     * Utiliza LayoutManagers horizontal y vertical.
     */
    private fun configurarRecyclerViews() {
        adaptadorHoras = PrediccionHorasAdapter(usarFahrenheit = usarFahrenheit)
        vinculacion.mainRecyclerHourly.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = adaptadorHoras
        }

        adaptadorDias = PrediccionDiasAdapter(usarFahrenheit = usarFahrenheit)
        vinculacion.mainRecyclerDaily.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = adaptadorDias
        }
    }

    /**
     * Observa cambios en los datos del ViewModel mediante LiveData.
     * Actualiza automáticamente cuando los datos cambian.
     */
    private fun observarDatos() {
        modeloVista.tiempoActual.observe(this) { tiempo ->
            if (tiempo != null) {
                actualizarVistaPrincipal(tiempo)
            }
        }

        modeloVista.prediccionHoras.observe(this) { listaHoras ->
            adaptadorHoras.actualizarDatos(listaHoras)
        }

        modeloVista.prediccionDiaria.observe(this) { listaDias ->
            adaptadorDias.actualizarDatos(listaDias)
        }


        modeloVista.idCiudadSeleccionada.observe(this) { idSeleccionado ->
            modeloVista.todasLasCiudades.value?.let { ciudades ->
                val ciudadActual = ciudades.find { it.idCiudad == idSeleccionado }
                if (ciudadActual != null) {
                    vinculacion.mainTextCity.text = ciudadActual.nombre
                    latitudActual = ciudadActual.latitud
                    longitudActual = ciudadActual.longitud
                }
            }
        }


        modeloVista.todasLasCiudades.observe(this) { ciudades ->
            modeloVista.idCiudadSeleccionada.value?.let { idSeleccionado ->
                val ciudadActual = ciudades.find { it.idCiudad == idSeleccionado }
                if (ciudadActual != null) {
                    vinculacion.mainTextCity.text = ciudadActual.nombre
                    latitudActual = ciudadActual.latitud
                    longitudActual = ciudadActual.longitud
                }
            }
        }
    }

    private fun actualizarVistaPrincipal(tiempo: TiempoActualEntidad) {

        val temperatura = if (usarFahrenheit) celsiusAFahrenheit(tiempo.temperatura) else tiempo.temperatura
        val maxima = if (usarFahrenheit) celsiusAFahrenheit(tiempo.tempAlta) else tiempo.tempAlta
        val minima = if (usarFahrenheit) celsiusAFahrenheit(tiempo.tempBaja) else tiempo.tempBaja
        val viento = if (usarMph) kphAMph(tiempo.vientoVelocidad) else tiempo.vientoVelocidad

        val unidadViento = if (usarMph) "mph" else "km/h"

        vinculacion.mainTextTemperature.text = "${temperatura.toInt()}°"
        vinculacion.mainTextDescription.text = DatosCompartidos.traducirDescripcion(this, tiempo.descripcion)
        vinculacion.mainTextMin.text = "MIN: ${minima.toInt()}°"
        vinculacion.mainTextMax.text = "MAX: ${maxima.toInt()}°"

        // Mostrar la fecha actual con idioma del sistema
        val formateadorFecha = SimpleDateFormat("EEEE, MMM dd, HH:mm", Locale.getDefault())
        vinculacion.mainTextDate.text = formateadorFecha.format(Date())

        // Actualizar detalles del clima
        vinculacion.detailHumedad.text = "${tiempo.humedad.toInt()}%"
        vinculacion.detailViento.text = "${viento.toInt()} $unidadViento"
        vinculacion.detailUv.text = "${tiempo.uvIndice.toInt()} (${obtenerNivelUV(tiempo.uvIndice.toInt())})"
        
        // Usar el valor fijo de precipitación de la base de datos
        vinculacion.detailPrecipitacion.text = "${tiempo.precipitacion}%"

        // Cargar icono del clima
        cargarIconoClima(tiempo.codigoIcono)
    }

    private fun cargarIconoClima(codigoIcono: String) {
        val idRecurso = when(codigoIcono) {
            "ic_sol" -> R.drawable.ic_sol
            "ic_lluvia" -> R.drawable.ic_lluvia
            "ic_nieve" -> R.drawable.ic_nieve
            "ic_nublado" -> R.drawable.ic_nublado
            "ic_parcialmente_nublado" -> R.drawable.ic_parcialmente_nublado
            "ic_tormenta" -> R.drawable.ic_tormenta
            else -> {
                val id = resources.getIdentifier(codigoIcono, "drawable", packageName)
                if (id != 0) id else R.drawable.ic_sol
            }
        }
        vinculacion.mainImageWeather.setImageResource(idRecurso)
    }

    private fun obtenerNivelUV(indice: Int): String {
        return when {
            indice <= 2 -> getString(R.string.main_bajo)
            indice <= 5 -> getString(R.string.main_medio)
            else -> getString(R.string.main_alto)
        }
    }

    // Convierte Celsius a Fahrenheit
    private fun celsiusAFahrenheit(celsius: Double): Double = (celsius * 9/5) + 32

    // Convierte km/h a mph
    private fun kphAMph(kph: Double): Double = kph / 1.609

    // Configurar botón de selección de ciudad
    private fun configurarBotonSeleccionCiudad() {
        vinculacion.mainButtonSelectCity.setOnClickListener {
            mostrarDialogoSeleccionCiudad()
        }
    }

    private fun configurarBotonScrollAbajo() {
        vinculacion.mainButtonScrollDown.setOnClickListener {
            // Hacer scroll hasta el título "PRONOSTICO HORARIO"
            vinculacion.mainScrollView.post {
                vinculacion.mainScrollView.smoothScrollTo(0, vinculacion.mainTitleHourly.top)
            }
        }
    }

    private fun mostrarDialogoSeleccionCiudad() {
        modeloVista.todasLasCiudades.value?.let { ciudades ->
            if (ciudades.isEmpty()) {
                return
            }

            val nombresCiudades = ciudades.map { it.nombre }.toTypedArray()
            val idActual = modeloVista.tiempoActual.value?.idCiudadFk ?: 1
            val posicionActual = ciudades.indexOfFirst { it.idCiudad == idActual }

            AlertDialog.Builder(this)
                .setTitle(R.string.main_seleccionar_ciudad_titulo)
                .setSingleChoiceItems(nombresCiudades, posicionActual) { dialogo, posicion ->
                    val ciudadSeleccionada = ciudades[posicion]
                    modeloVista.cambiarCiudadSeleccionada(ciudadSeleccionada.idCiudad)
                    dialogo.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar preferencias y actualizar vista cuando volvemos a la activity
        modeloVista.tiempoActual.value?.let { actualizarVistaPrincipal(it) }
    }
}
