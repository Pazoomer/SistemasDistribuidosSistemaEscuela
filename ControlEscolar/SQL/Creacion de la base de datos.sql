#CREATE DATABASE escuela;
USE escuela;

-- Tabla de Maestros
CREATE TABLE maestros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(100),
    curp VARCHAR(18),
    rfc VARCHAR(13),
    correo VARCHAR(100),
    telefono VARCHAR(15),
    direccion TEXT
);

-- Tabla de Materias
CREATE TABLE materias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    id_maestro INT,
    FOREIGN KEY (id_maestro) REFERENCES maestros(id)
);

-- Tabla de Tutores
CREATE TABLE tutores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(100),
    direccion TEXT,
    parentesco VARCHAR(50),
    telefono VARCHAR(15)
);

-- Tabla de Alumnos
CREATE TABLE alumnos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(100),
    curp VARCHAR(18),
    fecha_nacimiento DATE,
    direccion TEXT,
    genero ENUM('Masculino', 'Femenino', 'Otro'),
    telefono VARCHAR(15),
    id_tutor INT UNIQUE,
    FOREIGN KEY (id_tutor) REFERENCES tutores(id)
);

-- Tabla de relación Materia-Alumno (muchos a muchos)
CREATE TABLE materias_alumnos (
    id_alumno INT,
    id_materia INT,
    PRIMARY KEY (id_alumno, id_materia),
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id),
    FOREIGN KEY (id_materia) REFERENCES materias(id)
);

-- Tabla de Asignaciones
CREATE TABLE asignaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100),
    descripcion TEXT,
    fecha_limite DATE,
    id_materia INT,
    FOREIGN KEY (id_materia) REFERENCES materias(id)
);

-- Tabla de Entregas de Asignaciones
CREATE TABLE asignaciones_entregas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_asignacion INT,
    id_alumno INT,
    archivo_adjunto VARCHAR(255),
    fecha_entrega DATE,
    estado ENUM('entregado', 'pendiente', 'tarde'),
    calificacion DECIMAL(5,2),
    FOREIGN KEY (id_asignacion) REFERENCES asignaciones(id),
    FOREIGN KEY (id_alumno) REFERENCES alumnos(id)
);
