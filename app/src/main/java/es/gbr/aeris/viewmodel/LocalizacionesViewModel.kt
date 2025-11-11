package es.gbr.aeris.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import es.gbr.aeris.model.DatosCompartidos
import es.gbr.aeris.model.database.database.AppDataBase
import es.gbr.aeris.model.database.relations.CiudadConTiempoActual
import es.gbr.aeris.model.repository.WeatherRepository

/**
 * ViewModel para gestionar las ubicaciones (ciudades).
 * Maneja el filtrado de ciudades según búsqueda del usuario y ciudades ocultas.
 */
class LocalizacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WeatherRepository
    val todasLasCiudadesDB: LiveData<List<CiudadConTiempoActual>>

    // Variables para el filtrado
    private var textoBusquedaActual: String = ""
    private var ciudadesOcultasActuales: Set<Int> = emptySet()

    // LiveData mutable para las ciudades filtradas
    private val _ciudadesFiltradas = MutableLiveData<List<CiudadConTiempoActual>>()
    val ciudadesFiltradas: LiveData<List<CiudadConTiempoActual>> = _ciudadesFiltradas

    init {
        val weatherDao = AppDataBase.obtenerBaseDeDatos(application).weatherDao()
        repository = WeatherRepository(weatherDao)
        
        todasLasCiudadesDB = repository.obtenerCiudadesConTiempoActual()
        ciudadesOcultasActuales = DatosCompartidos.obtenerCiudadesOcultas()

        _ciudadesFiltradas.value = emptyList()
    }

    /**
     * Actualiza la lista de ciudades aplicando los filtros actuales.
     * Debe ser llamado cada vez que cambian los datos de origen.
     */
    fun actualizarListaFiltrada(todasLasCiudades: List<CiudadConTiempoActual>) {
        val listaFiltrada = todasLasCiudades
            // Primero filtrar ciudades ocultas
            .filter { ciudadConTiempo ->
                !ciudadesOcultasActuales.contains(ciudadConTiempo.ciudad.idCiudad)
            }
            // Luego filtrar por búsqueda
            .filter { ciudadConTiempo ->
                if (textoBusquedaActual.isEmpty()) {
                    true
                } else {
                    ciudadConTiempo.ciudad.nombre.contains(textoBusquedaActual, ignoreCase = true)
                }
            }
        _ciudadesFiltradas.value = listaFiltrada
    }

    /**
     * Actualiza el texto de búsqueda y reaplica el filtro.
     */
    fun buscarCiudad(texto: String) {
        textoBusquedaActual = texto
        todasLasCiudadesDB.value?.let { actualizarListaFiltrada(it) }
    }

    /**
     * Actualiza el conjunto de ciudades ocultas y reaplica el filtro.
     */
    fun actualizarCiudadesOcultas(ocultas: Set<Int>) {
        ciudadesOcultasActuales = ocultas
        todasLasCiudadesDB.value?.let { actualizarListaFiltrada(it) }
    }
}
