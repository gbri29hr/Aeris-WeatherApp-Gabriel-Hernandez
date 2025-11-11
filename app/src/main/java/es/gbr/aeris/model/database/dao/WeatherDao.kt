package es.gbr.aeris.model.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import es.gbr.aeris.model.database.entities.*
import es.gbr.aeris.model.database.relations.CiudadConTiempoActual

/**
 * DAO (Data Access Object) para acceder a la base de datos Room.
 * Define todas las operaciones de consulta e inserción para las tablas del clima.
 */
@Dao
interface WeatherDao {


    @Query("SELECT * FROM ciudades")
    fun obtenerTodasLasCiudades(): LiveData<List<CiudadEntidad>>


    @Query("SELECT * FROM tiempo_actual WHERE id_ciudad_fk = :idCiudad")
    fun obtenerTiempoActual(idCiudad: Int): LiveData<TiempoActualEntidad>


    @Query("SELECT * FROM prediccion_hora WHERE id_ciudad_fk = :idCiudad ORDER BY id_hora ASC")
    fun obtenerPrediccionHoras(idCiudad: Int): LiveData<List<PrediccionHorasEntidad>>

    @Query("SELECT * FROM prediccion_dia WHERE id_ciudad_fk = :idCiudad ORDER BY id_dia ASC")
    fun obtenerPrediccionDiaria(idCiudad: Int): LiveData<List<PrediccionDiariaEntidad>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCiudad(ciudad: CiudadEntidad): Long


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTiempoActual(tiempo: TiempoActualEntidad)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarListaHoras(listaHoras: List<PrediccionHorasEntidad>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarListaDias(listaDias: List<PrediccionDiariaEntidad>)


    @Query("DELETE FROM ciudades WHERE id_ciudad = :idCiudad")
    suspend fun eliminarCiudadPorId(idCiudad: Int)


    @Transaction
    @Query("SELECT * FROM ciudades")
    fun obtenerCiudadesConTiempoActual(): LiveData<List<CiudadConTiempoActual>>
}
