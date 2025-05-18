package zamora.jorge.aplicacionescuela


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import zamora.jorge.aplicacionescuela.data.Alumno

class MainActivity : AppCompatActivity() {

    private lateinit var tvNombre: TextView
    private lateinit var tvCurp: TextView
    private lateinit var btnMaterias: TextView
    private lateinit var database: DatabaseReference
    private lateinit var alumnosRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvNombre = findViewById(R.id.tvNombre)
        tvCurp = findViewById(R.id.tvCurp)
        btnMaterias = findViewById(R.id.btnMaterias)
        database = Firebase.database.reference
        alumnosRef = database.child("alumnos")

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
    }

    private fun obtenerInformacionAlumno(tutorId: String) {
        alumnosRef.orderByChild("id_tutor").equalTo(tutorId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChildren()) {
                        for (childSnapshot in snapshot.children) {
                            val alumno = childSnapshot.getValue(Alumno::class.java)
                            val alumnoId = childSnapshot.key  // <- AQUÍ
                            alumno?.let {
                                tvNombre.text = "Nombre Alumno: ${it.nombre_completo}"
                                tvCurp.text = "CURP: ${it.curp}"

                                // Configura el botón de materias con el ID correcto
                                btnMaterias.setOnClickListener {
                                    val intent = Intent(this@MainActivity, Materias::class.java)
                                    intent.putExtra("alumnoId", alumnoId) // <- CORRECTO
                                    startActivity(intent)
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
}