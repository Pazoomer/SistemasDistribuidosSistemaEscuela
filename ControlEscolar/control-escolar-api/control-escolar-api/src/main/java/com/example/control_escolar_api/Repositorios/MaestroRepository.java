package com.example.control_escolar_api.Repositorios;

import com.example.control_escolar_api.Entidades.Maestro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaestroRepository extends JpaRepository<Maestro, Long> {
}