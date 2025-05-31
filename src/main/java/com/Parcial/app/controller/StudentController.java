package com.Parcial.app.controller;

import com.Parcial.app.model.*;
import com.Parcial.app.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estudiante")
public class StudentController {

    private final CursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final HorarioRepository horarioRepository;
    private final PagoRepository pagoRepository;

    public StudentController(CursoRepository cursoRepository,
                             InscripcionRepository inscripcionRepository,
                             UsuarioRepository usuarioRepository,
                             HorarioRepository horarioRepository,
                             PagoRepository pagoRepository) {
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.horarioRepository = horarioRepository;
        this.pagoRepository = pagoRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario estudiante = (Usuario) session.getAttribute("usuario");

        if (estudiante == null || !estudiante.getRol().equalsIgnoreCase("ESTUDIANTE")) {
            return "redirect:/login?rol=estudiante";
        }

        List<Inscripcion> inscripciones = inscripcionRepository.findByEstudianteId(estudiante.getId());
        List<Curso> cursosInscritos = inscripciones.stream()
            .map(inscripcion -> cursoRepository.findById(inscripcion.getCursoId()).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        model.addAttribute("estudiante", estudiante);
        model.addAttribute("cursosInscritos", cursosInscritos);
        return "estudiante";
    }

    @GetMapping("/inscripcion")
    public String mostrarInscripcion(HttpSession session, Model model) {
        Usuario estudiante = (Usuario) session.getAttribute("usuario");

        if (estudiante == null || !estudiante.getRol().equalsIgnoreCase("ESTUDIANTE")) {
            return "redirect:/login?rol=estudiante";
        }

        List<Curso> cursosDisponibles = cursoRepository.findAll();

        List<String> cursosInscritosIds = inscripcionRepository.findByEstudianteId(estudiante.getId())
            .stream()
            .map(Inscripcion::getCursoId)
            .collect(Collectors.toList());

        List<Map<String, Object>> cursosConInfo = cursosDisponibles.stream().map(curso -> {
            Map<String, Object> cursoInfo = new HashMap<>();
            cursoInfo.put("curso", curso);
            cursoInfo.put("yaInscrito", cursosInscritosIds.contains(curso.getId()));

            Usuario instructor = usuarioRepository.findById(curso.getInstructorId()).orElse(null);
            cursoInfo.put("instructorNombre", instructor != null ? instructor.getNombre() : "No asignado");

            List<Horario> horarios = horarioRepository.findByCursoId(curso.getId());
            cursoInfo.put("horarios", horarios);

            return cursoInfo;
        }).collect(Collectors.toList());

        model.addAttribute("cursos", cursosConInfo);
        model.addAttribute("estudiante", estudiante);
        return "inscripcion";
    }

    @PostMapping("/inscribir")
    public String inscribirEnCurso(@RequestParam String cursoId,
                                   HttpSession session,
                                   RedirectAttributes redirect) {
        Usuario estudiante = (Usuario) session.getAttribute("usuario");

        if (estudiante == null || !estudiante.getRol().equalsIgnoreCase("ESTUDIANTE")) {
            return "redirect:/login?rol=estudiante";
        }

        boolean yaInscrito = inscripcionRepository.existsByEstudianteIdAndCursoId(estudiante.getId(), cursoId);

        if (yaInscrito) {
            redirect.addFlashAttribute("error", "Ya estás inscrito en este curso.");
            return "redirect:/estudiante/inscripcion";
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEstudianteId(estudiante.getId());
        inscripcion.setCursoId(cursoId);

        try {
            inscripcionRepository.save(inscripcion);
            redirect.addFlashAttribute("success", "Inscripción exitosa.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al inscribirse: " + e.getMessage());
        }

        return "redirect:/estudiante/inscripcion";
    }

    @PostMapping("/desinscribir")
    public String desinscribirDeCurso(@RequestParam String cursoId,
                                      HttpSession session,
                                      RedirectAttributes redirect) {
        Usuario estudiante = (Usuario) session.getAttribute("usuario");

        if (estudiante == null || !estudiante.getRol().equalsIgnoreCase("ESTUDIANTE")) {
            return "redirect:/login?rol=estudiante";
        }

        List<Inscripcion> inscripciones = inscripcionRepository.findByEstudianteId(estudiante.getId());
        Inscripcion inscripcion = inscripciones.stream()
            .filter(i -> i.getCursoId().equals(cursoId))
            .findFirst()
            .orElse(null);

        if (inscripcion != null) {
            try {
                inscripcionRepository.delete(inscripcion);
                redirect.addFlashAttribute("success", "Te has desinscrito del curso.");
            } catch (Exception e) {
                redirect.addFlashAttribute("error", "Error al desinscribirse: " + e.getMessage());
            }
        } else {
            redirect.addFlashAttribute("error", "No estabas inscrito en este curso.");
        }

        return "redirect:/estudiante/inscripcion";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?rol=estudiante";
    }

    @GetMapping("/pagos")
    public String mostrarPagos(HttpSession session, Model model) {
        Usuario estudiante = (Usuario) session.getAttribute("usuario");

        if (estudiante == null || !estudiante.getRol().equalsIgnoreCase("ESTUDIANTE")) {
            return "redirect:/login?rol=estudiante";
        }

        List<Pago> pagos = pagoRepository.findByEstudianteId(estudiante.getId());
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("pagos", pagos);
        return "pagos";
    }

    @GetMapping("/cal")
    public String mostrarCalendario(HttpSession session, Model model) {
        Usuario estudiante = (Usuario) session.getAttribute("usuario");

        if (estudiante == null || !estudiante.getRol().equalsIgnoreCase("ESTUDIANTE")) {
            return "redirect:/login?rol=estudiante";
        }

        List<Inscripcion> inscripciones = inscripcionRepository.findByEstudianteId(estudiante.getId());
        List<Map<String, Object>> eventos = new ArrayList<>();

        for (Inscripcion inscripcion : inscripciones) {
            Curso curso = cursoRepository.findById(inscripcion.getCursoId()).orElse(null);
            if (curso != null) {
                List<Horario> horarios = horarioRepository.findByCursoId(curso.getId());
                for (Horario horario : horarios) {
                    Map<String, Object> evento = new HashMap<>();
                    evento.put("titulo", curso.getNombre());
                    evento.put("dia", horario.getDiaSemana());
                    evento.put("horaInicio", horario.getHoraInicio().toString());
                    evento.put("horaFin", horario.getHoraFin().toString());
                    evento.put("instructor", usuarioRepository.findById(curso.getInstructorId())
                        .map(Usuario::getNombre).orElse("Instructor no asignado"));
                    eventos.add(evento);
                }
            }
        }

        Map<String, List<Map<String, Object>>> eventosPorDia = eventos.stream()
            .collect(Collectors.groupingBy(e -> (String) e.get("dia")));

        model.addAttribute("estudiante", estudiante);
        model.addAttribute("eventosPorDia", eventosPorDia);
        model.addAttribute("diasSemana", Arrays.asList(
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"));

        return "cal";
    }
}
