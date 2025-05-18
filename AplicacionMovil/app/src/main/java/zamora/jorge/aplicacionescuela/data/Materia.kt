package zamora.jorge.aplicacionescuela.data

import java.io.Serializable

data class Materia(
    val id: String = "",
    val id_maestro: String? = null,
    val nombre: String? = null
) : Serializable
