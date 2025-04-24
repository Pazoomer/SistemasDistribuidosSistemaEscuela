USE escuela;
-- Maestros
INSERT INTO maestros (nombre_completo, curp, rfc, correo, telefono, direccion) VALUES
('Juan Pérez González', 'PEGJ800101HSONRL09', 'PEGJ800101AAA', 'juan.perez@escuela.mx', '6621234567', 'Calle Reforma 123, Hermosillo'),
('Laura Medina Torres', 'METL850202MSONRL05', 'METL850202BBB', 'laura.medina@escuela.mx', '6621112233', 'Blvd. Kino 456, Hermosillo'),
('Carlos Jiménez Díaz', 'JIDC900303HSONRL07', 'JIDC900303CCC', 'carlos.jimenez@escuela.mx', '6629988776', 'Col. San Benito, Hermosillo');

-- Materias
INSERT INTO materias (nombre, id_maestro) VALUES
('Matemáticas', 1),
('Historia', 2),
('Ciencias', 3);

-- Tutores
INSERT INTO tutores (nombre_completo, direccion, parentesco, telefono) VALUES
('María López', 'Col. Centro, Hermosillo', 'Madre', '6627654321'),
('José Ramírez', 'Col. Pitic, Hermosillo', 'Padre', '6624433221'),
('Ana Torres', 'Col. Sahuaro, Hermosillo', 'Tía', '6628877665');

-- Alumnos
INSERT INTO alumnos (nombre_completo, curp, fecha_nacimiento, direccion, genero, telefono, id_tutor) VALUES
('Carlos Martínez', 'MACC050505HSONCR01', '2005-05-05', 'Av. Universidad 456, Hermosillo', 'Masculino', '6622345678', 1),
('Lucía González', 'GOLU060606MSONCR02', '2006-06-06', 'Av. Veracruz 789, Hermosillo', 'Femenino', '6622233445', 2),
('Luis Herrera', 'HELU070707HSONCR03', '2007-07-07', 'Av. Juárez 321, Hermosillo', 'Masculino', '6623344556', 3);

-- Materias-Alumnos (relación)
INSERT INTO materias_alumnos (id_alumno, id_materia) VALUES
(1, 1),
(2, 2),
(3, 3);

-- Asignaciones
INSERT INTO asignaciones (titulo, descripcion, fecha_limite, id_materia) VALUES
('Tarea 1', 'Ejercicios del 1 al 10', '2025-04-30', 1),
('Ensayo 1', 'Ensayo sobre la Revolución Mexicana', '2025-04-25', 2),
('Experimento 1', 'Crear un volcán casero', '2025-04-28', 3);

-- Entregas
INSERT INTO asignaciones_entregas (id_asignacion, id_alumno, archivo_adjunto, fecha_entrega, estado, calificacion) VALUES
(1, 1, 'tarea1_carlos.pdf', '2025-04-28', 'entregado', 9.5),
(2, 2, 'ensayo1_lucia.docx', '2025-04-24', 'entregado', 10.0),
(3, 3, 'experimento1_luis.mp4', '2025-04-27', 'entregado', 8.7);
