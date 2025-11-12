package es.gbr.aeris.view.activities

import es.gbr.aeris.R
import es.gbr.aeris.databinding.ActivityLocalizacionesBinding
import es.gbr.aeris.model.database.entities.CiudadEntidad
import es.gbr.aeris.view.adapters.LocalizacionAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import es.gbr.aeris.model.DatosCompartidos
import es.gbr.aeris.viewmodel.LocalizacionesViewModel

/**
 * Activity que gestiona la lista de ubicaciones/ciudades guardadas.
 * Permite buscar, añadir y eliminar ciudades de la lista visible.
 * 
 * Características:
 * - Búsqueda en tiempo real de ciudades
 * - Modo de eliminación múltiple
 * - Marcar ciudad como principal (ubicación actual)
 * - Las ciudades se ocultan pero no se borran de la BD
 */
class LocalizacionesActivity : AppCompatActivity() {

    private lateinit var vinculacion: ActivityLocalizacionesBinding
    private val modeloVista: LocalizacionesViewModel by viewModels()
    private lateinit var adaptadorLocalizacion: LocalizacionAdapter
    
    // Preferencias recibidas desde otras activities
    private var usarFahrenheit: Boolean = false
    private var usarMph: Boolean = false
    private var temaOscuro: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vinculacion = ActivityLocalizacionesBinding.inflate(layoutInflater)
        setContentView(vinculacion.root)

        intent.extras?.let {
            usarFahrenheit = it.getBoolean("usarFahrenheit", false)
            usarMph = it.getBoolean("usarMph", false)
            temaOscuro = it.getBoolean("temaOscuro", false)
        }

        configurarNavegacionInferior()
        configurarRecyclerView()
        configurarBusqueda()
        configurarBotonAnadir()
        configurarBotonEliminar()
        configurarBotonUbicacionActual()
        observarDatos()
    }
    
    // Actualiza la ciudad principal cuando volvemos a esta pantalla
    override fun onResume() {
        super.onResume()
        adaptadorLocalizacion.actualizarCiudadPrincipal(DatosCompartidos.idCiudadSeleccionada)
    }

    // Configurar el botón de añadir ciudad
    private fun configurarBotonAnadir() {
        vinculacion.btnAddLocation.setOnClickListener {
            mostrarDialogoAnadirCiudad()
        }
    }
    
    // Muestra un diálogo con todas las ciudades disponibles
    private fun mostrarDialogoAnadirCiudad() {
        modeloVista.todasLasCiudadesDB.value?.let { todasLasCiudades ->
            if (todasLasCiudades.isEmpty()) {
                Toast.makeText(this, R.string.ubicaciones_cargando, Toast.LENGTH_SHORT).show()
                return
            }

            val ciudadesOcultas = obtenerCiudadesOcultas()
            
            // Lista de nombres de ciudades con indicador si ya está visible
            val nombresCiudades = todasLasCiudades.map { 
                val estaOculta = ciudadesOcultas.contains(it.ciudad.idCiudad)
                if (estaOculta) {
                    "${it.ciudad.nombre}"
                } else {
                    "${it.ciudad.nombre} ✓"
                }
            }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle(R.string.ubicaciones_seleccionar_titulo)
                .setItems(nombresCiudades) { _, posicion ->
                    val ciudadSeleccionada = todasLasCiudades[posicion].ciudad
                    val estaOculta = ciudadesOcultas.contains(ciudadSeleccionada.idCiudad)
                    
                    if (estaOculta) {
                        mostrarDialogoConfirmarAnadir(ciudadSeleccionada)
                    } else {
                        Toast.makeText(this, "${ciudadSeleccionada.nombre} ${getString(R.string.ubicaciones_ya_visible)}", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(R.string.btn_cancelar, null)
                .show()
        } ?: run {
            Toast.makeText(this, R.string.ubicaciones_cargando, Toast.LENGTH_SHORT).show()
        }
    }
    
    // Confirma antes de añadir una ciudad
    private fun mostrarDialogoConfirmarAnadir(ciudad: CiudadEntidad) {
        AlertDialog.Builder(this)
            .setTitle(R.string.ubicaciones_anadir_titulo)
            .setMessage(getString(R.string.ubicaciones_anadir_mensaje, ciudad.nombre))
            .setPositiveButton(R.string.ubicaciones_anadir_btn) { _, _ ->
                mostrarCiudad(ciudad.idCiudad)
                Toast.makeText(this, getString(R.string.ubicaciones_ciudad_anadida, ciudad.nombre), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }
    
    private fun obtenerCiudadesOcultas(): Set<Int> {
        return DatosCompartidos.obtenerCiudadesOcultas()
    }
    
    // Oculta una ciudad de la lista
    private fun ocultarCiudad(idCiudad: Int) {
        DatosCompartidos.ocultarCiudad(idCiudad)
        modeloVista.actualizarCiudadesOcultas(obtenerCiudadesOcultas())
    }
    
    // Hace visible una ciudad oculta
    private fun mostrarCiudad(idCiudad: Int) {
        DatosCompartidos.mostrarCiudad(idCiudad)
        modeloVista.actualizarCiudadesOcultas(obtenerCiudadesOcultas())
    }

    // Configurar botón de ubicación actual
    private fun configurarBotonUbicacionActual() {
        vinculacion.cardUsarUbicacion.setOnClickListener {
            Toast.makeText(this, "Disponible próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    // Configurar botón de eliminar múltiples ciudades
    private fun configurarBotonEliminar() {
        vinculacion.btnDeleteMode.setOnClickListener {
            adaptadorLocalizacion.activarModoEliminacion()
            Toast.makeText(this, R.string.ubicaciones_seleccionar_para_eliminar, Toast.LENGTH_SHORT).show()
            
            // Cambiar el botón a modo confirmación
            vinculacion.btnDeleteMode.setImageResource(android.R.drawable.ic_menu_delete)
            vinculacion.btnDeleteMode.setOnClickListener {
                if (adaptadorLocalizacion.haySeleccionadas()) {
                    mostrarDialogoEliminarMultiple()
                } else {
                    Toast.makeText(this, R.string.ubicaciones_no_seleccionadas, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    // Cancela el modo de eliminación
    private fun configurarBotonCancelar() {
        adaptadorLocalizacion.desactivarModoEliminacion()
        vinculacion.btnDeleteMode.setImageResource(R.drawable.ic_eliminar)
        configurarBotonEliminar()
    }

    // Configurar la navegación inferior
    private fun configurarNavegacionInferior() {
        vinculacion.bottomNavigation.selectedItemId = R.id.nav_localizaciones

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
                R.id.nav_localizaciones -> true
                R.id.nav_settings -> {
                    val intencion = Intent(this, AjustesActivity::class.java)
                    val bundle = Bundle()
                    bundle.putBoolean("usarFahrenheit", usarFahrenheit)
                    bundle.putBoolean("usarMph", usarMph)
                    bundle.putBoolean("temaOscuro", temaOscuro)
                    intencion.putExtras(bundle)
                    startActivity(intencion)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    // Configurar el RecyclerView de ciudades
    private fun configurarRecyclerView() {
        adaptadorLocalizacion = LocalizacionAdapter(
            alHacerClicEnElemento = { ciudad ->
                seleccionarCiudad(ciudad)
            },
            alHacerClicEnEliminar = { ciudad ->
                mostrarDialogoEliminar(ciudad)
            },
            idCiudadPrincipal = DatosCompartidos.idCiudadSeleccionada,
            usarFahrenheit = usarFahrenheit
        )

        vinculacion.locationsRecycler.layoutManager = LinearLayoutManager(this)
        vinculacion.locationsRecycler.adapter = adaptadorLocalizacion
    }

    // Configurar el buscador de ciudades
    private fun configurarBusqueda() {
        vinculacion.locationsSearchBarEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se necesita implementación
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                modeloVista.buscarCiudad(s.toString())
            }
        })
    }

    // Observar cambios en los datos para actualizar la lista
    private fun observarDatos() {
        modeloVista.actualizarCiudadesOcultas(DatosCompartidos.obtenerCiudadesOcultas())

        // Observar lista completa
        modeloVista.todasLasCiudadesDB.observe(this) { todasLasCiudades ->
            if (todasLasCiudades != null) {
                modeloVista.actualizarListaFiltrada(todasLasCiudades)
            }
        }

        // Observar lista filtrada
        modeloVista.ciudadesFiltradas.observe(this) { listaCiudades ->
            if (listaCiudades != null) {
                adaptadorLocalizacion.actualizarDatos(listaCiudades)
            } else {
                adaptadorLocalizacion.actualizarDatos(emptyList())
            }
        }
    }

    private fun seleccionarCiudad(ciudad: CiudadEntidad) {

    }

    /**
     * Muestra diálogo de confirmación para ocultar una sola ciudad.
     * No permite eliminar la ciudad marcada como principal.
     */
    private fun mostrarDialogoEliminar(ciudad: CiudadEntidad) {
        val idCiudadPrincipal = DatosCompartidos.idCiudadSeleccionada

        if (ciudad.idCiudad == idCiudadPrincipal) {
            Toast.makeText(this, R.string.ubicaciones_no_puede_eliminar_principal, Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(this)
            .setTitle(R.string.ubicaciones_dialogo_eliminar_titulo)
            .setMessage("${getString(R.string.ubicaciones_dialogo_eliminar_mensaje)} ${ciudad.nombre}?")
            .setPositiveButton(R.string.ubicaciones_eliminar) { _, _ ->
                ocultarCiudad(ciudad.idCiudad)
                Toast.makeText(this, getString(R.string.ubicaciones_ciudad_eliminada, ciudad.nombre), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    /**
     * Muestra diálogo de confirmación para ocultar múltiples ciudades seleccionadas.
     * Verifica que la ciudad principal no esté en la selección.
     */
    private fun mostrarDialogoEliminarMultiple() {
        val idCiudadPrincipal = DatosCompartidos.idCiudadSeleccionada

        val idsAEliminar = adaptadorLocalizacion.obtenerCiudadesSeleccionadas()
        
        // Verificar si se intenta eliminar la ciudad principal
        if (idsAEliminar.contains(idCiudadPrincipal)) {
            Toast.makeText(this, R.string.ubicaciones_no_eliminar_principal_deseleccionar, Toast.LENGTH_LONG).show()
            return
        }
        
        val cantidad = idsAEliminar.size
        AlertDialog.Builder(this)
            .setTitle(R.string.ubicaciones_dialogo_eliminar_titulo)
            .setMessage(getString(R.string.ubicaciones_eliminar_multiple_mensaje, cantidad))
            .setPositiveButton(R.string.ubicaciones_eliminar) { _, _ ->
                idsAEliminar.forEach { id ->
                    ocultarCiudad(id)
                }
                Toast.makeText(this, getString(R.string.ubicaciones_multiples_eliminadas, cantidad), Toast.LENGTH_SHORT).show()
                configurarBotonCancelar()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                configurarBotonCancelar()
            }
            .show()
    }
}