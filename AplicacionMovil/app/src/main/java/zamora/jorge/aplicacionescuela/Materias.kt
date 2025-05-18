package zamora.jorge.aplicacionescuela

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*
import zamora.jorge.aplicacionescuela.data.Materia

class Materias : AppCompatActivity() {

    private lateinit var lvMaterias: ListView
    private lateinit var tvTituloMaterias: TextView
    private lateinit var database: DatabaseReference
    private lateinit var alumnosRef: DatabaseReference
    private lateinit var materiasRef: DatabaseReference
    private var alumnoId: String? = null  // variable global para alumnoId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_materias)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase
        database = com.google.firebase.Firebase.database.reference
        alumnosRef = database.child("alumnos")
        materiasRef = database.child("materias")

        // Obtener alumnoId desde intent y referencias UI
        alumnoId = intent.getStringExtra("alumnoId")
        lvMaterias = findViewById(R.id.lvMaterias)
        tvTituloMaterias = findViewById(R.id.tvTituloMateria)

        if (alumnoId != null) {
            obtenerMateriasDelAlumno(alumnoId!!)
        } else {
            tvTituloMaterias.text = "Error: Alumno ID no proporcionado"
            Log.e("MateriasActivity", "Alumno ID es nullo")
        }
    }

    private fun obtenerMateriasDelAlumno(alumnoId: String) {
        // Obtener los IDs de materias que tiene el alumno
        alumnosRef.child(alumnoId).child("materias")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val materiaIds = mutableListOf<String>()
                    for (materiaIdSnapshot in snapshot.children) {
                        materiaIdSnapshot.key?.let { materiaIds.add(it) }
                    }
                    Log.d("MateriasActivity", "materiaIds: $materiaIds")
                    if (materiaIds.isNotEmpty()) {
                        obtenerDetallesMaterias(materiaIds)
                    } else {
                        tvTituloMaterias.text = "El alumno no tiene materias asignadas"
                        lvMaterias.adapter = ArrayAdapter(
                            this@Materias,
                            android.R.layout.simple_list_item_1,
                            emptyList<String>()
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MateriasActivity", "Error getting alumno materias: ${error.message}")
                    tvTituloMaterias.text = "Error al obtener materias: ${error.message}"
                }
            })
    }

    private fun obtenerDetallesMaterias(materiaIds: List<String>) {
        materiasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val materias = mutableListOf<Materia>()
                for (materiaId in materiaIds) {
                    if (snapshot.hasChild(materiaId)) {
                        val materiaSnapshot = snapshot.child(materiaId)
                        val materia = materiaSnapshot.getValue(Materia::class.java)
                        materia?.let {
                            val materiaConId = it.copy(id = materiaId)
                            materias.add(materiaConId)
                        }
                    } else {
                        Log.e("MateriasActivity", "Materia ID: $materiaId no se encuentra en la base de datos")
                    }
                }
                if (materias.isNotEmpty()) {
                    val materiaAdapter = MateriaAdapter(this@Materias, materias, alumnoId)
                    lvMaterias.adapter = materiaAdapter
                    tvTituloMaterias.text = "Materias del alumno"
                } else {
                    tvTituloMaterias.text = "No se encontraron materias"
                    lvMaterias.adapter = ArrayAdapter(
                        this@Materias,
                        android.R.layout.simple_list_item_1,
                        emptyList<String>()
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MateriasActivity", "Error obteniendo materiales: ${error.message}")
                tvTituloMaterias.text = "Error al obtener materias: ${error.message}"
            }
        })
    }

    internal class MateriaAdapter(
        context: Context,
        materias: List<Materia>,
        private val alumnoId: String?
    ) : ArrayAdapter<Materia>(context, 0, materias) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val materia = getItem(position)!!

            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_materia, parent, false)

            val tvNombreMateria: TextView = view.findViewById(R.id.tvNombreMateria)
            val ivFondoMateria: ImageView = view.findViewById(R.id.ivFondoMateria)

            tvNombreMateria.text = materia.nombre

            tvNombreMateria.setOnClickListener {
                val intent = Intent(context, DetallesMateria::class.java)
                intent.putExtra("materia", materia)
                alumnoId?.let { id -> intent.putExtra("alumnoId", id) }
                context.startActivity(intent)
            }

            // Cambiar background según materia
            val backgroundRes = when (materia.nombre) {
                "Innovacion" -> R.drawable.rounded_rectangle_background
                "Programación Web" -> R.drawable.rounded_rectangle_background
                "Aplicaciones Web" -> R.drawable.rounded_rectangle_background
                else -> R.drawable.rounded_rectangle_background
            }
            ivFondoMateria.setBackgroundResource(backgroundRes)

            return view
        }
    }
}
