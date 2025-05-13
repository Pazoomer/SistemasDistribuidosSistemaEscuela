// firebase.js
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.6.0/firebase-app.js";

import {
  getDatabase,
  ref,
  set,
  get,
  child,
  push
} from "https://www.gstatic.com/firebasejs/11.6.0/firebase-database.js";

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

console.log("Firebase inicializado correctamente");

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
      // Filtra las materias por el id del maestro
      const materiasDelMaestro = Object.entries(materias)
        .filter(([_, materia]) => materia.idMaestro === idMaestro)
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

export async function cargarMateriasPorAlumno(idAlumno) {
  const db = getDatabase();

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
  const db = getDatabase();
  try {
    const nuevoRef = ref(db, "maestros");
    await push(nuevoRef, datos);
    console.log("Maestro agregado correctamente");
  } catch (error) {
    console.error("Error en agregarMaestro:", error);
    throw error;
  }
}

