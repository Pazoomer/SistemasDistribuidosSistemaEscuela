// firebase.js
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.6.0/firebase-app.js";

import {
  getDatabase,
  ref,
  set,
  get,
  child,
  update,
  push
} from "https://www.gstatic.com/firebasejs/11.6.0/firebase-database.js";

import { getAuth, signOut, signInWithEmailAndPassword } from "https://www.gstatic.com/firebasejs/11.6.0/firebase-auth.js";

const firebaseConfig = {
  apiKey: "AIzaSyCT73pxUNL8KTa8Wa_5IPZ2zFA27S7vHw8",
  authDomain: "sistemaescuela-9bc14.firebaseapp.com",
  databaseURL: "https://sistemaescuela-9bc14-default-rtdb.firebaseio.com",
  projectId: "sistemaescuela-9bc14",
  storageBucket: "sistemaescuela-9bc14.firebasestorage.app",
  messagingSenderId: "505482865718",
  appId: "1:505482865718:web:b9b3d9bb7539e6b00dfc97"
};

const app = initializeApp(firebaseConfig);
const db = getDatabase(app);
const auth = getAuth(app);

console.log("Firebase inicializado correctamente");

export function enviarMensaje(chatId, remitenteId, texto) {
  const chatRef = ref(db, `chats/${chatId}`);
  const mensaje = {
    texto: texto,
    remitenteId: remitenteId,
    timestamp: Date.now()
  };
  return push(chatRef, mensaje);
}

export async function obtenerTodosLosChatsDeUsuario(usuarioActualId) {
  const chatsRef = ref(db, "chats");

  try {
    const snapshot = await get(chatsRef);
    if (snapshot.exists()) {
      const todosLosChats = snapshot.val();
      const chatsDelUsuario = [];

      for (const chatKey in todosLosChats) {
        if (chatKey.includes(usuarioActualId)) {
          const mensajes = Object.values(todosLosChats[chatKey]);
          chatsDelUsuario.push({
            chatId: chatKey,
            mensajes: mensajes
          });
        }
      }

      return chatsDelUsuario;
    } else {
      return [];
    }
  } catch (error) {
    console.error("Error al obtener chats:", error);
    return [];
  }
}

export async function obtenerEntregasPorAsignacion(idAsignacion) {
  const dbRef = ref(db);

  try {
    // Obtener entregas
    const snapshotEntregas = await get(child(dbRef, 'asignaciones_entregas'));
    if (!snapshotEntregas.exists()) return [];

    const entregas = snapshotEntregas.val();

    // Filtrar entregas por la asignación
    const entregasFiltradas = Object.entries(entregas)
      .filter(([_, entrega]) => entrega.id_asignacion === idAsignacion)
      .map(([id, entrega]) => ({ id, ...entrega }));

    // Obtener todos los alumnos para hacer match
    const snapshotAlumnos = await get(child(dbRef, 'alumnos'));
    const alumnos = snapshotAlumnos.exists() ? snapshotAlumnos.val() : {};

    // Agregar nombre del alumno a cada entrega
    const entregasConNombre = entregasFiltradas.map(entrega => {
      const alumno = alumnos[entrega.id_alumno];
      return {
        ...entrega,
        nombre_completo: alumno ? alumno.nombre_completo : "Desconocido"
      };
    });

    return entregasConNombre;
  } catch (error) {
    console.error("Error al obtener entregas con nombre de alumno:", error);
    throw error;
  }
}

export async function obtenerAlumnosPorMateria(idMateria) {
  const dbRef = ref(db);

  try {
    // Obtener todas las relaciones materia-alumno
    const relacionesSnap = await get(child(dbRef, 'materias_alumnos'));
    if (!relacionesSnap.exists()) return [];

    const relaciones = relacionesSnap.val();

    // Filtrar solo los alumnos de la materia dada
    const idsAlumnos = Object.values(relaciones)
      .filter(rel => rel.id_materia === idMateria)
      .map(rel => rel.id_alumno);

    const alumnosSnap = await get(child(dbRef, 'alumnos'));
    if (!alumnosSnap.exists()) return [];

    const alumnos = alumnosSnap.val();

    // Devolver datos completos de los alumnos de la materia
    const alumnosMateria = idsAlumnos.map(id => ({ id, ...alumnos[id] })).filter(a => a);
    return alumnosMateria;

  } catch (error) {
    console.error("Error al obtener alumnos por materia:", error);
    throw error;
  }
}

export async function entregarAsignacion(entrega) {
  const dbRef = ref(db);

  try {
    // Obtener todas las entregas
    const snapshot = await get(child(dbRef, 'asignaciones_entregas'));
    const entregas = snapshot.exists() ? snapshot.val() : {};

    // Buscar si ya existe una entrega del mismo alumno para la misma asignación
    let entregaExistenteId = null;

    for (const [id, e] of Object.entries(entregas)) {
      if (e.id_asignacion === entrega.id_asignacion && e.id_alumno === entrega.id_alumno) {
        entregaExistenteId = id;
        break;
      }
    }

    // Datos de la entrega
    const datosEntrega = {
      id_asignacion: entrega.id_asignacion,
      id_alumno: entrega.id_alumno,
      archivo_adjunto: entrega.archivo_adjunto,
      fecha_entrega: entrega.fecha_entrega,
      estado: "entregado",
      calificacion: null
    };

    if (entregaExistenteId) {
      // Actualizar entrega existente
      await set(ref(db, `asignaciones_entregas/${entregaExistenteId}`), datosEntrega);
      console.log("Entrega actualizada correctamente");
    } else {
      // Crear nueva entrega
      const nuevaEntregaRef = push(ref(db, 'asignaciones_entregas'));
      await set(nuevaEntregaRef, datosEntrega);
      console.log("Entrega registrada correctamente");
    }

  } catch (error) {
    console.error("Error al registrar o actualizar la entrega:", error);
    throw error;
  }
}

export async function calificarAsignacion(idEntrega, calificacion) {
  const entregaRef = ref(db, `asignaciones_entregas/${idEntrega}`);

  try {
    await update(entregaRef, {
      calificacion: calificacion
    });
    console.log("Calificación asignada correctamente");
  } catch (error) {
    console.error("Error al calificar la entrega:", error);
    throw error;
  }
}

export async function editarAsignacion(asignacion) {
  const dbRef = ref(getDatabase());
  try {
    await set(child(dbRef, `asignaciones/${asignacion.id}`), {
      titulo: asignacion.titulo,
      descripcion: asignacion.descripcion,
      fecha_limite: asignacion.fecha_limite,
      id_materia: asignacion.id_materia
    });
    console.log("Asignación editada correctamente");
  } catch (error) {
    console.error("Error al editar la asignación:", error);
    throw error;
  }
}

export async function obtenerCalificacionesPorMaestro(idMaestro) {
  const dbRef = ref(getDatabase());

  try {
    // 1. Obtener materias del maestro
    const materiasSnap = await get(child(dbRef, 'materias'));
    const materias = [];
    materiasSnap.forEach(snap => {
      const materia = snap.val();
      if (materia.id_maestro === idMaestro) {
        materia.id = snap.key;
        materias.push(materia);
      }
    });

    const resultados = [];

    // 2. Recorrer materias
    for (const materia of materias) {
      // 3. Obtener asignaciones por materia
      const asignacionesSnap = await get(child(dbRef, 'asignaciones'));
      const asignaciones = [];
      asignacionesSnap.forEach(snap => {
        const asignacion = snap.val();
        if (asignacion.id_materia === materia.id) {
          asignacion.id = snap.key;
          asignaciones.push(asignacion);
        }
      });

      // 4. Recorrer asignaciones
      for (const asignacion of asignaciones) {
        const entregasSnap = await get(child(dbRef, 'asignaciones_entregas'));

        entregasSnap.forEach(entregaSnap => {
          const entrega = entregaSnap.val();
          if (entrega.id_asignacion === asignacion.id) {
            const idAlumno = entrega.id_alumno;
            const calificacion = entrega.calificacion || null;

            // 5. Obtener CURP del alumno
            resultados.push(
              get(child(dbRef, `alumnos/${idAlumno}`)).then(usuarioSnap => {
                const usuario = usuarioSnap.val();
                return {
                  idMaestro: idMaestro,
                  curp: usuario.curp,
                  materia: materia.nombre,
                  asignacion: asignacion.titulo,
                  calificacion: calificacion
                };
              })
            );
          }
        });
      }
    }

    // 6. Esperar todas las consultas de CURP
    return await Promise.all(resultados);

  } catch (error) {
    console.error("Error al obtener calificaciones:", error);
    return [];
  }
}

/**
 * Cierra la sesión del usuario autenticado y limpia el localStorage.
 * @returns {Promise<void>}
 */
export async function cerrarSesionConFirebase() {
  try {
    await signOut(auth);
    localStorage.clear();
    console.log("Sesión cerrada correctamente");
  } catch (error) {
    console.error("Error al cerrar sesión:", error);
    throw new Error("No se pudo cerrar la sesión.");
  }
}

/**
 * Inicia sesión con correo y contraseña, y obtiene el ID del usuario desde la base de datos.
 * @param {string} correo - Correo del usuario.
 * @param {string} contrasena - Contraseña del usuario.
 * @param {string} rol - "maestro" o "alumno".
 * @returns {Promise<{ id: string, correo: string, rol: string }>} - Info del usuario logueado.
 */
export async function loginConFirebase(correo, contrasena, rol) {
  try {
    // 1. Login con autenticación
    await signInWithEmailAndPassword(auth, correo, contrasena);

    // 2. Buscar ID en la colección correspondiente
    const ruta = rol === "maestro" ? "maestros" : "alumnos";
    const snapshot = await get(ref(db, ruta));

    if (!snapshot.exists()) {
      throw new Error("No se encontraron datos en Firebase.");
    }

    const datos = snapshot.val();
    for (const id in datos) {
      if (datos[id].correo === correo) {
        return { id, correo, rol };
      }
    }

    throw new Error("Correo no registrado en la base de datos.");
  } catch (error) {
    throw new Error(`Error al iniciar sesión: ${error.message}`);
  }
}

export async function cargarDatos() {
  const dbRef = ref(getDatabase());
  try {
    const snapshot = await get(child(dbRef, "maestros"));
    if (snapshot.exists()) {
      const datosObj = snapshot.val();
      const datos = Object.entries(datosObj).map(([id, data]) => ({ id, ...data }));
      console.log("Datos leídos correctamente:", datos);
      return datos;
    } else {
      console.log("No hay datos en 'maestros'");
      return [];
    }
  } catch (error) {
    console.error("Error al cargar datos:", error);
    throw error;
  }
}

export async function cargarMateriasPorMaestro(idMaestro) {
  try {
    const dbRef = ref(getDatabase());
    const snapshot = await get(child(dbRef, "materias"));

    if (snapshot.exists()) {
      const materias = snapshot.val();
      const materiasDelMaestro = Object.entries(materias)
        .filter(([_, materia]) => (materia.id_maestro || "") === idMaestro)
        .map(([id, materia]) => ({ id, ...materia }));

      return materiasDelMaestro;
    } else {
      console.warn("No hay materias registradas");
      return [];
    }
  } catch (error) {
    console.error("Error al cargar materias por maestro:", error);
    return [];
  }
}

export async function cargarAsignacionesPorMateria(idMateria) {
  const dbRef = ref(getDatabase());
  const snapshot = await get(child(dbRef, "asignaciones"));

  if (snapshot.exists()) {
    const data = snapshot.val();
    return Object.entries(data)
      .filter(([_, asignacion]) => asignacion.id_materia === idMateria)
      .map(([id, asignacion]) => ({ id, ...asignacion }));
  }

  return [];
}

// Este método es para guardar una nueva asignación (si no lo tienes)
export async function crearAsignacion(asignacion) {
  const nuevaRef = push(ref(db, "asignaciones"));
  await set(nuevaRef, asignacion);
}

//Obtiene todas las calificaciones de todas las materias de un maestro
export async function cargarMateriasPorAlumno(idAlumno) {
  try {
    // 1. Obtener relaciones materias_alumnos
    const relSnap = await get(ref(db, "materias_alumnos"));
    if (!relSnap.exists()) return [];

    const relaciones = relSnap.val();
    const idMaterias = [];

    for (const key in relaciones) {
      if (relaciones[key].id_alumno === idAlumno) {
        idMaterias.push(relaciones[key].id_materia);
      }
    }

    // 2. Obtener materias
    const materiasSnap = await get(ref(db, "materias"));
    if (!materiasSnap.exists()) return [];

    const materias = materiasSnap.val();
    const materiasDelAlumno = [];

    for (const key in materias) {
      if (idMaterias.includes(key)) {
        materiasDelAlumno.push({ id: key, ...materias[key] });
      }
    }

    return materiasDelAlumno;
  } catch (error) {
    console.error("Error al cargar materias del estudiante:", error);
    return [];
  }
}

export async function agregarDatosEjemplo() {
  try {
    // 1. Maestro
    const maestroRef = push(ref(db, "maestros"));
    const maestroId = maestroRef.key;
    await set(maestroRef, {
      nombre_completo: "Carlos Devoción",
      curp: "DEVO800101MDF",
      rfc: "DEVO800101MDF",
      correo: "carlos@ejemplo.com",
      telefono: "66244412233",
      direccion: "Av. Venustiano 123"
    });

    // 2. Materia asignada al maestro
    const materiaRef = push(ref(db, "materias"));
    const materiaId = materiaRef.key;
    await set(materiaRef, {
      nombre: "Seguridad Informática",
      id_maestro: maestroId
    });

    // 3. Tutor
    const tutorRef = push(ref(db, "tutores"));
    const tutorId = tutorRef.key;
    await set(tutorRef, {
      nombre_completo: "Mano Buenafuentes",
      direccion: "Calle Rosas 789",
      parentesco: "Padre",
      telefono: "6621234789"
    });

    // 4. Alumno con tutor
    const alumnoRef = push(ref(db, "alumnos"));
    const alumnoId = alumnoRef.key;
    await set(alumnoRef, {
      nombre_completo: "Emilio Mejia",
      curp: "EMRS020201WSONRS09",
      fecha_nacimiento: "2001-11-19",
      direccion: "Col. Medio #456",
      genero: "Masculino",
      telefono: "6628884477",
      correo: "emilio@gmail.com",
      id_tutor: tutorId
    });

    // 5. Relación alumno-materia
    const materiaAlumnoRef = push(ref(db, "materias_alumnos"));
    await set(materiaAlumnoRef, {
      id_alumno: alumnoId,
      id_materia: materiaId
    });

    // 6. Asignación en la materia
    const asignacionRef = push(ref(db, "asignaciones"));
    const asignacionId = asignacionRef.key;
    await set(asignacionRef, {
      titulo: "Proyecto web",
      descripcion: "Crear una página web",
      fecha_limite: "2025-06-03",
      id_materia: materiaId
    });

    // 7. Entrega de la asignación por el alumno
    const entregaRef = push(ref(db, "asignaciones_entregas"));
    await set(entregaRef, {
      id_asignacion: asignacionId,
      id_alumno: alumnoId,
      archivo_adjunto: "link-a-tu-archivo.html",
      fecha_entrega: "2025-06-01",
      estado: "entregado",
      calificacion: 6.8
    });

    console.log("Todos los datos de ejemplo se agregaron correctamente");
  } catch (error) {
    console.error("Error al agregar datos de ejemplo:", error);
  }
}

export async function agregarMaestro(datos) {
  try {
    const nuevoRef = ref(db, "maestros");
    await push(nuevoRef, datos);
    console.log("Maestro agregado correctamente");
  } catch (error) {
    console.error("Error en agregarMaestro:", error);
    throw error;
  }
}

