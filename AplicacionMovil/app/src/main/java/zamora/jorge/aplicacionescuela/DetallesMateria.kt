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
import zamora.jorge.aplicacionescuela.data.Asignacion
import zamora.jorge.aplicacionescuela.data.Materia

class DetallesMateria : AppCompatActivity() {

    private lateinit var tvNombreMateria: TextView
    private lateinit var lvActividades: ListView
    private lateinit var database: DatabaseReference
    private lateinit var asignacionesRef: DatabaseReference
    private var alumnoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_materia)

        tvNombreMateria = findViewById(R.id.tvNombreMateria)
        lvActividades = findViewById(R.id.lvActividades)

        // Obtener la materia pasada por el intent
        val materia = intent.getSerializableExtra("materia") as? Materia
        alumnoId = intent.getStringExtra("alumnoId")
        if (materia == null) {
            Toast.makeText(this, "Error al cargar materia", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvNombreMateria.text = materia.nombre

        // Inicializar Firebase
        database = FirebaseDatabase.getInstance().reference
        asignacionesRef = database.child("asignaciones")

        // Obtener asignaciones de esta materia
        cargarAsignacionesDeMateria(materia.id)
    }

    private fun cargarAsignacionesDeMateria(idMateria: String) {
        asignacionesRef.orderByChild("id_materia").equalTo(idMateria)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaAsignaciones = mutableListOf<Asignacion>()
                    for (asignacionSnapshot in snapshot.children) {
                        val asignacion = asignacionSnapshot.getValue(Asignacion::class.java)
                        if (asignacion != null) {
                            listaAsignaciones.add(asignacion)
                        }
                    }
                    if (listaAsignaciones.isNotEmpty()) {
                        val adapter = AsignacionAdapter(this@DetallesMateria, listaAsignaciones, alumnoId)
                        lvActividades.adapter = adapter
                    } else {
                        Toast.makeText(this@DetallesMateria, "No hay tareas para esta materia", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DetallesMateria", "Error al cargar asignaciones: ${error.message}")
                    Toast.makeText(this@DetallesMateria, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    internal class AsignacionAdapter(context: DetallesMateria, private val asignaciones: List<Asignacion>, private val alumnoId: String?) :
        ArrayAdapter<Asignacion>(context, 0, asignaciones) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_tarea, parent, false)

            val asignacion = asignaciones[position]

            val tvNombreTarea: TextView = view.findViewById(R.id.tvNombreTarea)
            val tvFechaTarea: TextView = view.findViewById(R.id.tvFechaTarea)
            val tvHoraTarea: TextView = view.findViewById(R.id.tvHoraTarea)
            val btnVerActividad: Button = view.findViewById(R.id.btnVerActividad)

            tvNombreTarea.text = asignacion.titulo

            // Dividir la fecha y hora si es que vienen juntas o usar por defecto
            val partes = asignacion.fecha_limite?.split(" ") ?: listOf("Fecha no disponible", "")
            tvFechaTarea.text = partes.getOrNull(0) ?: "Fecha no disponible"
            tvHoraTarea.text = partes.getOrNull(1) ?: ""

            btnVerActividad.setOnClickListener {
                val intent = Intent(context, Actividad::class.java)
                alumnoId?.let { id -> intent.putExtra("alumnoId", id) }
                intent.putExtra("actividad", asignacion)  // Asegúrate que Asignacion es Serializable o Parcelable
                context.startActivity(intent)
            }


            return view
        }
    }
}
