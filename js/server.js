const express = require('express');
const cors = require('cors');
const mysql = require('mysql2');
const { parse } = require('url');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const dbUrl = process.env.DATABASE_URL;

console.log(`Conectando a la base de datos: ${dbUrl}`);

const parsed = parse(dbUrl);
const [user, password] = parsed.auth.split(':');

const pool = mysql.createPool({
  host: parsed.hostname,
  port: parsed.port,
  user,
  password,
  database: parsed.pathname.replace('/', ''),
  waitForConnections: true,
  connectionLimit: 10
}).promise();

app.post('/subir-calificaciones', async (req, res) => {
  const calificaciones = req.body; // Debe ser un array de objetos

  if (!Array.isArray(calificaciones)) {
    return res.status(400).json({ error: "Se esperaba un array de calificaciones" });
  }

  try {
    for (const cal of calificaciones) {
      const {
        curp_alumno,
        materia,
        nombre_asignacion,
        calificacion,
        id_maestro
      } = cal;

      // Insertar en la tabla calificaciones
      await pool.query(
        `INSERT INTO calificaciones (curp_alumno, materia, nombre_asignacion, calificacion, id_maestro) 
         VALUES (?, ?, ?, ?, ?)`,
        [curp_alumno, materia, nombre_asignacion, calificacion, id_maestro]
      );
    }

    res.status(200).json({ mensaje: "Calificaciones guardadas correctamente" });
  } catch (error) {
    console.error("Error al subir calificaciones:", error);
    res.status(500).json({ error: "Error interno al subir calificaciones" });
  }
});

// Obtener usuario por correo y contraseña
app.post('/login', async (req, res) => {
  const { correo, contrasena } = req.body;
  try {
    const [rows] = await pool.query('SELECT * FROM usuarios WHERE correo = ? AND contrasena = ?', [correo, contrasena]);
    if (rows.length > 0) {
      res.json(rows[0]);
    } else {
      res.status(401).send('Credenciales incorrectas');
    }
  } catch (err) {
    console.error(err);
    res.status(500).send('Error al autenticar usuario');
  }
});

app.listen(PORT, () => {
  console.log(`Servidor escuchando en puerto: ${PORT}`);
});

// Rol: Maestros
// Subir calificaciones de un maestro (Maestro, Materias, asignaciones, asignaciones-entregas, curp del alumno, calificacion)

// Crear asignacion (Titulo, Descripcion, Fecha limite, Materia)
app.post('/asignaciones', async (req, res) => {
  const { titulo, descripcion, fecha_limite, id_materia } = req.body;
  try {
    await pool.query(
      'INSERT INTO asignaciones (titulo, descripcion, fecha_limite, id_materia) VALUES (?, ?, ?, ?)',
      [titulo, descripcion, fecha_limite, id_materia]
    );
    res.send('Asignación creada');
  } catch (err) {
    console.error(err);
    res.status(500).send('Error al crear asignación');
  }
});

// Editar asignacion (Titulo, Descripcion, Fecha limite, Materia)
app.put('/asignaciones/:id', async (req, res) => {
  const { titulo, descripcion, fecha_limite, id_materia } = req.body;
  const { id } = req.params;
  try {
    await pool.query(
      'UPDATE asignaciones SET titulo = ?, descripcion = ?, fecha_limite = ?, id_materia = ? WHERE id = ?',
      [titulo, descripcion, fecha_limite, id_materia, id]
    );
    res.send('Asignación actualizada');
  } catch (err) {
    console.error(err);
    res.status(500).send('Error al actualizar asignación');
  }
});

// Eliminar asignacion (Titulo, Descripcion, Fecha limite, Materia)
app.delete('/asignaciones/:id', async (req, res) => {
  const { id } = req.params;
  try {
    await pool.query('DELETE FROM asignaciones WHERE id = ?', [id]);
    res.send('Asignación eliminada');
  } catch (err) {
    console.error(err);
    res.status(500).send('Error al eliminar asignación');
  }
});

// Calificar asignación-entrega (Alumno, Asignacion, estado, calificacion)
app.put('/entregas/calificar/:id', async (req, res) => {
  const { estado, calificacion } = req.body;
  const { id } = req.params;
  try {
    await pool.query(
      'UPDATE asignaciones_entregas SET estado = ?, calificacion = ? WHERE id = ?',
      [estado, calificacion, id]
    );
    res.send('Entrega calificada');
  } catch (err) {
    console.error(err);
    res.status(500).send('Error al calificar entrega');
  }
});

// Obtener materias-maestro (Maestro)
app.get('/materias/maestro/:id_maestro', async (req, res) => {
  const { id_maestro } = req.params;
  try {
    const [rows] = await pool.query('SELECT * FROM materias WHERE id_maestro = ?', [id_maestro]);
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).send('Error al obtener materias del maestro');
  }
});

// Rol: Estudiante
// Entregar asignación-entrega (Alumno, Asignacion, link entrega, fecha entrega)
app.post('/entregas', async (req, res) => {
  const { id_asignacion, id_alumno, archivo_adjunto, fecha_entrega, estado } = req.body;
  try {
    await pool.query(
      'INSERT INTO asignaciones_entregas (id_asignacion, id_alumno, archivo_adjunto, fecha_entrega, estado) VALUES (?, ?, ?, ?, ?)',
      [id_asignacion, id_alumno, archivo_adjunto, fecha_entrega, estado]
    );
    res.send('Asignación entregada');
  } catch (err) {
    console.error(err);
    res.status(500).send('Error al entregar asignación');
  }
});

// Obtener materias-alumno (Alumno)
app.get('/materias/alumno/:id_alumno', async (req, res) => {
  const { id_alumno } = req.params;
  try {
    const [rows] = await pool.query(
      `SELECT m.* FROM materias m 
       JOIN materias_alumnos ma ON m.id = ma.id_materia 
       WHERE ma.id_alumno = ?`,
      [id_alumno]
    );
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).send('Error al obtener materias del alumno');
  }
});

// Rol: Ambos
// Obtener asignaciones por materia (Materia)
app.get('/asignaciones/materia/:id_materia', async (req, res) => {
  const { id_materia } = req.params;
  try {
    const [rows] = await pool.query('SELECT * FROM asignaciones WHERE id_materia = ?', [id_materia]);
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).send('Error al obtener asignaciones');
  }
});

//Cargar materias de ejemplo
app.get('/cargar_materias', async (req, res) => {
  try {
    const materias = [
      { nombre: 'Matemáticas', id_maestro: 1 },
      { nombre: 'Historia', id_maestro: 2 },
      { nombre: 'Ciencias', id_maestro: 3 }
    ];
    await pool.query('INSERT INTO materias (nombre, id_maestro) VALUES ?', [materias.map(m => [m.nombre, m.id_maestro])]);
    res.send('Materias cargadas');
  } catch (err) {
    console.error(err);
    res.status(500).send('Error al cargar materias');
  }
});

//Insertar datos de ejemplo
app.post('/insertar-firebase', async (req, res) => {
  try {
    // 1. Maestro
    const maestroRef = await addDoc(collection(db, 'maestros'), {
      nombre_completo: 'Juan Pérez',
      curp: 'JUAP800101HSONRL09',
      rfc: 'JUAP800101ABC',
      correo: 'juan.perez@escuela.com',
      telefono: '6621234567',
      direccion: 'Calle Falsa 123'
    });

    // 2. Materia
    const materiaRef = await addDoc(collection(db, 'materias'), {
      nombre: 'Matemáticas',
      id_maestro: maestroRef.id
    });

    // 3. Tutor
    const tutorRef = await addDoc(collection(db, 'tutores'), {
      nombre_completo: 'Laura Gómez',
      direccion: 'Av. Reforma 456',
      parentesco: 'Madre',
      telefono: '6627654321'
    });

    // 4. Alumno
    const alumnoRef = await addDoc(collection(db, 'alumnos'), {
      nombre_completo: 'Carlos Pérez',
      curp: 'CAPC050505HSRMRL03',
      fecha_nacimiento: '2005-05-05',
      direccion: 'Calle Falsa 123',
      genero: 'Masculino',
      telefono: '6625554321',
      id_tutor: tutorRef.id
    });

    // 5. Relación materia-alumno
    await addDoc(collection(db, 'materias_alumnos'), {
      id_alumno: alumnoRef.id,
      id_materia: materiaRef.id
    });

    // 6. Asignación
    const asignacionRef = await addDoc(collection(db, 'asignaciones'), {
      titulo: 'Tarea 1',
      descripcion: 'Resolver ejercicios de álgebra',
      fecha_limite: '2025-05-20',
      id_materia: materiaRef.id
    });

    // 7. Entrega de asignación
    await addDoc(collection(db, 'asignaciones_entregas'), {
      id_asignacion: asignacionRef.id,
      id_alumno: alumnoRef.id,
      archivo_adjunto: 'tarea1.pdf',
      fecha_entrega: '2025-05-18',
      estado: 'entregado',
      calificacion: 9.5
    });

    res.send('Datos de ejemplo insertados en Firebase');
  } catch (err) {
    console.error(err);
    res.status(500).send(`Error al insertar en Firebase: ${err.message}`);
  }
});

// Obtener maestros
app.get('/maestros', async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT * FROM maestros');
    res.json(rows);
  } catch (err) {
    console.error(err);
    res.status(500).send('Error al obtener datos');
  }
});

// Agregar maestro
app.post('/maestros', async (req, res) => {
  const { nombre_completo, curp, rfc, correo, telefono, direccion } = req.body;
  try {
    const sql = 'INSERT INTO maestros (nombre_completo, curp, rfc, correo, telefono, direccion) VALUES (?, ?, ?, ?, ?, ?)';
    const values = [nombre_completo, curp, rfc, correo, telefono, direccion];
    await pool.query(sql, values);
    res.send('Maestro agregado');
  } catch (err) {
    console.error(err);
    res.status(500).send(`Error al agregar maestro: ${err.message}`);
  }
});