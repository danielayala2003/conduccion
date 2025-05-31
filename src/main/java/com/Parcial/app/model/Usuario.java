package com.Parcial.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String id;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    @Email(message = "Debe ser un email válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
    
    @NotBlank(message = "El rol es obligatorio")
    private String rol; // ADMIN, INSTRUCTOR, ESTUDIANTE, RECEPCIONISTA, SUPERVISOR

    // Getters y Setters (generados automáticamente o manualmente)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {  // <-- Este es el método que necesitas
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}