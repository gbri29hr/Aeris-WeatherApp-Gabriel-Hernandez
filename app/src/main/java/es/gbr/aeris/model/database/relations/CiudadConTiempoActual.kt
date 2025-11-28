package es.gbr.aeris.model.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import es.gbr.aeris.model.database.entities.CiudadEntidad
import es.gbr.aeris.model.database.entities.TiempoActualEntidad

/**
 * Clase de relación de Room que combina una ciudad con su tiempo actual.
 */
data class CiudadConTiempoActual(
    @Embedded
    val ciudad: CiudadEntidad,

    @Relation(
        parentColumn = "id_ciudad",
        entityColumn = "fk_id_ciudad"
    )
    val tiempoActual: TiempoActualEntidad?
)