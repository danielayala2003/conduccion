package com.Parcial.app.controller;

import com.Parcial.app.model.Pago;
import com.Parcial.app.model.Usuario;
import com.Parcial.app.repository.PagoRepository;
import com.Parcial.app.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/recepcionista")
public class RecepcionistaController {

    private final UsuarioRepository usuarioRepository;
    private final PagoRepository pagoRepository;

    public RecepcionistaController(UsuarioRepository usuarioRepository,
                                   PagoRepository pagoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pagoRepository = pagoRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario recepcionista = (Usuario) session.getAttribute("usuario");

        if (recepcionista == null || !recepcionista.getRol().equalsIgnoreCase("RECEPCIONISTA")) {
            return "redirect:/login?rol=recepcionista";
        }

        model.addAttribute("recepcionista", recepcionista);
        return "recepcionista";
    }

    @GetMapping("/registro-estudiante")
    public String mostrarRegistroEstudiantes(HttpSession session, Model model) {
        Usuario recepcionista = (Usuario) session.getAttribute("usuario");

        if (recepcionista == null || !recepcionista.getRol().equalsIgnoreCase("RECEPCIONISTA")) {
            return "redirect:/login?rol=recepcionista";
        }

        List<Usuario> estudiantes = usuarioRepository.findByRol("ESTUDIANTE");
        model.addAttribute("estudiantes", estudiantes);
        return "registro-estudiante";
    }

    @PostMapping("/registro-estudiante/guardar")
    public String guardarEstudiante(@ModelAttribute Usuario estudiante,
                                    RedirectAttributes redirect,
                                    HttpSession session) {
        Usuario recepcionista = (Usuario) session.getAttribute("usuario");

        if (recepcionista == null || !recepcionista.getRol().equalsIgnoreCase("RECEPCIONISTA")) {
            return "redirect:/login?rol=recepcionista";
        }

        if (estudiante.getEmail() == null || estudiante.getEmail().isEmpty()) {
            redirect.addFlashAttribute("error", "El email es obligatorio");
            return "redirect:/recepcionista/registro-estudiante";
        }

        Usuario usuarioExistente = usuarioRepository.findByEmail(estudiante.getEmail());
        if (usuarioExistente != null && (estudiante.getId() == null || !usuarioExistente.getId().equals(estudiante.getId()))) {
            redirect.addFlashAttribute("error", "El email ya está registrado");
            return "redirect:/recepcionista/registro-estudiante";
        }

        if ((estudiante.getId() == null || estudiante.getId().isEmpty()) &&
            (estudiante.getPassword() == null || estudiante.getPassword().isEmpty())) {
            redirect.addFlashAttribute("error", "La contraseña es obligatoria para nuevos estudiantes");
            return "redirect:/recepcionista/registro-estudiante";
        }

        try {
            if (estudiante.getId() == null || estudiante.getId().isEmpty()) {
                estudiante.setId(UUID.randomUUID().toString());
            }
            estudiante.setRol("ESTUDIANTE");

            usuarioRepository.save(estudiante);
            redirect.addFlashAttribute("success", "Estudiante guardado exitosamente");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al guardar estudiante: " + e.getMessage());
        }

        return "redirect:/recepcionista/registro-estudiante";
    }

    @GetMapping("/horarios")
    public String mostrarHorarios() {
        return "horarios";
    }

    @GetMapping("/gestion-pagos")
    public String gestionPagos(HttpSession session, Model model) {
        Usuario recepcionista = (Usuario) session.getAttribute("usuario");

        if (recepcionista == null || !recepcionista.getRol().equalsIgnoreCase("RECEPCIONISTA")) {
            return "redirect:/login?rol=recepcionista";
        }

        try {
            List<Usuario> estudiantes = usuarioRepository.findByRol("ESTUDIANTE");
            List<Pago> pagos = pagoRepository.findAll();

            Map<String, String> estudiantesMap = estudiantes.stream()
                    .collect(Collectors.toMap(
                            Usuario::getId,
                            u -> u.getNombre() + " (" + u.getEmail() + ")"
                    ));

            model.addAttribute("estudiantes", estudiantes);
            model.addAttribute("estudiantesMap", estudiantesMap);
            model.addAttribute("pagos", pagos != null ? pagos : Collections.emptyList());

        } catch (Exception e) {
            model.addAttribute("pagos", Collections.emptyList());
            model.addAttribute("error", "Error al cargar pagos: " + e.getMessage());
        }

        return "gestion-pagos";
    }

    @PostMapping("/registrar-pago")
    public String registrarPago(@RequestParam String estudianteId,
                                @RequestParam String monto,
                                @RequestParam String fechaPago,
                                HttpSession session,
                                RedirectAttributes redirect) {
        Usuario recepcionista = (Usuario) session.getAttribute("usuario");

        if (recepcionista == null || !recepcionista.getRol().equalsIgnoreCase("RECEPCIONISTA")) {
            return "redirect:/login?rol=recepcionista";
        }

        try {
            if (!usuarioRepository.existsById(estudianteId)) {
                redirect.addFlashAttribute("error", "El estudiante no existe");
                return "redirect:/recepcionista/gestion-pagos";
            }

            Pago pago = new Pago();
            pago.setId(UUID.randomUUID().toString());
            pago.setEstudianteId(estudianteId);
            pago.setMonto(Double.parseDouble(monto));
            pago.setFechaPago(LocalDate.parse(fechaPago));
            pago.setRegistradoPor(recepcionista.getId());

            pagoRepository.save(pago);
            redirect.addFlashAttribute("success", "Pago registrado exitosamente");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al registrar pago: " + e.getMessage());
        }

        return "redirect:/recepcionista/gestion-pagos";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?rol=recepcionista";
    }
}
