package es.gbr.aeris.view.activities


import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import es.gbr.aeris.R
import es.gbr.aeris.databinding.ActivityAjustesBinding

/**
 * Activity que permite configurar las preferencias de la aplicación.
 * 
 * Opciones disponibles:
 * - Unidad de temperatura: Celsius/Fahrenheit
 * - Unidad de velocidad del viento: km/h / mph
 * - Tema de la aplicación: Claro / Oscuro
 * 
 * Las preferencias se pasan entre activities mediante Bundle.
 */
class AjustesActivity : AppCompatActivity() {

    private lateinit var vinculacion: ActivityAjustesBinding
    private var usarFahrenheit: Boolean = false
    private var usarMph: Boolean = false
    private var temaOscuro: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vinculacion = ActivityAjustesBinding.inflate(layoutInflater)
        setContentView(vinculacion.root)

        intent.extras?.let {
            usarFahrenheit = it.getBoolean("usarFahrenheit", false)
            usarMph = it.getBoolean("usarMph", false)
            temaOscuro = it.getBoolean("temaOscuro", false)
        }

        cargarPreferencias()
        configurarListeners()

        vinculacion.bottomNavigation.selectedItemId = R.id.nav_settings

        vinculacion.bottomNavigation.setOnItemSelectedListener { elemento ->
            when (elemento.itemId) {
                R.id.nav_home -> {
                    val intencion = Intent(this, MainActivity::class.java)
                    val bundle = Bundle()
                    bundle.putBoolean("usarFahrenheit", usarFahrenheit)
                    bundle.putBoolean("usarMph", usarMph)
                    bundle.putBoolean("temaOscuro", temaOscuro)
                    intencion.putExtras(bundle)
                    startActivity(intencion)
                    finish()
                    true
                }
                R.id.nav_localizaciones -> {
                    val intencion = Intent(this, LocalizacionesActivity::class.java)
                    val bundle = Bundle()
                    bundle.putBoolean("usarFahrenheit", usarFahrenheit)
                    bundle.putBoolean("usarMph", usarMph)
                    bundle.putBoolean("temaOscuro", temaOscuro)
                    intencion.putExtras(bundle)
                    startActivity(intencion)
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    true
                }
                else -> false
            }
        }
    }

    /** Carga las preferencias actuales y actualiza los switches y colores */
    private fun cargarPreferencias() {
        vinculacion.switchTemperatura.isChecked = usarFahrenheit
        vinculacion.switchViento.isChecked = usarMph
        vinculacion.switchTema.isChecked = temaOscuro

        actualizarColoresTemperatura(usarFahrenheit)
        actualizarColoresViento(usarMph)
        actualizarColoresTema(temaOscuro)
    }

    /** Configura los listeners para los switches de preferencias */
    private fun configurarListeners() {
        vinculacion.switchTemperatura.setOnCheckedChangeListener { _, estaActivado ->
            usarFahrenheit = estaActivado
            actualizarColoresTemperatura(estaActivado)
        }

        vinculacion.switchViento.setOnCheckedChangeListener { _, estaActivado ->
            usarMph = estaActivado
            actualizarColoresViento(estaActivado)
        }

        vinculacion.switchTema.setOnCheckedChangeListener { _, estaActivado ->
            temaOscuro = estaActivado
            actualizarColoresTema(estaActivado)
            aplicarTema(estaActivado)
        }
    }

    /**
     * Actualiza los colores de las etiquetas según el estado del switch.
     * La opción activa se muestra con el color primario, la inactiva con color gris.
     */
    private fun actualizarColoresLabels(esActivo: Boolean, labelActivo: TextView, labelInactivo: TextView) {
        val colorActivo = ContextCompat.getColor(this, R.color.primario_claro)
        val colorInactivo = ContextCompat.getColor(this, R.color.texto_variante_claro)

        labelActivo.setTextColor(if (esActivo) colorActivo else colorInactivo)
        labelInactivo.setTextColor(if (esActivo) colorInactivo else colorActivo)
    }

    /** Actualiza colores para el selector de temperatura */
    private fun actualizarColoresTemperatura(esFahrenheit: Boolean) {
        actualizarColoresLabels(esFahrenheit, vinculacion.labelFahrenheit, vinculacion.labelCelsius)
    }

    /** Actualiza colores para el selector de velocidad del viento */
    private fun actualizarColoresViento(esMph: Boolean) {
        actualizarColoresLabels(esMph, vinculacion.labelMph, vinculacion.labelKph)
    }

    /** Actualiza colores para el selector de tema */
    private fun actualizarColoresTema(esOscuro: Boolean) {
        actualizarColoresLabels(esOscuro, vinculacion.labelDark, vinculacion.labelLight)
    }

    /**
     * Aplica el tema seleccionado a toda la aplicación.
     * Utiliza AppCompatDelegate para cambiar entre modo claro y oscuro.
     */
    private fun aplicarTema(esOscuro: Boolean) {
        if (esOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}