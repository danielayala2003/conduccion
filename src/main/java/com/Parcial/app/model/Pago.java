package com.Parcial.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Document(collection = "pagos")
public class Pago {
    @Id
    private String id;
    private String estudianteId;
    private double monto;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;
    
    private String registradoPor;

    // Constructores
    public Pago() {}

    public Pago(String id, String estudianteId, double monto, LocalDate fechaPago, String registradoPor) {
        this.id = id;
        this.estudianteId = estudianteId;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.registradoPor = registradoPor;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(String estudianteId) {
        this.estudianteId = estudianteId;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }
}