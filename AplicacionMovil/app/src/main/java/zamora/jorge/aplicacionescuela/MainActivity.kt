package zamora.jorge.aplicacionescuela


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import org.json.JSONObject
import zamora.jorge.aplicacionescuela.data.Alumno

class MainActivity : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var tvCurp: TextView
    private lateinit var btnMaterias: TextView
    private lateinit var btnMensajes: TextView
    private lateinit var btnCerrarSesion: TextView
    private lateinit var btnBajoRendimiento: Button
    private var entregasBajasList = mutableListOf<DataSnapshot>()
    private lateinit var database: DatabaseReference
    private lateinit var alumnosRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvNombre = findViewById(R.id.tvNombre)
        tvCurp = findViewById(R.id.tvCurp)
        btnMaterias = findViewById(R.id.btnMaterias)
        btnMensajes = findViewById(R.id.btnMensajes)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        btnBajoRendimiento = findViewById(R.id.btnBajoRendimiento)
        btnBajoRendimiento.visibility = Button.GONE

        database = Firebase.database.reference
        alumnosRef = database.child("alumnos")
        auth = FirebaseAuth.getInstance()

        // Obtener el usuario (tutor) del Intent
        val user = intent.getParcelableExtra<FirebaseUser>("user")

        // Verificar si se recibió el usuario
        if (user != null) {
            // Ahora obtener la información del alumno asociado a este tutor
            obtenerInformacionAlumno(user.uid) // Usar el uid del tutor para buscar al alumno

        } else {
            // Manejar el caso en que no se recibió el usuario
            tvNombre.text = "Error: No se recibió información del tutor."
            tvCurp.text = ""
        }

        btnMensajes.setOnClickListener {
            val intent = Intent(this, Mensajes::class.java)
            startActivity(intent)
        }

        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }


    private fun cerrarSesion() {
        auth.signOut() // Cierra la sesión en Firebase Authentication

        val intent = Intent(this, Login::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish() // Cierra MainActivity
    }

    private fun obtenerInformacionAlumno(tutorId: String) {
        alumnosRef.orderByChild("id_tutor").equalTo(tutorId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        for (childSnapshot in snapshot.children) {
                            val alumno = childSnapshot.getValue(Alumno::class.java)
                            val alumnoId = childSnapshot.key
                            alumno?.let {
                                tvNombre.text = "Nombre Alumno: ${it.nombre_completo}"
                                tvCurp.text = "CURP: ${it.curp}"

                                btnMaterias.setOnClickListener {
                                    val intent = Intent(this@MainActivity, Materias::class.java)
                                    intent.putExtra("alumnoId", alumnoId)
                                    startActivity(intent)
                                }

                                // 🔽 Llama a la función de filtrado
                                if (alumnoId != null) {
                                    obtenerEntregasBajas(alumnoId)
                                }

                                return
                            }
                        }
                    } else {
                        tvNombre.text = "No se encontró alumno asociado a este tutor."
                        tvCurp.text = ""
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    tvNombre.text = "Error al buscar alumno: ${error.message}"
                    tvCurp.text = ""
                }
            })
    }


    private fun obtenerEntregasBajas(alumnoId: String) {
        val entregasRef = database.child("asignaciones_entregas")
        val entregasBajasJsonList = mutableListOf<String>()

        entregasRef.orderByChild("id_alumno").equalTo(alumnoId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (entregaSnapshot in snapshot.children) {
                        val calificacion = entregaSnapshot.child("calificacion").getValue(Double::class.java)

                        if (calificacion != null && calificacion <= 7.0) {
                            val idAsignacion = entregaSnapshot.child("id_asignacion").getValue(String::class.java) ?: ""
                            val fechaEntrega = entregaSnapshot.child("fecha_entrega").getValue(String::class.java) ?: ""
                            val archivoAdjunto = entregaSnapshot.child("archivo_adjunto").getValue(String::class.java) ?: ""

                            // Convertir en JSON válido
                            val entregaJson = JSONObject().apply {
                                put("id_asignacion", idAsignacion)
                                put("fecha_entrega", fechaEntrega)
                                put("calificacion", calificacion)
                                put("archivo_adjunto", archivoAdjunto)
                            }

                            entregasBajasJsonList.add(entregaJson.toString())
                        }
                    }

                    // Mostrar botón si hay entregas bajas
                    if (entregasBajasJsonList.isNotEmpty()) {
                        btnBajoRendimiento.visibility = View.VISIBLE

                        btnBajoRendimiento.setOnClickListener {
                            val intent = Intent(this@MainActivity, AsignacionesBjas::class.java)
                            intent.putStringArrayListExtra("entregasBajas", ArrayList(entregasBajasJsonList))
                            startActivity(intent)
                        }
                    } else {
                        btnBajoRendimiento.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error al consultar entregas: ${error.message}")
                }
            })
    }

}