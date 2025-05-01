export async function cargarDatos() {
    const res = await fetch('https://sistemasdistribuidossistemaescuela.onrender.com/maestros');
    const data = await res.json();
    return data;
  }
  
  export async function agregarMaestro(datos) {
    const res = await fetch('https://sistemasdistribuidossistemaescuela.onrender.com/maestros', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(datos)
    });
    if (!res.ok) throw new Error('Error al agregar maestro');
  }
  