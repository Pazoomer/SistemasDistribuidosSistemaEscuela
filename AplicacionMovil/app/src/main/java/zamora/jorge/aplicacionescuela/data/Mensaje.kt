package zamora.jorge.aplicacionescuela.data

data class Mensaje(
    val id: String="", // Identificador único del mensaje
    val texto: String="",
    val hora: String="",
    var esEnviado: Boolean=false, // true si es enviado por el usuario actual, false si es recibido
    val remitenteId: String="" // Opcional: para identificar quién envió el mensaje
)