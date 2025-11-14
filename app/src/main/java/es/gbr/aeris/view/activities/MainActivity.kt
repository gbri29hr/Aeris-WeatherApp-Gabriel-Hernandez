package es.gbr.aeris.view.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
 * Activity principal de la aplicación que muestra el clima actual y los pronósticos.
 *
 * Funcionalidades:
 * - Muestra temperatura actual, descripción del clima e icono
 * - Presenta temperaturas mínima y máxima del día
 * - RecyclerView horizontal con pronóstico por horas (24 horas)
 * - RecyclerView vertical con pronóstico diario (7 días)
 * - Cards con datos detallados: humedad, viento, índice UV y precipitación
 * - Selector de ciudad mediante AlertDialog
 * - Conversión automática de unidades (°C/°F, km/h/mph)
 * - Navegación inferior (BottomNavigationView)
 *
 * Arquitectura MVVM: Esta Activity observa cambios en el ViewModel mediante LiveData
 * y actualiza la UI automáticamente cuando los datos cambian.
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding para acceso seguro a las vistas del layout
    private lateinit var vinculacion: ActivityMainBinding

    // ViewModel que gestiona los datos del clima (arquitectura MVVM)
    private val modeloVista: MainViewModel by viewModels()

    // Adaptadores para los RecyclerViews de pronósticos
    private lateinit var adaptadorHoras: PrediccionHorasAdapter
    private lateinit var adaptadorDias: PrediccionDiasAdapter

    // Preferencias del usuario recibidas mediante Bundle desde otras Activities
    private var usarFahrenheit: Boolean = false  // true = Fahrenheit, false = Celsius
    private var usarMph: Boolean = false  // true = MPH, false = Km/h
    private var temaOscuro: Boolean = false  // true = Oscuro, false = Claro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperar preferencias del usuario desde el Bundle (Intent)
        // Estas preferencias vienen de InicioActivity o AjustesActivity
        intent.extras?.let {
            usarFahrenheit = it.getBoolean("usarFahrenheit", false)
            usarMph = it.getBoolean("usarMph", false)
            temaOscuro = it.getBoolean("temaOscuro", false)
        }

        // Inicializar ViewBinding para acceso a las vistas
        vinculacion = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vinculacion.root)

        // Establecer la ciudad seleccionada en el ViewModel desde datos compartidos
        modeloVista.cambiarCiudadSeleccionada(DatosCompartidos.idCiudadSeleccionada)

        // Configurar componentes de la interfaz
        configurarRecyclerViews()
        observarDatos()
        configurarBotonSeleccionCiudad()
        configurarBotonScrollAbajo()

        // Marcar la opción "Inicio" como seleccionada en el menú de navegación
        vinculacion.bottomNavigation.selectedItemId = R.id.nav_home

        // Configurar listeners para el menú de navegación inferior
        vinculacion.bottomNavigation.setOnItemSelectedListener { elemento ->
            when (elemento.itemId) {
                R.id.nav_home -> {
                    // Ya estamos en Home, no hacer nada
                    true
                }
                R.id.nav_localizaciones -> {
                    // Navegar a LocalizacionesActivity pasando las preferencias mediante Bundle
                    val intencion = Intent(this, LocalizacionesActivity::class.java)
                    val bundle = Bundle()
                    bundle.putBoolean("usarFahrenheit", usarFahrenheit)
                    bundle.putBoolean("usarMph", usarMph)
                    bundle.putBoolean("temaOscuro", temaOscuro)
                    intencion.putExtras(bundle)
                    startActivity(intencion)
                    finish()  // Finalizar esta Activity para evitar acumulación en el stack
                    true
                }
                R.id.nav_settings -> {
                    // Navegar a AjustesActivity pasando las preferencias mediante Bundle
                    val intencion = Intent(this, AjustesActivity::class.java)
                    val bundle = Bundle()
                    bundle.putBoolean("usarFahrenheit", usarFahrenheit)
                    bundle.putBoolean("usarMph", usarMph)
                    bundle.putBoolean("temaOscuro", temaOscuro)
                    intencion.putExtras(bundle)
                    startActivity(intencion)
                    finish()  // Finalizar esta Activity para evitar acumulación en el stack
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
                }
            }
        }


        modeloVista.todasLasCiudades.observe(this) { ciudades ->
            modeloVista.idCiudadSeleccionada.value?.let { idSeleccionado ->
                val ciudadActual = ciudades.find { it.idCiudad == idSeleccionado }
                if (ciudadActual != null) {
                    vinculacion.mainTextCity.text = ciudadActual.nombre
                }
            }
        }
    }

    /**
     * Actualiza todos los elementos de la vista principal con los datos del clima.
     * Realiza conversiones de unidades según las preferencias del usuario.
     *
     * @param tiempo Entidad con los datos del tiempo actual de la base de datos
     */
    private fun actualizarVistaPrincipal(tiempo: TiempoActualEntidad) {
        // Convertir temperaturas según preferencia del usuario
        val temperatura = if (usarFahrenheit) celsiusAFahrenheit(tiempo.temperatura) else tiempo.temperatura
        val maxima = if (usarFahrenheit) celsiusAFahrenheit(tiempo.tempAlta) else tiempo.tempAlta
        val minima = if (usarFahrenheit) celsiusAFahrenheit(tiempo.tempBaja) else tiempo.tempBaja

        // Convertir velocidad del viento según preferencia del usuario
        val viento = if (usarMph) kphAMph(tiempo.vientoVelocidad) else tiempo.vientoVelocidad
        val unidadViento = if (usarMph) "mph" else "km/h"

        // Actualizar textos principales del clima
        vinculacion.mainTextTemperature.text = "${temperatura.toInt()}°"
        vinculacion.mainTextDescription.text = DatosCompartidos.traducirDescripcion(this, tiempo.descripcion)
        vinculacion.mainTextMin.text = "MIN: ${minima.toInt()}°"
        vinculacion.mainTextMax.text = "MAX: ${maxima.toInt()}°"

        // Mostrar la fecha actual con formato localizado según idioma del sistema
        val formateadorFecha = SimpleDateFormat("EEEE, MMM dd, HH:mm", Locale.getDefault())
        vinculacion.mainTextDate.text = formateadorFecha.format(Date())

        // Actualizar cards con detalles del clima
        vinculacion.detailHumedad.text = "${tiempo.humedad.toInt()}%"
        vinculacion.detailViento.text = "${viento.toInt()} $unidadViento"
        vinculacion.detailUv.text = "${tiempo.uvIndice.toInt()} (${obtenerNivelUV(tiempo.uvIndice.toInt())})"
        vinculacion.detailPrecipitacion.text = "${tiempo.precipitacion}%"

        // Cargar y mostrar el icono del clima correspondiente
        cargarIconoClima(tiempo.codigoIcono)
    }

    /**
     * Carga el icono del clima según el código recibido de la base de datos.
     *
     * @param codigoIcono Nombre del drawable del icono (ej: "ic_sol", "ic_lluvia")
     */
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

    /**
     * Determina el nivel de riesgo del índice UV según estándares internacionales.
     *
     * @param indice Valor numérico del índice UV
     * @return String localizado: "Bajo", "Medio" o "Alto"
     */
    private fun obtenerNivelUV(indice: Int): String {
        return when {
            indice <= 2 -> getString(R.string.main_bajo)
            indice <= 5 -> getString(R.string.main_medio)
            else -> getString(R.string.main_alto)
        }
    }

    /**
     * Convierte temperatura de Celsius a Fahrenheit.
     * Fórmula: F = (C × 9/5) + 32
     */
    private fun celsiusAFahrenheit(celsius: Double): Double = (celsius * 9/5) + 32

    /**
     * Convierte velocidad de km/h a millas por hora.
     * Factor: 1 mph = 1.609 km/h
     */
    private fun kphAMph(kph: Double): Double = kph / 1.609

    /**
     * Configura el botón para mostrar el diálogo de selección de ciudad.
     */
    private fun configurarBotonSeleccionCiudad() {
        vinculacion.mainButtonSelectCity.setOnClickListener {
            mostrarDialogoSeleccionCiudad()
        }
    }

    /**
     * Configura el FAB para hacer scroll suave hacia el pronóstico horario.
     */
    private fun configurarBotonScrollAbajo() {
        vinculacion.mainButtonScrollDown.setOnClickListener {
            // Hacer scroll hasta el título "PRONOSTICO HORARIO"
            vinculacion.mainScrollView.post {
                vinculacion.mainScrollView.smoothScrollTo(0, vinculacion.mainTitleHourly.top)
            }
        }
    }

    /**
     * Muestra un AlertDialog con todas las ciudades disponibles.
     * Al seleccionar una ciudad, actualiza automáticamente todos los datos.
     */
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

    /**
     * Método del ciclo de vida. Refresca la vista cuando vuelve a primer plano.
     */
    override fun onResume() {
        super.onResume()
        // Recargar vista cuando volvemos a la activity
        modeloVista.tiempoActual.value?.let { actualizarVistaPrincipal(it) }
    }
}
