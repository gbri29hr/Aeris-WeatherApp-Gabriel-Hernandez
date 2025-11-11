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

class LocalizacionesActivity : AppCompatActivity() {

    private lateinit var vinculacion: ActivityLocalizacionesBinding
    private val modeloVista: LocalizacionesViewModel by viewModels()
    private lateinit var adaptadorLocalizacion: LocalizacionAdapter
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
    
    override fun onResume() {
        super.onResume()
        adaptadorLocalizacion.actualizarCiudadPrincipal(DatosCompartidos.idCiudadSeleccionada)
    }

    private fun configurarBotonAnadir() {
        vinculacion.btnAddLocation.setOnClickListener {
            mostrarDialogoAnadirCiudad()
        }
    }
    
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
    
    private fun ocultarCiudad(idCiudad: Int) {
        DatosCompartidos.ocultarCiudad(idCiudad)
        modeloVista.actualizarCiudadesOcultas(obtenerCiudadesOcultas())
    }
    
    private fun mostrarCiudad(idCiudad: Int) {
        DatosCompartidos.mostrarCiudad(idCiudad)
        modeloVista.actualizarCiudadesOcultas(obtenerCiudadesOcultas())
    }

    private fun configurarBotonUbicacionActual() {
        vinculacion.cardUsarUbicacion.setOnClickListener {
            Toast.makeText(this, "Disponible próximamente", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun configurarBotonEliminar() {
        vinculacion.btnDeleteMode.setOnClickListener {
            adaptadorLocalizacion.activarModoEliminacion()
            Toast.makeText(this, R.string.ubicaciones_seleccionar_para_eliminar, Toast.LENGTH_SHORT).show()
            
            // Cambiar el botón a "Confirmar"
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
    
    private fun configurarBotonCancelar() {
        adaptadorLocalizacion.desactivarModoEliminacion()
        vinculacion.btnDeleteMode.setImageResource(R.drawable.ic_eliminar)
        configurarBotonEliminar()
    }

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

    private fun configurarBusqueda() {
        // Usar addTextChangedListener con TextWatcher como en PDF 2.8
        vinculacion.locationsSearchBarEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se necesita implementación
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No se necesita implementación
            }

            override fun afterTextChanged(s: Editable?) {
                // Llamar a la búsqueda después de que el texto ha cambiado
                modeloVista.buscarCiudad(s.toString())
            }
        })
    }

    private fun observarDatos() {
        modeloVista.actualizarCiudadesOcultas(DatosCompartidos.obtenerCiudadesOcultas())

        // Observar cambios en la lista completa de ciudades para actualizar el filtrado
        modeloVista.todasLasCiudadesDB.observe(this) { todasLasCiudades ->
            if (todasLasCiudades != null) {
                modeloVista.actualizarListaFiltrada(todasLasCiudades)
            }
        }

        // Observar cambios en las ciudades filtradas para actualizar el adaptador
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
    
    private fun mostrarDialogoEliminarMultiple() {
        // Obtener ID de la ciudad principal
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