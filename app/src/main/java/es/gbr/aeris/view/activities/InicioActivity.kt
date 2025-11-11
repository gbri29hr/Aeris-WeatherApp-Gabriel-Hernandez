package es.gbr.aeris.view.activities

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

        // Configurar botón de entrada
        binding.inicioButtonEntrar.setOnClickListener {
            // Crear Intent para ir a MainActivity
            val intent = Intent(this, MainActivity::class.java)

            // Pasar preferencias por defecto usando Bundle (PDF 2.15_Intent.pdf)
            val bundle = Bundle()
            bundle.putBoolean("usarFahrenheit", false)  // Por defecto Celsius
            bundle.putBoolean("usarMph", false)         // Por defecto km/h
            bundle.putBoolean("temaOscuro", false)      // Por defecto tema claro
            intent.putExtras(bundle)

            startActivity(intent)
            finish() // Cerrar esta Activity para que no vuelva con botón atrás
        }
    }
}
