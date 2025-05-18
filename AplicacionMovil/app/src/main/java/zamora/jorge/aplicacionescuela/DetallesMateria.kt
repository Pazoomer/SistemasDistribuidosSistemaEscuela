package zamora.jorge.aplicacionescuela

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import zamora.jorge.aplicacionescuela.data.Asignacion // Asegúrate que esta clase es Serializable y tiene un campo 'id'
import zamora.jorge.aplicacionescuela.data.Materia

class DetallesMateria : AppCompatActivity() {

    private lateinit var tvNombreMateria: TextView
    private lateinit var lvActividades: ListView
    private lateinit var database: DatabaseReference
    private lateinit var asignacionesRef: DatabaseReference
    private var alumnoId: String? = null

    // Constante para el Tag de Log
    private companion object {
        private const val TAG = "DetallesMateria"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_materia)

        Log.d(TAG, "onCreate: Iniciando DetallesMateria.")

        tvNombreMateria = findViewById(R.id.tvNombreMateria)
        lvActividades = findViewById(R.id.lvActividades)

        // Obtener la materia pasada por el intent
        val materia = intent.getSerializableExtra("materia") as? Materia
        alumnoId = intent.getStringExtra("alumnoId")

        Log.d(TAG, "Intent - Alumno ID: $alumnoId")
        Log.d(TAG, "Intent - Materia (si existe): ${materia?.nombre}, ID Materia: ${materia?.id}")


        if (materia == null || materia.id == null || materia.id!!.isEmpty()) {
            Toast.makeText(this, "Error: Datos de materia incompletos o inválidos.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Error: Objeto Materia es null o su ID es null/vacío.")
            finish()
            return
        }

        tvNombreMateria.text = materia.nombre

        // Inicializar Firebase
        database = FirebaseDatabase.getInstance().reference
        // Apuntar directamente al nodo 'asignaciones'
        asignacionesRef = database.child("asignaciones")

        // Obtener asignaciones de esta materia usando el ID de la materia
        cargarAsignacionesDeMateria(materia.id!!) // materia.id ya fue validado
    }

    private fun cargarAsignacionesDeMateria(idMateria: String) {
        Log.d(TAG, "cargarAsignacionesDeMateria: Buscando asignaciones para idMateria: $idMateria")
        asignacionesRef.orderByChild("id_materia").equalTo(idMateria)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaAsignaciones = mutableListOf<Asignacion>()
                    Log.d(TAG, "onDataChange: Snapshot recibido para asignaciones. Existe: ${snapshot.exists()}. Hijos: ${snapshot.childrenCount}")

                    if (!snapshot.exists()) {
                        Log.w(TAG, "No se encontraron asignaciones para idMateria: $idMateria")
                        Toast.makeText(this@DetallesMateria, "No hay tareas para esta materia.", Toast.LENGTH_SHORT).show()
                        // Limpiar el adaptador si la lista está vacía y ya había algo
                        lvActividades.adapter = null
                        return
                    }

                    for (asignacionSnapshot in snapshot.children) {
                        val asignacion = asignacionSnapshot.getValue(Asignacion::class.java)
                        if (asignacion != null) {
                            // ¡IMPORTANTE! Asignar la clave del snapshot como el ID de la asignación.
                            asignacion.id = asignacionSnapshot.key
                            if (asignacion.id != null && asignacion.id!!.isNotEmpty()) {
                                listaAsignaciones.add(asignacion)
                                Log.d(TAG, "Asignación agregada: ${asignacion.titulo}, ID: ${asignacion.id}")
                            } else {
                                Log.w(TAG, "Asignación omitida porque su key (ID) es null o vacía: ${asignacion.titulo}")
                            }
                        } else {
                            Log.w(TAG, "Error al convertir snapshot a objeto Asignacion para key: ${asignacionSnapshot.key}")
                        }
                    }

                    if (listaAsignaciones.isNotEmpty()) {
                        val adapter = AsignacionAdapter(this@DetallesMateria, listaAsignaciones, alumnoId)
                        lvActividades.adapter = adapter
                        Log.d(TAG, "Adaptador configurado con ${listaAsignaciones.size} asignaciones.")
                    } else {
                        Toast.makeText(this@DetallesMateria, "No hay tareas válidas para esta materia después de filtrar.", Toast.LENGTH_SHORT).show()
                        lvActividades.adapter = null // Limpiar por si acaso
                        Log.d(TAG, "La lista de asignaciones está vacía después del procesamiento.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al cargar asignaciones: ${error.message}", error.toException())
                    Toast.makeText(this@DetallesMateria, "Error al cargar tareas: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    // El Adaptador Interno no necesita ser 'internal' si solo se usa aquí, puede ser privado o simplemente anidado.
    private class AsignacionAdapter(
        // Usar 'context' en lugar de 'this@DetallesMateria' para claridad si es necesario,
        // pero como es una clase interna, tiene acceso a los miembros de DetallesMateria.
        // Sin embargo, pasar el contexto explícitamente es buena práctica.
        private val activityContext: AppCompatActivity, // Cambiado para aceptar AppCompatActivity
        private val asignaciones: List<Asignacion>,
        private val alumnoId: String?
    ) : ArrayAdapter<Asignacion>(activityContext, 0, asignaciones) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(activityContext).inflate(R.layout.item_tarea, parent, false)

            val asignacion = getItem(position) // Usar getItem(position) es más seguro

            if (asignacion == null) {
                // Manejar el caso improbable de que la asignación sea nula
                Log.e(TAG, "AsignacionAdapter: getItem($position) devolvió null.")
                // Podrías devolver una vista de error o una vista vacía
                return view // O una vista de error
            }

            val tvNombreTarea: TextView = view.findViewById(R.id.tvNombreTarea)
            val tvFechaTarea: TextView = view.findViewById(R.id.tvFechaTarea)
            val tvHoraTarea: TextView = view.findViewById(R.id.tvHoraTarea) // Asumiendo que tienes este TextView en item_tarea.xml
            val btnVerActividad: Button = view.findViewById(R.id.btnVerActividad)

            tvNombreTarea.text = asignacion.titulo ?: "Título no disponible"

            val fechaLimite = asignacion.fecha_limite ?: "Fecha no disponible"
            // Dividir la fecha y hora si es que vienen juntas o usar por defecto
            // Esto es un ejemplo, ajusta según el formato real de tu fecha_limite
            val partes = fechaLimite.split(" ")
            tvFechaTarea.text = partes.getOrNull(0) ?: fechaLimite // Si no hay espacio, muestra todo
            tvHoraTarea.text = partes.getOrNull(1) ?: "" // Si no hay segunda parte, muestra vacío

            btnVerActividad.setOnClickListener {
                // Validar que el ID de la asignación no sea nulo antes de iniciar la actividad
                if (asignacion.id == null || asignacion.id!!.isEmpty()) {
                    Toast.makeText(activityContext, "Error: Esta tarea no tiene un ID válido.", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Intento de abrir Actividad con asignacion.id null o vacío para: ${asignacion.titulo}")
                    return@setOnClickListener
                }

                if (alumnoId == null || alumnoId.isEmpty()) {
                    Toast.makeText(activityContext, "Error: ID de alumno no disponible.", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Intento de abrir Actividad con alumnoId null o vacío.")
                    return@setOnClickListener
                }

                Log.d(TAG, "Abriendo Actividad para asignacion ID: ${asignacion.id}, Titulo: ${asignacion.titulo}, Alumno ID: $alumnoId")

                val intent = Intent(activityContext, Actividad::class.java)
                intent.putExtra("alumnoId", alumnoId)
                intent.putExtra("actividad", asignacion) // Asignacion debe ser Serializable
                activityContext.startActivity(intent)
            }
            return view
        }
    }
}
