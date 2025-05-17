package com.example.control_escolar_api.Servicios;

import com.example.control_escolar_api.Entidades.Maestro;
import com.example.control_escolar_api.Repositorios.MaestroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaestroService {

    private final MaestroRepository repo;

    @Autowired
    public MaestroService(MaestroRepository repo) {
        this.repo = repo;
    }

    // Obtener todos los maestros
    public List<Maestro> obtenerTodos() {
        return repo.findAll();
    }

    // Obtener maestros que no han entregado calificaciones (simulado de momento)
    public List<Maestro> obtenerSinCalificacion() {
        return repo.findAll();
    }

    // Guardar un nuevo maestro
    public Maestro guardar(Maestro maestro) {
        return repo.save(maestro);
    }

    // Obtener maestro por ID
    public Optional<Maestro> obtenerPorId(Long id) {
        return repo.findById(id);
    }

    // Eliminar maestro por ID
    public void eliminarPorId(Long id) {
        repo.deleteById(id);
    }
}
