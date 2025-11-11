package es.gbr.aeris.view.adapters

import es.gbr.aeris.R
import es.gbr.aeris.model.DatosCompartidos
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.gbr.aeris.databinding.ItemPrediccionDiasBinding
import es.gbr.aeris.model.database.entities.PrediccionDiariaEntidad
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adaptador para el RecyclerView de predicción por días.
 * Muestra la temperatura máxima/mínima, día e icono del clima para cada día de la semana.
 *
 * @param datos Lista de predicciones diarias desde la base de datos
 * @param usarFahrenheit Indica si se deben mostrar las temperaturas en Fahrenheit o Celsius
 */
class PrediccionDiasAdapter(
    private var datos: List<PrediccionDiariaEntidad> = emptyList(),
    private var usarFahrenheit: Boolean = false
) : RecyclerView.Adapter<PrediccionDiasAdapter.PrediccionDiasViewHolder>() {

    /**
     * ViewHolder que mantiene las referencias a las vistas de cada item.
     */
    class PrediccionDiasViewHolder(
        val vinculacion: ItemPrediccionDiasBinding
    ) : RecyclerView.ViewHolder(vinculacion.root) {

        /**
         * Vincula los datos de una predicción diaria con las vistas del item.
         * Muestra dos líneas: día de la BD (traducido) y día calculado siguiente.
         */
        fun vincular(elemento: PrediccionDiariaEntidad, usarFahrenheit: Boolean, posicion: Int) {
            // Mostrar día traducido según idioma (Lunes/Monday)
            vinculacion.itemDailyDayPrimary.text = DatosCompartidos.traducirDia(
                vinculacion.root.context, elemento.nombreDia
            )
            
            // Calcular y mostrar el día siguiente al actual (posicion + 2)
            val calendario = Calendar.getInstance()
            calendario.add(Calendar.DAY_OF_YEAR, posicion + 2)
            val formatoDia = SimpleDateFormat("EEEE", Locale.getDefault())
            val diaSiguiente = formatoDia.format(calendario.time).replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            }
            vinculacion.itemDailyDaySecondary.text = diaSiguiente
            
            // Convertir temperaturas según preferencia del usuario
            val tempAlta = if (usarFahrenheit) (elemento.tempAlta * 9/5) + 32 else elemento.tempAlta
            val tempBaja = if (usarFahrenheit) (elemento.tempBaja * 9/5) + 32 else elemento.tempBaja
            vinculacion.itemDailyTemp.text = "${tempBaja.toInt()}° / ${tempAlta.toInt()}°"

            // Cargar icono del clima según el código
            val idRecurso = when(elemento.codigoIcono) {
                "ic_sol" -> R.drawable.ic_sol
                "ic_lluvia" -> R.drawable.ic_lluvia
                "ic_nieve" -> R.drawable.ic_nieve
                "ic_nublado" -> R.drawable.ic_nublado
                "ic_parcialmente_nublado" -> R.drawable.ic_parcialmente_nublado
                "ic_tormenta" -> R.drawable.ic_tormenta
                else -> {
                    val contexto = vinculacion.root.context
                    val id = contexto.resources.getIdentifier(
                        elemento.codigoIcono,
                        "drawable",
                        contexto.packageName
                    )
                    if (id != 0) id else R.drawable.ic_sol
                }
            }
            vinculacion.itemDailyIcon.setImageResource(idRecurso)
        }
    }

    /** Crea un nuevo ViewHolder inflando el layout del item */
    override fun onCreateViewHolder(padre: ViewGroup, tipoVista: Int): PrediccionDiasViewHolder {
        val vinculacion = ItemPrediccionDiasBinding.inflate(
            LayoutInflater.from(padre.context),
            padre,
            false
        )
        return PrediccionDiasViewHolder(vinculacion)
    }

    /** Vincula los datos de una posición específica con el ViewHolder */
    override fun onBindViewHolder(contenedor: PrediccionDiasViewHolder, posicion: Int) {
        contenedor.vincular(datos[posicion], usarFahrenheit, posicion)
    }

    /** Retorna el número total de items en la lista */
    override fun getItemCount(): Int = datos.size

    /**
     * Actualiza la lista de datos y notifica al RecyclerView para que se redibuje.
     * Llamado cuando cambian los datos desde el ViewModel.
     */
    fun actualizarDatos(nuevosDatos: List<PrediccionDiariaEntidad>) {
        datos = nuevosDatos
        notifyDataSetChanged()
    }
}