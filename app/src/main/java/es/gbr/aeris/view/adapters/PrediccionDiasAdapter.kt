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

// Adaptador para mostrar la predicción del clima por días
class PrediccionDiasAdapter(
    private var datos: List<PrediccionDiariaEntidad> = emptyList(),
    private var usarFahrenheit: Boolean = false
) : RecyclerView.Adapter<PrediccionDiasAdapter.PrediccionDiasViewHolder>() {

    // ViewHolder para cada elemento de la lista
    class PrediccionDiasViewHolder(
        val vinculacion: ItemPrediccionDiasBinding
    ) : RecyclerView.ViewHolder(vinculacion.root) {

        // Muestra los datos de un día con dos líneas de texto
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

    override fun onCreateViewHolder(padre: ViewGroup, tipoVista: Int): PrediccionDiasViewHolder {
        val vinculacion = ItemPrediccionDiasBinding.inflate(
            LayoutInflater.from(padre.context),
            padre,
            false
        )
        return PrediccionDiasViewHolder(vinculacion)
    }

    override fun onBindViewHolder(contenedor: PrediccionDiasViewHolder, posicion: Int) {
        contenedor.vincular(datos[posicion], usarFahrenheit, posicion)
    }


    override fun getItemCount(): Int = datos.size

    // Actualiza los datos cuando cambian desde el ViewModel
    fun actualizarDatos(nuevosDatos: List<PrediccionDiariaEntidad>) {
        datos = nuevosDatos
        notifyDataSetChanged()
    }
}