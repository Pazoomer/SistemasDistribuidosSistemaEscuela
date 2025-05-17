package zamora.jorge.aplicacionescuela

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = Color.BLACK

        val iniciarSesion:Button = findViewById(R.id.btnIniciarSesion)
        val etCorreo:EditText = findViewById(R.id.etCorreo)
        val etContrasena:EditText = findViewById(R.id.etContrasena)
        val tvError:TextView = findViewById(R.id.tvError)

        auth = Firebase.auth


        iniciarSesion.setOnClickListener {
            val correo: String = etCorreo.text.toString()
            val password: String = etContrasena.text.toString()
            login(correo, password)
        }




        
    }


    /**
     * Se llama cuando la actividad se vuelve visible para el usuario.
     * Verifica si hay un usuario actualmente autenticado y, si lo hay,
     * navega directamente a la actividad CreateJoinHome.
     */
    public override fun onStart()
    {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            goToMain(currentUser)
        }
    }

    /**
     * Intenta iniciar sesión en Firebase Authentication con el correo electrónico y la contraseña proporcionados.
     * Si el inicio de sesión es exitoso, llama a `goToMain` para navegar a la siguiente actividad.
     * Si falla, muestra un mensaje de error en el TextView.
     *
     * @param email El correo electrónico del usuario.
     * @param password La contraseña del usuario.
     */
    fun login(email: String, password: String) {
        // Autenticamos al usuario con sus credenciales
        if ( email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingrese todos sus datos", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar correo
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso, obtener el usuario actual y navegar a la siguiente actividad
                    val user = auth.currentUser
                    showError(visible = false)
                    goToMain(user!!)
                }
                else if (password.toString().length<6){
                    showError(text = "La contraseña debe tener al menos 6 caracteres", visible = true)
                }
                else {
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    showError(text = "Correo o contraseña incorrectas", visible = true)
                }
            }
    }

    /**
     * Navega a la actividad CreateJoinHome después de un inicio de sesión exitoso.
     * Pasa el correo electrónico del usuario como un extra al Intent.
     * Limpia la pila de actividades para evitar volver a la actividad de inicio de sesión con el botón de retroceso.
     *
     * @param user El objeto FirebaseUser del usuario autenticado.
     */
    private fun goToMain(user: FirebaseUser){
        val intent: Intent = Intent(this,MainActivity::class.java)
        intent.putExtra("user",user.email)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    /**
     * Muestra u oculta el TextView de error con el mensaje proporcionado.
     *
     * @param text El texto del error a mostrar. Si está vacío, se usará el texto actual.
     * @param visible Booleano que indica si el TextView de error debe ser visible (true) u oculto (false).
     */
    private fun showError(text: String="", visible: Boolean){
        val error: TextView =findViewById(R.id.tvError)
        error.text=text
        error.visibility= if(visible) View.VISIBLE else View.INVISIBLE
    }

}


