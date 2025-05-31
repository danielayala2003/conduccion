package com.Parcial.app.controller;

import com.Parcial.app.model.*;
import com.Parcial.app.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/instructor")
public class InstructorController {

    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final MaterialRepository materialRepository;
    private final ClaseRepository claseRepository;
    private final InscripcionRepository inscripcionRepository;

    public InstructorController(CursoRepository cursoRepository,
                                UsuarioRepository usuarioRepository,
                                AsistenciaRepository asistenciaRepository,
                                EvaluacionRepository evaluacionRepository,
                                MaterialRepository materialRepository,
                                ClaseRepository claseRepository,
                                InscripcionRepository inscripcionRepository) {
        this.cursoRepository = cursoRepository;
        this.usuarioRepository = usuarioRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.materialRepository = materialRepository;
        this.claseRepository = claseRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario instructor = getInstructorFromSession(session);
        if (instructor == null) return redirectToLogin();

        List<Curso> cursos = cursoRepository.findByInstructorId(instructor.getId());
        model.addAttribute("cursos", cursos);
        model.addAttribute("instructor", instructor);
        return "instructor"; // Ahora apunta a instructor.html
    }

    @GetMapping("/estudiantes")
    public String listarEstudiantes(HttpSession session, Model model) {
        Usuario instructor = getInstructorFromSession(session);
        if (instructor == null) return redirectToLogin();

        List<Curso> cursos = cursoRepository.findByInstructorId(instructor.getId());
        List<String> cursosIds = cursos.stream().map(Curso::getId).collect(Collectors.toList());

        List<Inscripcion> inscripciones = inscripcionRepository.findByCursoIdIn(cursosIds);
        List<String> estudiantesIds = inscripciones.stream()
                .map(Inscripcion::getEstudianteId)
                .distinct()
                .collect(Collectors.toList());

        List<Usuario> estudiantes = usuarioRepository.findByIdInAndRol(estudiantesIds, "ESTUDIANTE");

        Map<String, String> nombresCursos = cursos.stream()
                .collect(Collectors.toMap(Curso::getId, Curso::getNombre));

        model.addAttribute("cursos", cursos);
        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("nombresCursos", nombresCursos);
        return "estudiantes"; // estudiantes.html
    }

    @GetMapping("/asistencia")
    public String mostrarAsistenciaForm(HttpSession session, Model model) {
        Usuario instructor = getInstructorFromSession(session);
        if (instructor == null) return redirectToLogin();

        List<Clase> clases = claseRepository.findByInstructorId(instructor.getId());
        List<Curso> cursos = cursoRepository.findByInstructorId(instructor.getId());

        List<Inscripcion> inscripciones = inscripcionRepository.findByCursoIdIn(
                cursos.stream().map(Curso::getId).collect(Collectors.toList())
        );
        List<Usuario> estudiantes = usuarioRepository.findByIdIn(
                inscripciones.stream().map(Inscripcion::getEstudianteId).collect(Collectors.toList())
        );

        model.addAttribute("clases", clases);
        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("asistencia", new Asistencia());
        return "asistencia"; // asistencia.html
    }

    @PostMapping("/registrar-asistencia")
    public String registrarAsistencia(@ModelAttribute Asistencia asistencia,
                                      HttpSession session) {
        Usuario instructor = getInstructorFromSession(session);
        if (instructor == null) return redirectToLogin();

        asistencia.setFechaRegistro(LocalDate.now());
        asistencia.setInstructorId(instructor.getId());
        asistenciaRepository.save(asistencia);
        return "redirect:/instructor/asistencia?success=true";
    }

    @GetMapping("/evaluaciones")
    public String mostrarEvaluaciones(HttpSession session, Model model) {
        Usuario instructor = getInstructorFromSession(session);
        if (instructor == null) return redirectToLogin();

        List<Evaluacion> evaluaciones = evaluacionRepository.findByInstructorId(instructor.getId());

        List<Curso> cursos = cursoRepository.findByInstructorId(instructor.getId());
        List<Inscripcion> inscripciones = inscripcionRepository.findByCursoIdIn(
                cursos.stream().map(Curso::getId).collect(Collectors.toList())
        );
        List<Usuario> estudiantes = usuarioRepository.findByIdIn(
                inscripciones.stream().map(Inscripcion::getEstudianteId).collect(Collectors.toList())
        );

        model.addAttribute("evaluaciones", evaluaciones);
        model.addAttribute("nuevaEvaluacion", new Evaluacion());
        model.addAttribute("estudiantes", estudiantes);
        return "evaluaciones"; // evaluaciones.html
    }

    @PostMapping("/registrar-evaluacion")
    public String registrarEvaluacion(@ModelAttribute Evaluacion evaluacion,
                                      HttpSession session) {
        Usuario instructor = getInstructorFromSession(session);
        if (instructor == null) return redirectToLogin();

        evaluacion.setFecha(LocalDate.now());
        evaluacion.setInstructorId(instructor.getId());
        evaluacionRepository.save(evaluacion);
        return "redirect:/instructor/evaluaciones?success=true";
    }

    @GetMapping("/material")
    public String mostrarMaterial(HttpSession session, Model model) {
        Usuario instructor = getInstructorFromSession(session);
        if (instructor == null) return redirectToLogin();

        List<Material> materiales = materialRepository.findByInstructorId(instructor.getId());
        model.addAttribute("materiales", materiales);
        return "material"; // material.html
    }

    @PostMapping("/subir-material")
    public String subirMaterial(@RequestParam("archivo") MultipartFile archivo,
                                @RequestParam String descripcion,
                                HttpSession session) throws IOException {
        Usuario instructor = getInstructorFromSession(session);
        if (instructor == null) return redirectToLogin();

        Material material = new Material();
        material.setNombre(archivo.getOriginalFilename());
        material.setTipo(archivo.getContentType());
        material.setDatos(archivo.getBytes());
        material.setDescripcion(descripcion);
        material.setFechaSubida(LocalDate.now());
        material.setInstructorId(instructor.getId());

        materialRepository.save(material);
        return "redirect:/instructor/material?success=true";
    }

    @GetMapping("/calendario")
    public String mostrarCalendario(HttpSession session, Model model) {
        Usuario instructor = getInstructorFromSession(session);
        if (instructor == null) return redirectToLogin();

        List<Clase> clases = claseRepository.findByInstructorId(instructor.getId());
        model.addAttribute("clases", clases);
        return "calendario"; // calendario.html
    }

    private Usuario getInstructorFromSession(HttpSession session) {
        Usuario instructor = (Usuario) session.getAttribute("usuario");
        if (instructor == null || !instructor.getRol().equalsIgnoreCase("INSTRUCTOR")) {
            return null;
        }
        return instructor;
    }

    private String redirectToLogin() {
        return "redirect:/login?rol=instructor";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/login?rol=instructor";
    }
}
