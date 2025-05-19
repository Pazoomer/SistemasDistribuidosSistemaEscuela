package com.example.control_escolar_api.Repositorios;

import com.example.control_escolar_api.Entidades.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
}
