package es.gbr.aeris.view.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import es.gbr.aeris.databinding.ActivityInicioBinding

/**
 * Activity de inicio/bienvenida de la aplicación.
 * Es la primera pantalla que ve el usuario al abrir la app.
 *
 * Muestra el logo y un botón para entrar a la aplicación principal.
 */
class InicioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInicioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.inicioButtonEntrar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)

            val bundle = Bundle()
            bundle.putBoolean("usarFahrenheit", false)
            bundle.putBoolean("usarMph", false)
            
            // Detectar el tema del sistema
            val esTemaOscuro = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            bundle.putBoolean("temaOscuro", esTemaOscuro)
            
            intent.putExtras(bundle)

            startActivity(intent)
            finish()
        }
    }
}
