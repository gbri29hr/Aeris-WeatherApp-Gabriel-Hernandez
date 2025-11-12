package es.gbr.aeris.model.database.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import es.gbr.aeris.model.database.dao.WeatherDao
import es.gbr.aeris.model.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Base de datos principal de la aplicación usando Room.
 * Contiene todas las entidades relacionadas con el clima y ubicaciones.
 * 
 * Utiliza el patrón Singleton para garantizar una única instancia de la BD.
 * La versión 6 incluye todas las tablas necesarias para el funcionamiento completo.
 */
@Database(
    entities = [
        CiudadEntidad::class,
        TiempoActualEntidad::class,
        PrediccionHorasEntidad::class,
        PrediccionDiariaEntidad::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {

    /** DAO para acceder a todas las operaciones de base de datos */
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        /**
         * Obtiene la instancia única de la base de datos.
         * Si no existe, la crea.
         * 
         * @param context Contexto de la aplicación
         * @return Instancia singleton de AppDataBase
         */
        fun obtenerBaseDeDatos(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "weather_app.db"
                )
                    .fallbackToDestructiveMigration() // Permite recrear la BD si cambia la versión
                    .addCallback(DatabaseCallback()) // Callback para poblar datos iniciales
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }


    private class DatabaseCallback : Callback() {

        /**
         * Se ejecuta cuando la base de datos se crea por primera vez.
         */
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.weatherDao())
                }
            }
        }
        
        /**
         * Genera 24 horas de predicción del tiempo con variaciones realistas.
         * Simula el ciclo de temperatura durante el día usando función seno.
         * 
         * @param cityId ID de la ciudad para la que se genera la predicción
         * @param tempBase Temperatura base en Celsius
         * @param iconoBase Código del icono del clima
         * @return Lista de 24 predicciones horarias
         */
        private fun generarPrediccion24Horas(cityId: Int, tempBase: Double, iconoBase: String): List<PrediccionHorasEntidad> {
            val horas = mutableListOf<PrediccionHorasEntidad>()
            for (h in 0..23) {
                // Variación de temperatura durante el día simulando ciclo natural
                // Más fría a las 6 AM, más cálida a las 18 PM (±5°C de variación)
                // Metodo matematico solicitado a la IA para una aplicacion mas realista
                val variacion = sin((h - 6) * Math.PI / 12) * 5
                horas.add(PrediccionHorasEntidad(
                    idCiudadFk = cityId,
                    hora = String.format("%02d:00", h),
                    temperatura = tempBase + variacion,
                    codigoIcono = iconoBase
                ))
            }
            return horas
        }
        
        /**
         * Genera predicción para 7 días con temperaturas variadas.
         * Utiliza los 6 tipos de iconos disponibles en la app para variedad visual.
         * 
         * @param cityId ID de la ciudad para la que se genera la predicción
         * @param tempAlta Temperatura máxima base
         * @param tempBaja Temperatura mínima base
         * @param iconoBase Código del icono base (puede variar en los días)
         * @return Lista de 7 predicciones diarias
         */
        private fun generarPrediccion7Dias(cityId: Int, tempAlta: Double, tempBaja: Double, iconoBase: String): List<PrediccionDiariaEntidad> {
            val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
            // Variedad de iconos disponibles en la aplicación para representar diferentes estados del clima
            val iconos = listOf("ic_sol", "ic_parcialmente_nublado", "ic_nublado", "ic_sol", "ic_parcialmente_nublado", "ic_sol", "ic_lluvia")
            return dias.mapIndexed { index, dia ->
                PrediccionDiariaEntidad(
                    idCiudadFk = cityId,
                    nombreDia = dia,
                    // Ligera variación de temperatura a lo largo de la semana
                    tempAlta = tempAlta + (index - 3) * 0.5,
                    tempBaja = tempBaja + (index - 3) * 0.3,
                    codigoIcono = iconos[index]
                )
            }
        }

        /**
         * Puebla la base de datos con datos iniciales de 20 ciudades españolas.
         * Para cada ciudad inserta: información básica, tiempo actual, predicción 24h y 7 días.
         */
        suspend fun populateDatabase(weatherDao: WeatherDao) {

            // --- 1. CIUDAD REAL ---
            var cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Ciudad Real", latitud = 38.9863, longitud = -3.9271))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 28.0, descripcion = "Soleado",
                codigoIcono = "ic_sol", tempAlta = 30.0, tempBaja = 14.0,
                humedad = 25.0, vientoVelocidad = 5.0, uvIndice = 8.0, precipitacion = 5
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 28.0, "ic_sol"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 30.0, 14.0, "ic_sol"))

            // --- 2. MADRID ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Madrid", latitud = 40.4167, longitud = -3.7038))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 25.0, descripcion = "Soleado",
                codigoIcono = "ic_sol", tempAlta = 28.0, tempBaja = 15.0,
                humedad = 30.0, vientoVelocidad = 10.0, uvIndice = 7.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 25.0, "ic_sol"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 28.0, 15.0, "ic_sol"))

            // --- 3. BARCELONA ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Barcelona", latitud = 41.3888, longitud = 2.159))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 22.0, descripcion = "Parcialmente nublado",
                codigoIcono = "ic_parcialmente_nublado", tempAlta = 24.0, tempBaja = 18.0,
                humedad = 60.0, vientoVelocidad = 15.0, uvIndice = 5.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 22.0, "ic_parcialmente_nublado"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 24.0, 18.0, "ic_parcialmente_nublado"))

            // --- 4. VALENCIA ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Valencia", latitud = 39.4699, longitud = -0.3763))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 26.0, descripcion = "Nublado",
                codigoIcono = "ic_nublado", tempAlta = 29.0, tempBaja = 20.0,
                humedad = 55.0, vientoVelocidad = 12.0, uvIndice = 6.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 26.0, "ic_nublado"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 29.0, 20.0, "ic_nublado"))

            // --- 5. SEVILLA ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Sevilla", latitud = 37.3891, longitud = -5.9845))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 31.0, descripcion = "Soleado",
                codigoIcono = "ic_sol", tempAlta = 34.0, tempBaja = 19.0,
                humedad = 20.0, vientoVelocidad = 8.0, uvIndice = 9.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 31.0, "ic_sol"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 34.0, 19.0, "ic_sol"))

            // --- 6. ZARAGOZA ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Zaragoza", latitud = 41.6488, longitud = -0.8891))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 24.0, descripcion = "Parcialmente nublado",
                codigoIcono = "ic_parcialmente_nublado", tempAlta = 27.0, tempBaja = 14.0,
                humedad = 35.0, vientoVelocidad = 25.0, uvIndice = 7.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 24.0, "ic_parcialmente_nublado"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 27.0, 14.0, "ic_parcialmente_nublado"))

            // --- 7. MÁLAGA ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Málaga", latitud = 36.7213, longitud = -4.4214))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 27.0, descripcion = "Soleado",
                codigoIcono = "ic_sol", tempAlta = 29.0, tempBaja = 21.0,
                humedad = 50.0, vientoVelocidad = 10.0, uvIndice = 8.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 27.0, "ic_sol"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 29.0, 21.0, "ic_sol"))

            // --- 8. MURCIA ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Murcia", latitud = 37.9922, longitud = -1.1307))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 29.0, descripcion = "Soleado",
                codigoIcono = "ic_sol", tempAlta = 32.0, tempBaja = 18.0,
                humedad = 30.0, vientoVelocidad = 7.0, uvIndice = 9.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 29.0, "ic_sol"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 32.0, 18.0, "ic_sol"))

            // --- 9. PALMA DE MALLORCA ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Palma", latitud = 39.5696, longitud = 2.6502))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 25.0, descripcion = "Parcialmente nublado",
                codigoIcono = "ic_parcialmente_nublado", tempAlta = 27.0, tempBaja = 20.0,
                humedad = 65.0, vientoVelocidad = 14.0, uvIndice = 6.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 25.0, "ic_parcialmente_nublado"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 27.0, 20.0, "ic_parcialmente_nublado"))

            // --- 10. LAS PALMAS DE GRAN CANARIA ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Las Palmas", latitud = 28.1248, longitud = -15.43))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 23.0, descripcion = "Parcialmente nublado",
                codigoIcono = "ic_parcialmente_nublado", tempAlta = 25.0, tempBaja = 19.0,
                humedad = 70.0, vientoVelocidad = 18.0, uvIndice = 7.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 23.0, "ic_parcialmente_nublado"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 25.0, 19.0, "ic_parcialmente_nublado"))

            // --- 11. BILBAO ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Bilbao", latitud = 43.2630, longitud = -2.9350))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 18.0, descripcion = "Lluvioso",
                codigoIcono = "ic_lluvia", tempAlta = 20.0, tempBaja = 14.0,
                humedad = 80.0, vientoVelocidad = 10.0, uvIndice = 3.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 18.0, "ic_lluvia"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 20.0, 14.0, "ic_lluvia"))

            // --- 12. ALICANTE ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Alicante", latitud = 38.3452, longitud = -0.4810))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 26.0, descripcion = "Soleado",
                codigoIcono = "ic_sol", tempAlta = 28.0, tempBaja = 20.0,
                humedad = 58.0, vientoVelocidad = 11.0, uvIndice = 7.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 26.0, "ic_sol"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 28.0, 20.0, "ic_sol"))

            // --- 13. CÓRDOBA ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Córdoba", latitud = 37.8882, longitud = -4.7794))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 30.0, descripcion = "Soleado",
                codigoIcono = "ic_sol", tempAlta = 33.0, tempBaja = 18.0,
                humedad = 22.0, vientoVelocidad = 9.0, uvIndice = 9.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 30.0, "ic_sol"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 33.0, 18.0, "ic_sol"))

            // --- 14. VALLADOLID ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Valladolid", latitud = 41.6521, longitud = -4.7286))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 23.0, descripcion = "Parcialmente nublado",
                codigoIcono = "ic_parcialmente_nublado", tempAlta = 26.0, tempBaja = 12.0,
                humedad = 40.0, vientoVelocidad = 13.0, uvIndice = 6.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 23.0, "ic_parcialmente_nublado"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 26.0, 12.0, "ic_parcialmente_nublado"))

            // --- 15. VIGO ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Vigo", latitud = 42.2406, longitud = -8.7207))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 19.0, descripcion = "Nublado",
                codigoIcono = "ic_nublado", tempAlta = 21.0, tempBaja = 15.0,
                humedad = 75.0, vientoVelocidad = 10.0, uvIndice = 4.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 19.0, "ic_nublado"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 21.0, 15.0, "ic_nublado"))

            // --- 16. GRANADA ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Granada", latitud = 37.1773, longitud = -3.5986))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 28.0, descripcion = "Soleado",
                codigoIcono = "ic_sol", tempAlta = 31.0, tempBaja = 16.0,
                humedad = 28.0, vientoVelocidad = 6.0, uvIndice = 8.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 28.0, "ic_sol"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 31.0, 16.0, "ic_sol"))

            // --- 17. VITORIA-GASTEIZ ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Vitoria-Gasteiz", latitud = 42.8467, longitud = -2.6731))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 20.0, descripcion = "Parcialmente nublado",
                codigoIcono = "ic_parcialmente_nublado", tempAlta = 23.0, tempBaja = 11.0,
                humedad = 60.0, vientoVelocidad = 12.0, uvIndice = 5.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 20.0, "ic_parcialmente_nublado"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 23.0, 11.0, "ic_parcialmente_nublado"))

            // --- 18. GIJÓN ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Gijón", latitud = 43.5322, longitud = -5.6611))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 17.0, descripcion = "Lluvioso",
                codigoIcono = "ic_lluvia", tempAlta = 19.0, tempBaja = 14.0,
                humedad = 82.0, vientoVelocidad = 15.0, uvIndice = 3.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 17.0, "ic_lluvia"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 19.0, 14.0, "ic_lluvia"))

            // --- 19. SANTANDER ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Santander", latitud = 43.4623, longitud = -3.8099))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 18.0, descripcion = "Nublado",
                codigoIcono = "ic_nublado", tempAlta = 20.0, tempBaja = 15.0,
                humedad = 78.0, vientoVelocidad = 12.0, uvIndice = 4.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 18.0, "ic_nublado"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 20.0, 15.0, "ic_nublado"))

            // --- 20. PAMPLONA ---
            cityId = weatherDao.insertarCiudad(CiudadEntidad(nombre = "Pamplona", latitud = 42.8125, longitud = -1.6458))
            weatherDao.insertarTiempoActual(TiempoActualEntidad(
                idCiudadFk = cityId.toInt(), temperatura = 21.0, descripcion = "Soleado",
                codigoIcono = "ic_sol", tempAlta = 24.0, tempBaja = 13.0,
                humedad = 45.0, vientoVelocidad = 11.0, uvIndice = 6.0, precipitacion = 10
            ))
            weatherDao.insertarListaHoras(generarPrediccion24Horas(cityId.toInt(), 21.0, "ic_sol"))
            weatherDao.insertarListaDias(generarPrediccion7Dias(cityId.toInt(), 24.0, 13.0, "ic_sol"))

        }
    }
}
