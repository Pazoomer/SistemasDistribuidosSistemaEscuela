package zamora.jorge.aplicacionescuela

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout // Importa LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zamora.jorge.aplicacionescuela.data.Conversacion

class Mensajes : AppCompatActivity() {

    private lateinit var recyclerViewChats: RecyclerView // Cambiado el nombre de la variable
    private lateinit var conversacionesAdapter: ConversacionesAdapter
    private val listaConversaciones = mutableListOf<Conversacion>()
    // private var idUsuarioActual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mensajes) // Usa tu layout

        // Ajuste de padding para Edge-to-Edge usando el ID de tu LinearLayout raíz
        val mainLayout = findViewById<LinearLayout>(R.id.main) // Usando R.id.main

        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Aplicamos el padding del sistema ADEMÁS del padding que ya tiene tu LinearLayout
            // Asumiendo que el padding="16dp" de tu XML es para el contenido DENTRO de las barras
            val density = resources.displayMetrics.density
            val padding16dp = (16 * density).toInt() // Convierte 16dp a píxeles

            v.setPadding(
                systemBars.left + padding16dp,
                systemBars.top + padding16dp,
                systemBars.right + padding16dp,
                systemBars.bottom + padding16dp
            )
            insets
        }

        recyclerViewChats = findViewById(R.id.recyclerViewChats) // Usando el ID de tu RecyclerView
        setupRecyclerView()
        cargarConversacionesDeEjemplo()
    }

    private fun setupRecyclerView() {
        conversacionesAdapter = ConversacionesAdapter(listaConversaciones) { conversacion ->
            val intent = Intent(this, Chat::class.java)
            intent.putExtra("CONTACT_ID", conversacion.contactId)
            intent.putExtra("CONTACT_NAME", conversacion.contactName)
            intent.putExtra("CONTACT_IMAGE_RES_ID", conversacion.profileImageResId)
            startActivity(intent)
        }
        recyclerViewChats.layoutManager = LinearLayoutManager(this)
        recyclerViewChats.adapter = conversacionesAdapter
    }

    private fun cargarConversacionesDeEjemplo() {
        listaConversaciones.add(
            Conversacion(
                "prof_juan_perez",
                "Prof. Juan Perez",
                R.drawable.chuy,
                "Esta bien maestro",
                "09:38 AM"
            )
        )
        conversacionesAdapter.notifyDataSetChanged()
    }
}

    class ConversacionesAdapter(
        private val conversaciones: List<Conversacion>,
        private val onItemClicked: (Conversacion) -> Unit
    ) : RecyclerView.Adapter<ConversacionesAdapter.ConversacionViewHolder>() {

        inner class ConversacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            // IDs tomados de tu XML para el item de la lista de chats/conversaciones
            private val imageViewProfile: ImageView = itemView.findViewById(R.id.imageViewProfile)
            private val textViewContactName: TextView =
                itemView.findViewById(R.id.textViewSenderName) // Tu XML usa textViewSenderName
            private val textViewLastMessage: TextView =
                itemView.findViewById(R.id.textViewLastMessage)
            private val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)

            fun bind(conversacion: Conversacion) {
                textViewContactName.text = conversacion.contactName
                textViewLastMessage.text = conversacion.lastMessage
                textViewTimestamp.text = conversacion.timestamp
                imageViewProfile.setImageResource(conversacion.profileImageResId) // O usa Glide/Picasso si son URLs

                itemView.setOnClickListener {
                    onItemClicked(conversacion)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversacionViewHolder {
            // Asegúrate de inflar el nombre correcto de tu archivo XML aquí.
            // Si tu archivo se llama 'item_chat_preview.xml', usa R.layout.item_chat_preview
            // Si tu archivo se llama 'item_chat.xml' (y es el que me mostraste), usa R.layout.item_chat
            val view = LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_chat,
                    parent,
                    false
                ) // <--- CAMBIA ESTO AL NOMBRE REAL DE TU ARCHIVO XML
            return ConversacionViewHolder(view)
        }

        override fun onBindViewHolder(holder: ConversacionViewHolder, position: Int) {
            holder.bind(conversaciones[position])
        }

        override fun getItemCount(): Int = conversaciones.size
    }