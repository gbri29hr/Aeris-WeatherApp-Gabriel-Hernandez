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
 * ViewModel principal que gestiona los datos del clima para MainActivity.
 * Implementa el patrón MVVM actuando como intermediario entre la vista y el repositorio.
 *
 * Responsabilidades:
 * - Obtener y gestionar datos del clima desde el repositorio
 * - Mantener el estado de la ciudad seleccionada
 * - Proporcionar LiveData observables para la actualización reactiva de la UI
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Repositorio que maneja el acceso a la base de datos
    private val repository: WeatherRepository

    // LiveData con todas las ciudades disponibles
    val todasLasCiudades: LiveData<List<CiudadEntidad>>

    // MutableLiveData privado para controlar cambios de ciudad internamente
    private val _idCiudadSeleccionada = MutableLiveData<Int>()

    // LiveData público de solo lectura para observar la ciudad seleccionada
    val idCiudadSeleccionada: LiveData<Int> = _idCiudadSeleccionada

    init {
        // Inicializar base de datos y repositorio
        val weatherDao = AppDataBase.obtenerBaseDeDatos(application).weatherDao()
        repository = WeatherRepository(weatherDao)

        // Cargar todas las ciudades disponibles
        todasLasCiudades = repository.obtenerTodasLasCiudades()

        // Establecer la ciudad seleccionada desde datos compartidos
        _idCiudadSeleccionada.value = DatosCompartidos.idCiudadSeleccionada
    }

    /**
     * LiveData del tiempo actual que se actualiza automáticamente cuando cambia
     * la ciudad seleccionada usando switchMap.
     */
    val tiempoActual: LiveData<TiempoActualEntidad> = _idCiudadSeleccionada.switchMap { id ->
        repository.obtenerTiempoActual(id)
    }

    /**
     * LiveData de las predicciones horarias (24 horas) que se actualiza automáticamente
     * cuando cambia la ciudad seleccionada.
     */
    val prediccionHoras: LiveData<List<PrediccionHorasEntidad>> = _idCiudadSeleccionada.switchMap { id ->
        repository.obtenerPrediccionHoras(id)
    }

    /**
     * LiveData de la predicción diaria (7 días) que se actualiza automáticamente
     * cuando cambia la ciudad seleccionada.
     */
    val prediccionDiaria: LiveData<List<PrediccionDiariaEntidad>> = _idCiudadSeleccionada.switchMap { id ->
        repository.obtenerPrediccionDiaria(id)
    }

    /**
     * Cambia la ciudad seleccionada y actualiza el valor en DatosCompartidos.
     * Esto desencadena automáticamente la actualización de todos los LiveData relacionados.
     *
     * @param nuevoIdCiudad ID de la nueva ciudad a seleccionar
     */
    fun cambiarCiudadSeleccionada(nuevoIdCiudad: Int) {
        _idCiudadSeleccionada.value = nuevoIdCiudad
        DatosCompartidos.idCiudadSeleccionada = nuevoIdCiudad
    }
}