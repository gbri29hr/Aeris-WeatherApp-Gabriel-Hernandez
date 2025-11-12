package es.gbr.aeris.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import es.gbr.aeris.model.DatosCompartidos
import es.gbr.aeris.model.database.database.AppDataBase
import es.gbr.aeris.model.database.entities.CiudadEntidad
import es.gbr.aeris.model.database.entities.PrediccionDiariaEntidad
import es.gbr.aeris.model.database.entities.PrediccionHorasEntidad
import es.gbr.aeris.model.database.entities.TiempoActualEntidad
import es.gbr.aeris.model.repository.WeatherRepository

/**
 * ViewModel principal de la aplicación que gestiona los datos del clima.
 * Implementa el patrón MVVM, actuando como intermediario entre la UI y el repositorio.
 *
 * Utiliza LiveData para observar cambios en los datos de manera reactiva.
 * Los datos se actualizan automáticamente cuando cambia la ciudad seleccionada.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WeatherRepository

    // Lista de todas las ciudades
    val todasLasCiudades: LiveData<List<CiudadEntidad>>

    // ID de la ciudad seleccionada
    private val _idCiudadSeleccionada = MutableLiveData<Int>()
    val idCiudadSeleccionada: LiveData<Int> = _idCiudadSeleccionada

    init {
        val weatherDao = AppDataBase.obtenerBaseDeDatos(application).weatherDao()
        repository = WeatherRepository(weatherDao)
        todasLasCiudades = repository.obtenerTodasLasCiudades()
        _idCiudadSeleccionada.value = DatosCompartidos.idCiudadSeleccionada
    }

    /**
     * LiveData del tiempo actual que se actualiza automáticamente cuando cambia la ciudad.
     * Utiliza switchMap (No visto en clase pero necesario para mi aplicacion) para cambiar la fuente de datos de forma reactiva.
     */

    val tiempoActual: LiveData<TiempoActualEntidad> = _idCiudadSeleccionada.switchMap { id ->
        repository.obtenerTiempoActual(id)
    }

    /**
     * LiveData de la predicción por horas que se actualiza con la ciudad seleccionada.
     */
    val prediccionHoras: LiveData<List<PrediccionHorasEntidad>> = _idCiudadSeleccionada.switchMap { id ->
        repository.obtenerPrediccionHoras(id)
    }

    /**
     * LiveData de la predicción diaria que se actualiza con la ciudad seleccionada.
     */
    val prediccionDiaria: LiveData<List<PrediccionDiariaEntidad>> = _idCiudadSeleccionada.switchMap { id ->
        repository.obtenerPrediccionDiaria(id)
    }

    /**
     * Cambia la ciudad seleccionada, lo que desencadena la actualización automática
     * de todos los LiveData relacionados (tiempo actual, predicciones, etc.).
     */
    fun cambiarCiudadSeleccionada(nuevoIdCiudad: Int) {
        _idCiudadSeleccionada.value = nuevoIdCiudad
        DatosCompartidos.idCiudadSeleccionada = nuevoIdCiudad
    }
}