const express = require('express');
const cors = require('cors');
const mysql = require('mysql2');
const { parse } = require('url');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const dbUrl = process.env.DATABASE_URL;
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

app.listen(PORT, () => {
  console.log(`Servidor escuchando en http://localhost:${PORT}`);
});