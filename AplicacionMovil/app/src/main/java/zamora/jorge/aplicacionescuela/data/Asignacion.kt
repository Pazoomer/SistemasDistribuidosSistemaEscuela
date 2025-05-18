package zamora.jorge.aplicacionescuela.data

import java.io.Serializable

data class Asignacion(
    var id: String? = null,
    val titulo: String? = null,
    val descripcion: String? = null,
    val fecha_limite: String? = null
) : java.io.Serializable

