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

