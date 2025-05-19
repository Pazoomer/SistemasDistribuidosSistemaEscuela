package zamora.jorge.aplicacionescuela

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import zamora.jorge.aplicacionescuela.data.Mensaje
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class Chat : AppCompatActivity() {

    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSendMessage: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private val listaMensajes = mutableListOf<Mensaje>()
    private val USUARIO_ACTUAL_ID = "WCwuXdhA6Lg5W0epyoErf79q0Zv1" // Identificador para el usuario que usa la app
    private val OTRO_USUARIO_ID = "-OOZmkq2hhGdFUMYfyCN" // Identificador del contacto
    val database = FirebaseDatabase.getInstance()
    val chatRef = database.getReference("chats/${USUARIO_ACTUAL_ID}_${OTRO_USUARIO_ID}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val toolbar: Toolbar = findViewById(R.id.toolbarChat)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Opcional: para botón de atrás
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configurar información de la Toolbar
        val contactNameTextView: TextView = findViewById(R.id.textViewContactName)
        val contactStatusTextView: TextView = findViewById(R.id.textViewContactStatus)
        // val profileImageViewToolbar: ImageView = findViewById(R.id.imageViewProfileToolbar) // Ya se carga por XML con @drawable/chuy

        contactNameTextView.text = "Prof. Juan Perez" // Puedes obtener esto de un Intent, etc.
        contactStatusTextView.text = "Matemáticas"

        // Manejo de Insets para EdgeToEdge
        val mainLayout = findViewById<android.view.View>(R.id.main_chat_container)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSendMessage = findViewById(R.id.buttonSendMessage)

        chatAdapter = ChatAdapter(listaMensajes)
        val layoutManager = LinearLayoutManager(this)
        // layoutManager.stackFromEnd = true // Para que la lista comience desde abajo
        recyclerViewMessages.layoutManager = layoutManager
        recyclerViewMessages.adapter = chatAdapter

        cargarMensajesDeEjemplo()

        buttonSendMessage.setOnClickListener {
            val textoMensaje = editTextMessage.text.toString().trim()
            if (textoMensaje.isNotEmpty()) {
                enviarMensaje(textoMensaje)
            }
        }

        // Opcional: Si habilitaste setDisplayHomeAsUpEnabled, maneja su acción
        toolbar.setNavigationOnClickListener {
            finish() // O la lógica que prefieras para el botón de atrás
        }

        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val mensaje = snapshot.getValue(Mensaje::class.java)
                mensaje?.let {
                    it.esEnviado = it.remitenteId == USUARIO_ACTUAL_ID
                    chatAdapter.addMensaje(it)
                    recyclerViewMessages.scrollToPosition(chatAdapter.itemCount - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        })

    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun enviarMensaje(texto: String) {
        val mensajeId = UUID.randomUUID().toString()
        val mensaje = Mensaje(
            id = mensajeId,
            texto = texto,
            hora = getCurrentTime(),
            esEnviado = true,
            remitenteId = USUARIO_ACTUAL_ID
        )
        chatRef.child(mensajeId).setValue(mensaje)

        // Mostrar en UI
        chatAdapter.addMensaje(mensaje)
        editTextMessage.setText("")
        recyclerViewMessages.scrollToPosition(chatAdapter.itemCount - 1)
    }

    private fun cargarMensajesDeEjemplo() {
        listaMensajes.add(Mensaje(UUID.randomUUID().toString(),"Buenas tardes, señor López. Su hijo ha mejorado mucho en pronunciación.", "9:30 AM", false, OTRO_USUARIO_ID))
        listaMensajes.add(Mensaje(UUID.randomUUID().toString(),"¡Qué bueno! Ha estado practicando en casa.", "9:31 AM", true, USUARIO_ACTUAL_ID))
        listaMensajes.add(Mensaje(UUID.randomUUID().toString(),"Se nota. Solo necesita trabajar en la gramática.", "9:32 AM", false, OTRO_USUARIO_ID))
        listaMensajes.add(Mensaje(UUID.randomUUID().toString(),"De acuerdo, lo ayudaré con eso.", "9:33 AM", true, USUARIO_ACTUAL_ID))
        // Añade más mensajes si es necesario
        chatAdapter.notifyDataSetChanged()
        if (chatAdapter.itemCount > 0) {
            recyclerViewMessages.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }
}

class ChatAdapter(private val mensajes: MutableList<Mensaje>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TIPO_VISTA_ENVIADO = 1
        private const val TIPO_VISTA_RECIBIDO = 2
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewMessageContent: TextView = itemView.findViewById(R.id.textViewMessageContent)
        private val textViewMessageTimestamp: TextView = itemView.findViewById(R.id.textViewMessageTimestamp)

        fun bind(mensaje: Mensaje) {
            textViewMessageContent.text = mensaje.texto
            textViewMessageTimestamp.text = mensaje.hora
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewMessageContent: TextView = itemView.findViewById(R.id.textViewMessageContent)
        private val textViewMessageTimestamp: TextView = itemView.findViewById(R.id.textViewMessageTimestamp)

        fun bind(mensaje: Mensaje) {
            textViewMessageContent.text = mensaje.texto
            textViewMessageTimestamp.text = mensaje.hora
        }
    }

    override fun getItemViewType(position: Int): Int {
        val mensaje = mensajes[position]
        return if (mensaje.esEnviado) {
            TIPO_VISTA_ENVIADO
        } else {
            TIPO_VISTA_RECIBIDO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_VISTA_ENVIADO) {
            val view = inflater.inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else { // TIPO_VISTA_RECIBIDO
            val view = inflater.inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mensaje = mensajes[position]
        when (holder) {
            is SentMessageViewHolder -> holder.bind(mensaje)
            is ReceivedMessageViewHolder -> holder.bind(mensaje)
        }
    }

    override fun getItemCount(): Int = mensajes.size

    fun addMensaje(mensaje: Mensaje) {
        mensajes.add(mensaje)
        notifyItemInserted(mensajes.size - 1)
    }
}