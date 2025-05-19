package zamora.jorge.aplicacionescuela

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject
import zamora.jorge.aplicacionescuela.data.EntregaBaja

class AsignacionesBjas : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EntregaBajaAdapter
    private val listaEntregas = mutableListOf<EntregaBaja>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignaciones_bjas)

        recyclerView = findViewById(R.id.recyclerEntregasBajas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = EntregaBajaAdapter(listaEntregas)
        recyclerView.adapter = adapter

        val entregasJson = intent.getStringArrayListExtra("entregasBajas")

        if (entregasJson != null) {
            for (jsonString in entregasJson) {
                try {
                    val json = JSONObject(jsonString)

                    val idAsignacion = json.optString("id_asignacion")
                    val fechaEntrega = json.optString("fecha_entrega")
                    val calificacion = json.optDouble("calificacion")
                    val archivoAdjunto = json.optString("archivo_adjunto")

                    obtenerDatosAsignacion(idAsignacion) { titulo, descripcion ->
                        val entrega = EntregaBaja(
                            titulo = titulo,
                            descripcion = descripcion,
                            fecha_entrega = fechaEntrega,
                            calificacion = calificacion,
                            archivo_adjunto = archivoAdjunto
                        )
                        listaEntregas.add(entrega)
                        adapter.notifyDataSetChanged() // <- Aquí se actualiza después de añadir
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun obtenerDatosAsignacion(
        idAsignacion: String,
        callback: (String, String) -> Unit
    ) {
        val ref = FirebaseDatabase.getInstance().getReference("asignaciones").child(idAsignacion)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val titulo = snapshot.child("titulo").getValue(String::class.java) ?: "Sin título"
                val descripcion = snapshot.child("descripcion").getValue(String::class.java) ?: ""
                callback(titulo, descripcion)
            }

            override fun onCancelled(error: DatabaseError) {
                callback("Error", "")
            }
        })
    }
}




class EntregaBajaAdapter(private val listaEntregas: List<EntregaBaja>) :
    RecyclerView.Adapter<EntregaBajaAdapter.EntregaViewHolder>() {

    class EntregaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloAsignacion)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionAsignacion)
        val tvFechaEntrega: TextView = itemView.findViewById(R.id.tvFechaEntrega)
        val tvCalificacion: TextView = itemView.findViewById(R.id.tvCalificacion)
        val tvArchivoAdjunto: TextView = itemView.findViewById(R.id.tvArchivoAdjunto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntregaViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entrega_baja, parent, false)
        return EntregaViewHolder(vista)
    }

    override fun onBindViewHolder(holder: EntregaViewHolder, position: Int) {
        val entrega = listaEntregas[position]

        holder.tvTitulo.text = entrega.titulo
        holder.tvDescripcion.text = entrega.descripcion
        holder.tvFechaEntrega.text = "Fecha de entrega: ${entrega.fecha_entrega}"
        holder.tvCalificacion.text = "Calificación: ${entrega.calificacion}"

        // Color rojo para calificación
        holder.tvCalificacion.setTextColor(Color.RED)

        // Archivo adjunto clicable
        holder.tvArchivoAdjunto.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(entrega.archivo_adjunto)
            holder.itemView.context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int = listaEntregas.size
}