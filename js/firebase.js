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
    const nuevoMaestroRef = push(ref(db, "maestros"));
    await set(nuevoMaestroRef, {
      nombre_completo: "Carlos Devoción",
      curp: "DEVO800101MDF",
      rfc: "DEVO800101MDF",
      correo: "carlos@ejemplo.com",
      telefono: "66244412233",
      direccion: "Av. Venustio 123"
    });
    console.log("Datos de ejemplo agregados correctamente");
  } catch (error) {
    console.error("Error al agregar datos:", error);
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

