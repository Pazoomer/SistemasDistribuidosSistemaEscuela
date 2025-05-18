package zamora.jorge.aplicacionescuela

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log // Importar Log para depuración
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import zamora.jorge.aplicacionescuela.data.Asignacion

class Actividad : AppCompatActivity() {
    private lateinit var tvNombreTarea: TextView
    private lateinit var tvFechaHoraTarea: TextView
    private lateinit var tvDescripcionTarea: TextView
    private lateinit var tvPdf: TextView
    private lateinit var btnAvalarTarea: Button

    private lateinit var database: DatabaseReference
    private var idAlumno: String? = null
    private var idAsignacion: String? = null // Este se obtiene de asignacion.id
    private var entregaKey: String? = null
    private var archivoUrl: String? = null

    // Constante para el Tag de Log
    private companion object {
        private const val TAG = "ActividadEscuela"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad)

        Log.d(TAG, "onCreate: Iniciando Actividad.")

        tvNombreTarea = findViewById(R.id.tvNombreTarea)
        tvFechaHoraTarea = findViewById(R.id.tvFechaHoraTarea)
        tvDescripcionTarea = findViewById(R.id.tvDescripcionTarea)
        tvPdf = findViewById(R.id.tvPdf)
        btnAvalarTarea = findViewById(R.id.btnAprobarTarea)

        database = FirebaseDatabase.getInstance().reference

        // Obtener datos del intent
        val asignacion = intent.getSerializableExtra("actividad") as? Asignacion
        idAlumno = intent.getStringExtra("alumnoId")

        Log.d(TAG, "Intent - Alumno ID: $idAlumno")
        Log.d(TAG, "Intent - Asignación Título (si existe): ${asignacion?.titulo}")


        if (asignacion != null && idAlumno != null && idAlumno!!.isNotEmpty()) {
            // Es crucial que la Asignacion tenga un ID válido.
            if (asignacion.id != null && asignacion.id!!.isNotEmpty()) {
                this.idAsignacion = asignacion.id // Asignar el id de la asignación
                Log.d(TAG, "ID Asignación procesado: ${this.idAsignacion}")
                mostrarInformacionBasica(asignacion)
                cargarAsignacionEntrega()
            } else {
                Toast.makeText(this, "Error: El ID de la asignación es inválido.", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error: asignacion.id es null o vacío.")
                finish() // Cerrar si no hay ID de asignación
            }
        } else {
            Toast.makeText(this, "Datos insuficientes para mostrar la tarea (falta asignación o ID de alumno).", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Error: asignacion es $asignacion, idAlumno es $idAlumno")
            finish() // Cerrar si faltan datos esenciales
        }

        btnAvalarTarea.setOnClickListener {
            if (entregaKey != null) {
                avalarTarea()
            } else {
                Toast.makeText(this, "No se ha cargado información de entrega para avalar.", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Intento de avalar sin entregaKey.")
            }
        }
    }

    private fun mostrarInformacionBasica(asignacion: Asignacion) {
        tvNombreTarea.text = asignacion.titulo ?: "Título no disponible"
        tvFechaHoraTarea.text = asignacion.fecha_limite ?: "Fecha límite no disponible"
        tvDescripcionTarea.text = asignacion.descripcion ?: "Descripción no disponible"
        Log.d(TAG, "mostrarInformacionBasica: Mostrando datos de ${asignacion.titulo}")
    }

    private fun cargarAsignacionEntrega() {
        if (idAsignacion == null || idAlumno == null) {
            Log.e(TAG, "cargarAsignacionEntrega: idAsignacion o idAlumno es null. No se puede continuar.")
            Toast.makeText(this, "Error interno: IDs no disponibles para cargar entrega.", Toast.LENGTH_SHORT).show()
            return
        }

        // Usar "asignaciones_entregas" (plural) para que coincida con la estructura de la BD (según imagen)
        val ref = database.child("asignaciones_entregas")
        Log.d(TAG, "Consultando Firebase en: asignaciones_entregas, por id_asignacion: $idAsignacion")

        ref.orderByChild("id_asignacion").equalTo(idAsignacion)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "onDataChange: Snapshot recibido. Existe: ${snapshot.exists()}. Hijos: ${snapshot.childrenCount}")
                    var entregaEncontrada = false
                    if (!snapshot.exists()) {
                        Log.w(TAG, "No existen entregas para la asignación ID: $idAsignacion")
                    }
                    for (entregaSnapshot in snapshot.children) {
                        val idAlumnoEntrega = entregaSnapshot.child("id_alumno").getValue(String::class.java)
                        Log.d(TAG, "Procesando entrega para alumno: $idAlumnoEntrega (buscando: $idAlumno)")
                        if (idAlumnoEntrega == idAlumno) {
                            entregaKey = entregaSnapshot.key
                            archivoUrl = entregaSnapshot.child("archivo_adjunto").getValue(String::class.java)
                            Log.i(TAG, "Entrega encontrada para alumno $idAlumno. Key: $entregaKey, URL: $archivoUrl")

                            if (archivoUrl != null && archivoUrl!!.isNotEmpty()) {
                                // Texto que se mostrará como enlace
                                val textoDelEnlace = "Ver Archivo Adjunto"
                                val spannableString = SpannableString(textoDelEnlace)
                                val clickableSpan = object : ClickableSpan() {
                                    override fun onClick(widget: View) {
                                        // La URL real se usa aquí, no el textoDelEnlace
                                        archivoUrl?.let { urlReal ->
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlReal))
                                                startActivity(intent)
                                            } catch (e: ActivityNotFoundException) {
                                                Toast.makeText(this@Actividad, "No se puede abrir el enlace: URL inválida o ninguna aplicación puede manejarlo.", Toast.LENGTH_LONG).show()
                                                Log.e(TAG, "ActivityNotFoundException al abrir URL: $urlReal", e)
                                            } catch (e: Exception) {
                                                Toast.makeText(this@Actividad, "Error al intentar abrir el enlace.", Toast.LENGTH_LONG).show()
                                                Log.e(TAG, "Excepción general al abrir URL: $urlReal", e)
                                            }
                                        }
                                    }
                                }
                                // Aplicar el ClickableSpan a todo el textoDelEnlace
                                spannableString.setSpan(clickableSpan, 0, textoDelEnlace.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                                tvPdf.text = spannableString
                                tvPdf.movementMethod = LinkMovementMethod.getInstance() // Importante para que los enlaces sean clicables
                                tvPdf.isClickable = true // Asegurarse de que el TextView es clicable
                            } else {
                                tvPdf.text = "Sin archivo adjunto"
                                tvPdf.isClickable = false
                                Log.d(TAG, "No hay archivo adjunto para esta entrega.")
                            }
                            entregaEncontrada = true
                            val estadoActual = entregaSnapshot.child("estado").getValue(String::class.java)
                            Log.d(TAG, "Estado actual de la tarea: $estadoActual")
                            btnAvalarTarea.isEnabled = estadoActual != "avalado"
                            if (estadoActual == "avalado") {
                                btnAvalarTarea.text = "Tarea Ya Avalada"
                            } else {
                                btnAvalarTarea.text = "Avalar Tarea"
                            }
                            return // Salir después de encontrar la entrega relevante
                        }
                    }

                    if (!entregaEncontrada) {
                        tvPdf.text = "Sin archivo entregado por el alumno"
                        tvPdf.isClickable = false
                        btnAvalarTarea.isEnabled = false
                        btnAvalarTarea.text = "Avalar Tarea" // Texto por defecto
                        Log.w(TAG, "No se encontró entrega para el alumno $idAlumno en la asignación $idAsignacion")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Actividad, "Error al cargar datos de entrega: ${error.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Error en Firebase al cargar asignación entrega: ${error.message}", error.toException())
                    tvPdf.text = "Error al cargar archivo"
                    tvPdf.isClickable = false
                    btnAvalarTarea.isEnabled = false
                }
            })
    }

    private fun avalarTarea() {
        if (entregaKey == null) {
            Toast.makeText(this, "No se puede avalar: ID de entrega no encontrado.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "avalarTarea: entregaKey es null.")
            return
        }

        // Usar "asignaciones_entregas" (plural) también aquí si es la misma tabla
        val entregaRef = database.child("asignaciones_entregas").child(entregaKey!!)


        entregaRef.child("estado").setValue("avalado")
            .addOnSuccessListener {
                Toast.makeText(this, "Tarea avalada exitosamente.", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "Tarea avalada exitosamente para entregaKey: $entregaKey")
                btnAvalarTarea.isEnabled = false
                btnAvalarTarea.text = "Tarea Avalada"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al avalar la tarea: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error en Firebase al avalar tarea para entregaKey: $entregaKey", e)
            }
    }
}
