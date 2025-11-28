package es.gbr.aeris.model.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla 'prediccion_hora' en la base de datos.
 * Almacena la predicción del clima por horas para cada ciudad.
 */
@Entity(
    tableName = "prediccion_hora",
    foreignKeys = [ForeignKey(
        entity = CiudadEntidad::class,
        parentColumns = ["id_ciudad"],
        childColumns = ["fk_id_ciudad"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["fk_id_ciudad"])]
)
data class PrediccionHorasEntidad(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_hora")
    val idHora: Int = 0,

    @ColumnInfo(name = "fk_id_ciudad")
    val idCiudadFk: Int,

    @ColumnInfo(name = "hora")
    val hora: String,

    @ColumnInfo(name = "temperatura")
    val temperatura: Double,

    @ColumnInfo(name = "codigo_icono")
    val codigoIcono: String
)
