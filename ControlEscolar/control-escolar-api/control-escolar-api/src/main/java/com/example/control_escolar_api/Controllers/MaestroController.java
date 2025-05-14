package com.example.control_escolar_api.Controllers;

import com.example.control_escolar_api.Entidades.Maestro;
import com.example.control_escolar_api.Servicios.MaestroService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maestros")
public class MaestroController {
    private final MaestroService service;

    public MaestroController(MaestroService service) {
        this.service = service;
    }

    @GetMapping
    public List<Maestro> getMaestros() {
        return service.obtenerTodos();
    }

    public List<Maestro> getMaestrosSinCalifaciones(){
        return service.obtenerSinCalificacion();
    }
}
