package zamora.jorge.aplicacionescuela.data

import androidx.annotation.DrawableRes

data class Conversacion(
    val contactId: String, // ID único del contacto o de la conversación
    val contactName: String,
    @DrawableRes val profileImageResId: Int, // ID del recurso drawable para la imagen de perfil
    val lastMessage: String,
    val timestamp: String
)