package es.gbr.aeris.model.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla 'tiempo_actual' en la base de datos.
 * Almacena el clima actual de una ciudad específica.
 *
 * Utiliza ForeignKey para relacionarse con CiudadEntidad.
 * Cuando se elimina una ciudad, su tiempo actual también se elimina.
 */
@Entity(
    tableName = "tiempo_actual",
    foreignKeys = [ForeignKey(
        entity = CiudadEntidad::class,
        parentColumns = ["id_ciudad"],
        childColumns = ["id_ciudad_fk"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["id_ciudad_fk"], unique = true)]
)
data class TiempoActualEntidad(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_tiempo")
    val idTiempo: Int = 0,

    @ColumnInfo(name = "id_ciudad_fk")
    val idCiudadFk: Int,

    @ColumnInfo(name = "temperatura")
    val temperatura: Double,

    @ColumnInfo(name = "descripcion")
    val descripcion: String,

    @ColumnInfo(name = "codigo_icono")
    val codigoIcono: String,

    @ColumnInfo(name = "temp_alta")
    val tempAlta: Double,

    @ColumnInfo(name = "temp_baja")
    val tempBaja: Double,

    @ColumnInfo(name = "humedad")
    val humedad: Double,

    @ColumnInfo(name = "viento_velocidad")
    val vientoVelocidad: Double,

    @ColumnInfo(name = "uv_indice")
    val uvIndice: Double,

    @ColumnInfo(name = "precipitacion")
    val precipitacion: Int = 0
)