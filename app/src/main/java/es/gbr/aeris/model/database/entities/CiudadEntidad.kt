package es.gbr.aeris.model.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla de ciudades en la BD
@Entity(tableName = "ciudades")
data class CiudadEntidad(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_ciudad")
    val idCiudad: Int = 0,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "latitud")
    val latitud: Double,

    @ColumnInfo(name = "longitud")
    val longitud: Double
)