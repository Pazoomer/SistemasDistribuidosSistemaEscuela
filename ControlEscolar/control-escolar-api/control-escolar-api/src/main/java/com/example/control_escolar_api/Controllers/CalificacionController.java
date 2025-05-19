package com.example.control_escolar_api.Controllers;

import com.example.control_escolar_api.Entidades.Calificacion;
import com.example.control_escolar_api.Servicios.CalificacionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/calificacion")
public class CalificacionController {
    private final CalificacionService service;

    public CalificacionController(CalificacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<Calificacion> getCalificaciones() {
        return service.obtenerTodos();
    }
}
