package es.gbr.aeris.view.adapters

import android.util.Log
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
import kotlin.collections.get

class LocalizacionAdapter(
    private val alHacerClicEnElemento: (CiudadEntidad) -> Unit,
    private val alHacerClicEnEliminar: (CiudadEntidad) -> Unit,
    private var idCiudadPrincipal: Int = 1,
    private val usarFahrenheit: Boolean = false
) : RecyclerView.Adapter<LocalizacionAdapter.LocalizacionViewHolder>() {

    private var datos: List<CiudadConTiempoActual> = emptyList()
    private var modoEliminacion = false
    private val ciudadesSeleccionadas = mutableSetOf<Int>()

    /**
     * El ViewHolder. Almacena las vistas de cada item.
     */
    class LocalizacionViewHolder(
        val vinculacion: ItemLocalizacionBinding
    ) : RecyclerView.ViewHolder(vinculacion.root) {

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

            if (esCiudadPrincipal) {
                vinculacion.itemWeatherIcon.setImageResource(R.drawable.ic_home)
                vinculacion.itemMaxMinContainer.visibility = View.VISIBLE
            } else {
                vinculacion.itemMaxMinContainer.visibility = View.GONE
            }

            if (tiempo != null) {
                val temperatura = if (usarFahrenheit) celsiusAFahrenheit(tiempo.temperatura) else tiempo.temperatura
                val tempMin = if (usarFahrenheit) celsiusAFahrenheit(tiempo.tempBaja) else tiempo.tempBaja
                val tempMax = if (usarFahrenheit) celsiusAFahrenheit(tiempo.tempAlta) else tiempo.tempAlta

                vinculacion.itemTemperature.text = "${temperatura.toInt()}°"
                vinculacion.itemWeatherDescription.text = DatosCompartidos.traducirDescripcion(
                    vinculacion.root.context, tiempo.descripcion
                )

                if (esCiudadPrincipal) {
                    vinculacion.itemMinTemp.text = "MIN: ${tempMin.toInt()}°"
                    vinculacion.itemMaxTemp.text = "MAX: ${tempMax.toInt()}°"
                } else {
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

            // Configurar modo de eliminación
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
        
        private fun celsiusAFahrenheit(celsius: Double): Double = (celsius * 9/5) + 32

    }

    override fun onCreateViewHolder(padre: ViewGroup, tipoVista: Int): LocalizacionViewHolder {
        val vinculacion = ItemLocalizacionBinding.inflate(
            LayoutInflater.from(padre.context),
            padre,
            false
        )
        return LocalizacionViewHolder(vinculacion)
    }

    override fun onBindViewHolder(contenedor: LocalizacionAdapter.LocalizacionViewHolder, posicion: Int) {
        val esCiudadPrincipal = datos[posicion].ciudad.idCiudad == idCiudadPrincipal
        contenedor.vincular(datos[posicion], esCiudadPrincipal, alHacerClicEnElemento, alHacerClicEnEliminar, modoEliminacion, ciudadesSeleccionadas, usarFahrenheit)
    }

    override fun getItemCount(): Int = datos.size

    fun actualizarDatos(nuevosDatos: List<CiudadConTiempoActual>) {
        // Ordenar para que la ciudad principal aparezca primero
        datos = nuevosDatos.sortedByDescending { it.ciudad.idCiudad == idCiudadPrincipal }
        notifyDataSetChanged()
    }
    
    fun actualizarCiudadPrincipal(idCiudad: Int) {
        idCiudadPrincipal = idCiudad
        notifyDataSetChanged()
    }
    
    fun activarModoEliminacion() {
        modoEliminacion = true
        ciudadesSeleccionadas.clear()
        notifyDataSetChanged()
    }
    
    fun desactivarModoEliminacion() {
        modoEliminacion = false
        ciudadesSeleccionadas.clear()
        notifyDataSetChanged()
    }
    
    fun obtenerCiudadesSeleccionadas(): List<Int> {
        return ciudadesSeleccionadas.toList()
    }
    
    fun haySeleccionadas(): Boolean {
        return ciudadesSeleccionadas.isNotEmpty()
    }
}