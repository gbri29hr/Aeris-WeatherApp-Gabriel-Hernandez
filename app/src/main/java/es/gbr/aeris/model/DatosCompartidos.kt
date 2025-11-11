package es.gbr.aeris.model

import android.content.Context
import es.gbr.aeris.R

/**
 * Esta clase gestiona:
 * - La ciudad actualmente seleccionada por el usuario
 * - El conjunto de ciudades ocultas (eliminadas de la vista del usuario)
 * - Traducciones de descripciones del clima según el idioma del sistema
 */
object DatosCompartidos {


    var idCiudadSeleccionada: Int = 1


    private val ciudadesOcultas = mutableSetOf<Int>()


    fun obtenerCiudadesOcultas(): Set<Int> = ciudadesOcultas.toSet()


    fun ocultarCiudad(idCiudad: Int) {
        ciudadesOcultas.add(idCiudad)
    }


    fun mostrarCiudad(idCiudad: Int) {
        ciudadesOcultas.remove(idCiudad)
    }

    /**
     * Traduce la descripción del clima al idioma actual del sistema.
     * Utiliza recursos de strings localizados.
     */
    fun traducirDescripcion(context: Context, descripcion: String): String {
        return when (descripcion.lowercase()) {
            "soleado" -> context.getString(R.string.clima_soleado)
            "parcialmente nublado", "parcialmente_nublado" -> context.getString(R.string.clima_parcialmente_nublado)
            "nublado" -> context.getString(R.string.clima_nublado)
            "lluvioso", "lluvia" -> context.getString(R.string.clima_lluvioso)
            "tormenta" -> context.getString(R.string.clima_tormenta)
            "nieve" -> context.getString(R.string.clima_nieve)
            else -> descripcion
        }
    }

    fun traducirDia(context: Context, dia: String): String {
        return when (dia.lowercase()) {
            "lunes" -> if (context.resources.configuration.locales[0].language == "en") "Monday" else "Lunes"
            "martes" -> if (context.resources.configuration.locales[0].language == "en") "Tuesday" else "Martes"
            "miércoles", "miercoles" -> if (context.resources.configuration.locales[0].language == "en") "Wednesday" else "Miércoles"
            "jueves" -> if (context.resources.configuration.locales[0].language == "en") "Thursday" else "Jueves"
            "viernes" -> if (context.resources.configuration.locales[0].language == "en") "Friday" else "Viernes"
            "sábado", "sabado" -> if (context.resources.configuration.locales[0].language == "en") "Saturday" else "Sábado"
            "domingo" -> if (context.resources.configuration.locales[0].language == "en") "Sunday" else "Domingo"
            else -> dia
        }
    }
}

