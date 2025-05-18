package zamora.jorge.aplicacionescuela.data

data class Alumno(
    val curp: String? = null,
    val direccion: String? = null,
    val fecha_nacimiento: String? = null,
    val genero: String? = null,
    val id_tutor: String? = null,
    val materias: HashMap<String, String>? = null,
    val nombre_completo: String? = null,
    val telefono: String? = null
)