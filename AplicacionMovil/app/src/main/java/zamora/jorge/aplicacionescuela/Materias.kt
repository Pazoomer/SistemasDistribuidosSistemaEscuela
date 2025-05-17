package zamora.jorge.aplicacionescuela

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import zamora.jorge.aplicacionescuela.data.Maestro

class Materias : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_materias)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listView = findViewById<ListView>(R.id.lvMaterias)

        database = FirebaseDatabase.getInstance().reference.child("maestros")
        cargarMaestros { maestros ->
            val adapter = MaestroAdapter(this, maestros.toMutableList())
            listView.adapter = adapter
        }
    }

    fun cargarMaestros(callback: (List<Maestro>) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaMaestros = mutableListOf<Maestro>()
                for (maestroSnap in snapshot.children) {
                    val maestro = maestroSnap.getValue(Maestro::class.java)
                    if (maestro != null) {
                        listaMaestros.add(maestro)
                    }
                }
                callback(listaMaestros)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al cargar maestros", error.toException())
                callback(emptyList())
            }
        })
    }

    class MaestroAdapter(private val context: Context,
                         private var data: MutableList<Maestro>) : BaseAdapter() {

        override fun getCount(): Int = data.size

        override fun getItem(position: Int): Any = data[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_maestro, parent, false)

            view.findViewById<TextView>(R.id.nombre_completo).text = data[position].nombre_completo
            view.findViewById<TextView>(R.id.curp).text = data[position].curp
            view.findViewById<TextView>(R.id.rfc).text = data[position].rfc
            view.findViewById<TextView>(R.id.correo).text = data[position].correo
            view.findViewById<TextView>(R.id.telefono).text = data[position].telefono
            view.findViewById<TextView>(R.id.direccion).text = data[position].direccion

            return view
        }
    }
}