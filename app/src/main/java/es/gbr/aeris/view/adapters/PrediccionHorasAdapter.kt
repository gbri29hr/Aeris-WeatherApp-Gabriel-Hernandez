package es.gbr.aeris.view.adapters

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.gbr.aeris.R
import es.gbr.aeris.model.database.entities.PrediccionHorasEntidad
import es.gbr.aeris.databinding.ItemPrediccionHorasBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adaptador para el RecyclerView de predicción por horas.
 * Muestra la temperatura, hora e icono del clima para cada hora del día.
 *
 * @param datos Lista de predicciones por horas desde la base de datos
 * @param usarFahrenheit Indica si se deben mostrar las temperaturas en Fahrenheit o Celsius
 */
class PrediccionHorasAdapter(
    private var datos: List<PrediccionHorasEntidad> = emptyList(),
    private var usarFahrenheit: Boolean = false
) : RecyclerView.Adapter<PrediccionHorasAdapter.PrediccionHorasViewHolder>() {

    /**
     * ViewHolder que mantiene las referencias a las vistas de cada item.
     */
    class PrediccionHorasViewHolder(
        val vinculacion: ItemPrediccionHorasBinding
    ) : RecyclerView.ViewHolder(vinculacion.root) {

        /**
         * Vincula los datos de una predicción horaria con las vistas del item.
         * Resalta visualmente la hora actual con un borde de color.
         */
        fun vincular(elemento: PrediccionHorasEntidad, usarFahrenheit: Boolean) {
            // Mostrar hora
            vinculacion.itemHourlyTime.text = elemento.hora

            // Convertir temperatura según preferencia del usuario
            val temp = if (usarFahrenheit) (elemento.temperatura * 9/5) + 32 else elemento.temperatura
            vinculacion.itemHourlyTemp.text = "${temp.toInt()}°"
            
            // Resaltar visualmente la hora actual con un borde
            val horaActual = SimpleDateFormat("HH", Locale.getDefault()).format(Date())
            val esHoraActual = elemento.hora == horaActual || elemento.hora == "${horaActual}:00"
            
            if (esHoraActual) {
                // Obtener color primario del tema para el borde
                val typedValue = TypedValue()
                vinculacion.root.context.theme.resolveAttribute(
                    com.google.android.material.R.attr.colorPrimary,
                    typedValue,
                    true
                )
                vinculacion.itemHourlyCard.strokeWidth = 4
                vinculacion.itemHourlyCard.strokeColor = typedValue.data
            } else {
                // Sin borde para horas que no son la actual
                vinculacion.itemHourlyCard.strokeWidth = 0
            }

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
            vinculacion.itemHourlyIcon.setImageResource(idRecurso)
        }
    }

    /** Crea un nuevo ViewHolder inflando el layout del item */
    override fun onCreateViewHolder(padre: ViewGroup, tipoVista: Int): PrediccionHorasViewHolder {
        val vinculacion = ItemPrediccionHorasBinding.inflate(
            LayoutInflater.from(padre.context),
            padre,
            false
        )
        return PrediccionHorasViewHolder(vinculacion)
    }

    /** Vincula los datos de una posición específica con el ViewHolder */
    override fun onBindViewHolder(contenedor: PrediccionHorasViewHolder, posicion: Int) {
        contenedor.vincular(datos[posicion], usarFahrenheit)
    }

    /** Retorna el número total de items en la lista */
    override fun getItemCount(): Int = datos.size

    /**
     * Actualiza la lista de datos y notifica al RecyclerView para que se redibuje.
     * Llamado cuando cambian los datos desde el ViewModel.
     */
    fun actualizarDatos(nuevosDatos: List<PrediccionHorasEntidad>) {
        datos = nuevosDatos
        notifyDataSetChanged()
    }
}