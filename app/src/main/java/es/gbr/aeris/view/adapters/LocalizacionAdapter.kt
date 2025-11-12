package es.gbr.aeris.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.gbr.aeris.R
import es.gbr.aeris.databinding.ItemLocalizacionBinding
import es.gbr.aeris.model.DatosCompartidos
import es.gbr.aeris.model.database.entities.CiudadEntidad
import es.gbr.aeris.model.database.relations.CiudadConTiempoActual
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adaptador para el RecyclerView de ubicaciones/ciudades.
 * Muestra cada ciudad con su temperatura actual, descripción del clima y opción de eliminar.
 * 
 * Implementa un modo de selección múltiple para eliminar varias ciudades a la vez.
 * La ciudad principal se marca visualmente con un icono de casa.
 *
 */
class LocalizacionAdapter(
    private val alHacerClicEnElemento: (CiudadEntidad) -> Unit,
    private val alHacerClicEnEliminar: (CiudadEntidad) -> Unit,
    private var idCiudadPrincipal: Int = 1,
    private val usarFahrenheit: Boolean = false
) : RecyclerView.Adapter<LocalizacionAdapter.LocalizacionViewHolder>() {

    private var datos: List<CiudadConTiempoActual> = emptyList()
    

    private var modoEliminacion = false
    
    /** Conjunto de IDs de ciudades seleccionadas para eliminar */
    private val ciudadesSeleccionadas = mutableSetOf<Int>()

    /**
     * ViewHolder que mantiene las referencias a las vistas de cada item de ciudad.
     */
    class LocalizacionViewHolder(
        val vinculacion: ItemLocalizacionBinding
    ) : RecyclerView.ViewHolder(vinculacion.root) {

        /**
         * Vincula los datos de una ciudad con las vistas del item.
         * Muestra diferente información según si es la ciudad principal o no.
         * En modo eliminación, muestra checkboxes para selección múltiple.
         */
        fun vincular(
            elemento: CiudadConTiempoActual,
            esCiudadPrincipal: Boolean,
            alHacerClicEnElemento: (CiudadEntidad) -> Unit,
            alHacerClicEnEliminar: (CiudadEntidad) -> Unit,
            modoEliminacion: Boolean,
            ciudadesSeleccionadas: MutableSet<Int>,
            usarFahrenheit: Boolean
        ) {
            val ciudad = elemento.ciudad
            val tiempo = elemento.tiempoActual

            vinculacion.itemCityName.text = ciudad.nombre ?: "Sin nombre"
            vinculacion.itemDay.text = obtenerDiaDeLaSemana()

            // La ciudad principal se marca con icono de casa y muestra temperaturas máx/mín
            if (esCiudadPrincipal) {
                vinculacion.itemWeatherIcon.setImageResource(R.drawable.ic_home)
                vinculacion.itemMaxMinContainer.visibility = View.VISIBLE
            } else {
                vinculacion.itemMaxMinContainer.visibility = View.GONE
            }

            // Mostrar datos del tiempo si están disponibles
            if (tiempo != null) {
                val temperatura = if (usarFahrenheit) celsiusAFahrenheit(tiempo.temperatura) else tiempo.temperatura
                val tempMin = if (usarFahrenheit) celsiusAFahrenheit(tiempo.tempBaja) else tiempo.tempBaja
                val tempMax = if (usarFahrenheit) celsiusAFahrenheit(tiempo.tempAlta) else tiempo.tempAlta

                vinculacion.itemTemperature.text = "${temperatura.toInt()}°"
                // Traducir descripción del clima según idioma del sistema
                vinculacion.itemWeatherDescription.text = DatosCompartidos.traducirDescripcion(
                    vinculacion.root.context, tiempo.descripcion
                )

                if (esCiudadPrincipal) {
                    vinculacion.itemMinTemp.text = "MIN: ${tempMin.toInt()}°"
                    vinculacion.itemMaxTemp.text = "MAX: ${tempMax.toInt()}°"
                } else {
                    // Cargar icono del clima para ciudades no principales
                    val idRecurso = when(tiempo.codigoIcono) {
                        "ic_sol" -> R.drawable.ic_sol
                        "ic_lluvia" -> R.drawable.ic_lluvia
                        "ic_nieve" -> R.drawable.ic_nieve
                        "ic_nublado" -> R.drawable.ic_nublado
                        "ic_parcialmente_nublado" -> R.drawable.ic_parcialmente_nublado
                        "ic_tormenta" -> R.drawable.ic_tormenta
                        else -> R.drawable.ic_sol
                    }
                    vinculacion.itemWeatherIcon.setImageResource(idRecurso)
                }
            } else {
                // No hay datos de tiempo para esta ciudad
                vinculacion.itemTemperature.text = "--°"
                vinculacion.itemWeatherDescription.text = "Sin datos"
                if (esCiudadPrincipal) {
                    vinculacion.itemMinTemp.text = "MIN: --°"
                    vinculacion.itemMaxTemp.text = "MAX: --°"
                }
            }

            // Configurar visualización según modo eliminación
            // En modo eliminación se muestran checkboxes para selección múltiple
            if (modoEliminacion) {
                vinculacion.itemCheckbox.visibility = View.VISIBLE
                vinculacion.itemCheckbox.isChecked = ciudadesSeleccionadas.contains(ciudad.idCiudad)
                vinculacion.itemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        ciudadesSeleccionadas.add(ciudad.idCiudad)
                    } else {
                        ciudadesSeleccionadas.remove(ciudad.idCiudad)
                    }
                }
                vinculacion.root.setOnClickListener {
                    vinculacion.itemCheckbox.isChecked = !vinculacion.itemCheckbox.isChecked
                }
            } else {
                vinculacion.itemCheckbox.visibility = View.GONE
                vinculacion.root.setOnClickListener {
                    alHacerClicEnElemento(ciudad)
                }
            }
        }

        /** Obtiene el nombre del día actual según el idioma del sistema */
        private fun obtenerDiaDeLaSemana(): String {
            return try {
                val formateadorFecha = SimpleDateFormat("EEEE", Locale.getDefault())
                formateadorFecha.format(Date()).replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                }
            } catch (e: Exception) {
                "Today"
            }
        }
        
        /** Convierte temperatura de Celsius a Fahrenheit */
        private fun celsiusAFahrenheit(celsius: Double): Double = (celsius * 9/5) + 32

    }

    /** Crea un nuevo ViewHolder inflando el layout del item */
    override fun onCreateViewHolder(padre: ViewGroup, tipoVista: Int): LocalizacionViewHolder {
        val vinculacion = ItemLocalizacionBinding.inflate(
            LayoutInflater.from(padre.context),
            padre,
            false
        )
        return LocalizacionViewHolder(vinculacion)
    }

    /** Vincula los datos de una posición específica con el ViewHolder */
    override fun onBindViewHolder(contenedor: LocalizacionAdapter.LocalizacionViewHolder, posicion: Int) {
        val esCiudadPrincipal = datos[posicion].ciudad.idCiudad == idCiudadPrincipal
        contenedor.vincular(datos[posicion], esCiudadPrincipal, alHacerClicEnElemento, alHacerClicEnEliminar, modoEliminacion, ciudadesSeleccionadas, usarFahrenheit)
    }

    /** Retorna el número total de items en la lista */
    override fun getItemCount(): Int = datos.size

    /**
     * Actualiza la lista de ciudades y la reordena para mostrar la principal primero.
     */
    fun actualizarDatos(nuevosDatos: List<CiudadConTiempoActual>) {
        // Ordenar para que la ciudad principal aparezca siempre primero
        datos = nuevosDatos.sortedByDescending { it.ciudad.idCiudad == idCiudadPrincipal }
        notifyDataSetChanged()
    }
    
    /** Actualiza cuál es la ciudad marcada como principal */
    fun actualizarCiudadPrincipal(idCiudad: Int) {
        idCiudadPrincipal = idCiudad
        notifyDataSetChanged()
    }
    
    /** Activa el modo de selección múltiple para eliminar ciudades */
    fun activarModoEliminacion() {
        modoEliminacion = true
        ciudadesSeleccionadas.clear()
        notifyDataSetChanged()
    }
    
    /** Desactiva el modo de eliminación y limpia las selecciones */
    fun desactivarModoEliminacion() {
        modoEliminacion = false
        ciudadesSeleccionadas.clear()
        notifyDataSetChanged()
    }
    
    /** Retorna la lista de IDs de ciudades seleccionadas para eliminar */
    fun obtenerCiudadesSeleccionadas(): List<Int> {
        return ciudadesSeleccionadas.toList()
    }
    
    /** Verifica si hay ciudades seleccionadas en modo eliminación */
    fun haySeleccionadas(): Boolean {
        return ciudadesSeleccionadas.isNotEmpty()
    }
}