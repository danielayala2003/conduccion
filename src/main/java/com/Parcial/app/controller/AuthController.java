package com.Parcial.app.controller;

import com.Parcial.app.model.Usuario;
import com.Parcial.app.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.UUID;

@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // ==================== INIT ====================
    @PostConstruct
    public void init() {
        crearAdminPorDefecto();
    }

    private void crearAdminPorDefecto() {
        List<Usuario> admins = usuarioRepository.findAllByEmail("admin@escuela.com");

        // Eliminar todos los admins con ese correo si existen
        if (!admins.isEmpty()) {
            usuarioRepository.deleteAll(admins);
            System.out.println("Se eliminaron administradores duplicados.");
        }

        // Crear nuevo admin limpio
        Usuario admin = new Usuario();
        String password = "Admin123@"; // Fijo para pruebas

        admin.setNombre("Administrador Principal");
        admin.setEmail("admin@escuela.com");
        admin.setPassword(password);
        admin.setRol("ADMIN");

        usuarioRepository.save(admin);

        System.out.println("\n=== USUARIO ADMIN CREADO ===");
        System.out.println("Email: admin@escuela.com");
        System.out.println("Password: " + password);
        System.out.println("============================\n");
    }

    // ==================== AUTH METHODS ====================
    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("usuario") != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            return redirigirPorRol(usuario.getRol());
        }
        return "index";
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam String rol, Model model) {
        model.addAttribute("rol", rol);
        return "auth";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String rol,
            HttpSession session,
            Model model) {

        System.out.println("Intento de login - Email: " + email + " | Rol solicitado: " + rol);

        Usuario usuario = usuarioRepository.findByEmail(email.trim());

        if (usuario == null) {
            model.addAttribute("error", "Usuario no encontrado");
            model.addAttribute("rol", rol);
            return "auth";
        }

        if (!usuario.getPassword().equals(password.trim())) {
            model.addAttribute("error", "Contraseña incorrecta");
            model.addAttribute("rol", rol);
            return "auth";
        }

        if (!usuario.getRol().equalsIgnoreCase(rol.trim())) {
            model.addAttribute("error", "No tienes permisos para acceder como " + rol);
            model.addAttribute("rol", rol);
            return "auth";
        }

        session.setAttribute("usuario", usuario);
        return redirigirPorRol(usuario.getRol());
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // ==================== UTILITY METHODS ====================
    private String generarPasswordSeguro() {
        return "Admin_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String redirigirPorRol(String rol) {
        if (rol == null || rol.isEmpty()) {
            return "redirect:/";
        }
        return "redirect:/" + rol.toLowerCase() + "/dashboard";
    }

    // ==================== DEBUG ENDPOINT ====================
    @GetMapping("/debug/admin")
    @ResponseBody
    public String debugAdmin() {
        Usuario admin = usuarioRepository.findByEmail("admin@escuela.com");
        if (admin == null) {
            return "No existe usuario admin en la base de datos";
        }
        return String.format(
                "ADMIN ENCONTRADO:<br>" +
                        "Email: %s<br>" +
                        "Password: %s<br>" +
                        "Rol: %s<br>" +
                        "Nombre: %s",
                admin.getEmail(),
                admin.getPassword(),
                admin.getRol(),
                admin.getNombre()
        );
    }
}
