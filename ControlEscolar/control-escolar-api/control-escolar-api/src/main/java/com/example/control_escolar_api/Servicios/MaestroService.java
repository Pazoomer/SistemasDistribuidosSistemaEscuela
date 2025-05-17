package com.example.control_escolar_api.Servicios;

import com.example.control_escolar_api.Entidades.Maestro;
import com.example.control_escolar_api.Repositorios.MaestroRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MaestroService {
    private MaestroRepository repo;

    public MaestroService(MaestroRepository repo) {
        this.repo = repo;
    }

    public List<Maestro> obtenerTodos() {
        return repo.findAll();
    }

    public List<Maestro> obtenerSinCalificacion() {
        return null;
    }
}