package zamora.jorge.aplicacionescuela.data

data class EntregaBaja(
    val titulo: String = "",
    val descripcion: String = "",
    val fecha_entrega: String = "",
    val calificacion: Double = 0.0,
    val archivo_adjunto: String
)