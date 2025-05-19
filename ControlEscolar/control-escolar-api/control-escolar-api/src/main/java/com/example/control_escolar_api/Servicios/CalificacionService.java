package com.example.control_escolar_api.Servicios;

import com.example.control_escolar_api.Entidades.Calificaciones;
import com.example.control_escolar_api.Repositorios.CalificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CalificacionService {

    private final CalificacionRepository repo;

    @Autowired
    public CalificacionService(CalificacionRepository repo) {
        this.repo = repo;
    }

    // Obtener todos las calificaciones
    public List<Calificaciones> obtenerTodos() {
        return repo.findAll();
    }

    // Guardar un nueva calificacion
    public Calificaciones guardar(Calificaciones Calificacion) {
        return repo.save(Calificacion);
    }

    // Obtener calificacion por ID
    public Optional<Calificaciones> obtenerPorId(Long id) {
        return repo.findById(id);
    }

    // Eliminar calificacion por ID
    public void eliminarPorId(Long id) {
        repo.deleteById(id);
    }
}
