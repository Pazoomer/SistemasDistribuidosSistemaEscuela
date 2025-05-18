package zamora.jorge.aplicacionescuela

import android.os.Bundle
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

    private lateinit var database: DatabaseReference
    private var idAlumno: String? = null
    private var idAsignacion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad)

        tvNombreTarea = findViewById(R.id.tvNombreTarea)
        tvFechaHoraTarea = findViewById(R.id.tvFechaHoraTarea)
        tvDescripcionTarea = findViewById(R.id.tvDescripcionTarea)
        tvPdf = findViewById(R.id.tvPdf)

        database = FirebaseDatabase.getInstance().reference

        // Obtener datos del intent
        val asignacion = intent.getSerializableExtra("actividad") as? Asignacion
        idAlumno = intent.getStringExtra("alumnoId")

        if (asignacion != null && idAlumno != null) {
            mostrarInformacionBasica(asignacion)
            idAsignacion = asignacion.id
            cargarAsignacionEntrega()
        } else {
            Toast.makeText(this, "Datos insuficientes para mostrar la tarea", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun mostrarInformacionBasica(asignacion: Asignacion) {
        tvNombreTarea.text = asignacion.titulo ?: "Título no disponible"
        tvFechaHoraTarea.text = asignacion.fecha_limite ?: "Fecha límite no disponible"
        tvDescripcionTarea.text = asignacion.descripcion ?: "Descripción no disponible"
    }

    private fun cargarAsignacionEntrega() {
        val ref = database.child("asignaciones_entrega")
        ref.orderByChild("id_asignacion").equalTo(idAsignacion)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (entregaSnapshot in snapshot.children) {
                        val idAlumnoEntrega = entregaSnapshot.child("id_alumno").getValue(String::class.java)
                        if (idAlumnoEntrega == idAlumno) {
                            val archivoAdjunto = entregaSnapshot.child("archivo_adjunto").getValue(String::class.java)
                            tvPdf.text = archivoAdjunto ?: "Sin archivo adjunto"
                            return
                        }
                    }
                    tvPdf.text = "Sin archivo entregado"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Actividad, "Error al cargar archivo: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
