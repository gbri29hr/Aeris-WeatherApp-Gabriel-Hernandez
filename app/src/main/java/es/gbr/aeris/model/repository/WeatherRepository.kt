package es.gbr.aeris.model.repository

import androidx.lifecycle.LiveData
import es.gbr.aeris.model.database.dao.WeatherDao
import es.gbr.aeris.model.database.entities.*
import es.gbr.aeris.model.database.relations.CiudadConTiempoActual

// Capa de abstracción entre ViewModel y datos
class WeatherRepository(private val weatherDao: WeatherDao) {

    fun obtenerTodasLasCiudades(): LiveData<List<CiudadEntidad>> {
        return weatherDao.obtenerTodasLasCiudades()
    }

    fun obtenerTiempoActual(idCiudad: Int): LiveData<TiempoActualEntidad> {
        return weatherDao.obtenerTiempoActual(idCiudad)
    }

    fun obtenerPrediccionHoras(idCiudad: Int): LiveData<List<PrediccionHorasEntidad>> {
        return weatherDao.obtenerPrediccionHoras(idCiudad)
    }

    /**
     * Obtiene la predicción diaria de una ciudad.
     * @param idCiudad ID de la ciudad
     * @return LiveData con lista de predicciones diarias
     */
    fun obtenerPrediccionDiaria(idCiudad: Int): LiveData<List<PrediccionDiariaEntidad>> {
        return weatherDao.obtenerPrediccionDiaria(idCiudad)
    }

    /**
     * Obtiene todas las ciudades junto con su tiempo actual.
     * Utiliza una relación de Room para combinar datos de ambas tablas.
     * @return LiveData con lista de ciudades y su tiempo actual
     */
    fun obtenerCiudadesConTiempoActual(): LiveData<List<CiudadConTiempoActual>> {
        return weatherDao.obtenerCiudadesConTiempoActual()
    }

    /**
     * Inserta una nueva ciudad en la base de datos.
     * @param ciudad Entidad de ciudad a insertar
     * @return ID de la ciudad insertada
     */
    suspend fun insertarCiudad(ciudad: CiudadEntidad): Long {
        return weatherDao.insertarCiudad(ciudad)
    }
}