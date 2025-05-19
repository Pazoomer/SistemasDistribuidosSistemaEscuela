package com.example.control_escolar_api.Repositorios;

import com.example.control_escolar_api.Entidades.Calificaciones;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalificacionRepository extends JpaRepository<Calificaciones, Long> {
}
