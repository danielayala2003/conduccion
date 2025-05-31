package com.Parcial.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalTime;

@Document(collection = "horarios")
public class Horario {
    @Id
    private String id;
    private String cursoId;
    private String diaSemana; // Lunes, Martes, etc.
    private LocalTime horaInicio;
    private LocalTime horaFin;
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getCursoId() {
        return cursoId;
    }
    public void setCursoId(String cursoId) {
        this.cursoId = cursoId;
    }
    public String getDiaSemana() {
        return diaSemana;
    }
    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }
    public LocalTime getHoraInicio() {
        return horaInicio;
    }
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }
    public LocalTime getHoraFin() {
        return horaFin;
    }
    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
}