package com.example.control_escolar_api.Entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String curp_alumno;
    private String materia;
    private String nombre_asignacion;
    private String calificacion;
    private String id_maestro;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurp_alumno() {
        return curp_alumno;
    }

    public void setCurp_alumno(String curp_alumno) {
        this.curp_alumno = curp_alumno;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getNombre_asignacion() {
        return nombre_asignacion;
    }

    public void setNombre_asignacion(String nombre_asignacion) {
        this.nombre_asignacion = nombre_asignacion;
    }

    public String getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(String calificacion) {
        this.calificacion = calificacion;
    }

    public String getId_maestro() {
        return id_maestro;
    }

    public void setId_maestro(String id_maestro) {
        this.id_maestro = id_maestro;
    }
}