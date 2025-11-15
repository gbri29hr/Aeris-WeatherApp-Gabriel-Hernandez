package es.gbr.aeris.model

import android.content.Context
import es.gbr.aeris.R

/**
 * Objeto singleton que gestiona datos compartidos entre todas las Activities.
 *
 * Implementa el patrón Singleton de Kotlin usando la palabra clave 'object',
 * lo que garantiza una única instancia accesible globalmente.
 *
 * - Mantener el ID de la ciudad actualmente seleccionada
 * - Gestionar el conjunto de ciudades ocultas/eliminadas
 * - Proporcionar funciones de traducción de clima según idioma del sistema
 * - Traducir días de la semana según idioma del sistema
 */
object DatosCompartidos {

    // ID de la ciudad actualmente seleccionada (por defecto: 1 = Madrid)
    var idCiudadSeleccionada: Int = 1

    // Conjunto mutable de IDs de ciudades que el usuario ha ocultado/eliminado
    private val ciudadesOcultas = mutableSetOf<Int>()

    /**
     * Obtiene una copia inmutable del conjunto de ciudades ocultas.
     * @return Set de IDs de ciudades ocultas
     */
    fun obtenerCiudadesOcultas(): Set<Int> = ciudadesOcultas.toSet()

    /**
     * Marca una ciudad como oculta/eliminada de la lista del usuario.
     * @param idCiudad ID de la ciudad a ocultar
     */
    fun ocultarCiudad(idCiudad: Int) {
        ciudadesOcultas.add(idCiudad)
    }

    /**
     * Remueve una ciudad del conjunto de ocultas, haciéndola visible nuevamente.
     * @param idCiudad ID de la ciudad a mostrar
     */
    fun mostrarCiudad(idCiudad: Int) {
        ciudadesOcultas.remove(idCiudad)
    }

    /**
     * Traduce la descripción del clima al idioma actual del sistema.
     * Utiliza los recursos de strings.xml según el idioma configurado.
     *
     * @param context Contexto para acceder a recursos de strings
     * @param descripcion Descripción del clima en español (desde base de datos)
     * @return String traducido al idioma del sistema (español o inglés)
     */
    fun traducirDescripcion(context: Context, descripcion: String): String {
        return when (descripcion.lowercase()) {
            "soleado" -> context.getString(R.string.clima_soleado)
            "parcialmente nublado", "parcialmente_nublado" -> context.getString(R.string.clima_parcialmente_nublado)
            "nublado" -> context.getString(R.string.clima_nublado)
            "lluvioso", "lluvia" -> context.getString(R.string.clima_lluvioso)
            "tormenta" -> context.getString(R.string.clima_tormenta)
            "nieve" -> context.getString(R.string.clima_nieve)
            else -> descripcion  // Si no encuentra traducción, devuelve el original
        }
    }

    /**
     * Traduce el día de la semana al idioma actual del sistema.
     * Detecta el idioma configurado y devuelve el día correspondiente.
     *
     * @param context Contexto para acceder a configuración del sistema
     * @param dia Nombre del día en español (desde base de datos)
     * @return String del día traducido al idioma del sistema
     */
    fun traducirDia(context: Context, dia: String): String {
        return when (dia.lowercase()) {
            "lunes" -> if (context.resources.configuration.locales[0].language == "en") "Monday" else "Lunes"
            "martes" -> if (context.resources.configuration.locales[0].language == "en") "Tuesday" else "Martes"
            "miércoles", "miercoles" -> if (context.resources.configuration.locales[0].language == "en") "Wednesday" else "Miércoles"
            "jueves" -> if (context.resources.configuration.locales[0].language == "en") "Thursday" else "Jueves"
            "viernes" -> if (context.resources.configuration.locales[0].language == "en") "Friday" else "Viernes"
            "sábado", "sabado" -> if (context.resources.configuration.locales[0].language == "en") "Saturday" else "Sábado"
            "domingo" -> if (context.resources.configuration.locales[0].language == "en") "Sunday" else "Domingo"
            else -> dia  // Si no encuentra traducción, devuelve el original
        }
    }
}
