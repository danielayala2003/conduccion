package com.Parcial.app.controller;

import com.Parcial.app.model.Curso;
import com.Parcial.app.model.Horario;
import com.Parcial.app.model.Usuario;
import com.Parcial.app.repository.CursoRepository;
import com.Parcial.app.repository.HorarioRepository;
import com.Parcial.app.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final HorarioRepository horarioRepository;

    public AdminController(UsuarioRepository usuarioRepository, 
                         CursoRepository cursoRepository,
                         HorarioRepository horarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.horarioRepository = horarioRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ADMIN")) {
            return "redirect:/";
        }
        model.addAttribute("usuario", usuario);
        return "admin";
    }

    @GetMapping("/usuarios")
    public String gestionUsuarios(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("roles", Arrays.asList("ADMIN", "INSTRUCTOR", "ESTUDIANTE", "RECEPCIONISTA", "SUPERVISOR"));

        return "usuarios";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario,
                               RedirectAttributes redirect,
                               HttpSession session) {
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        if (usuario.getId() == null || usuario.getId().isEmpty()) {
            usuario.setId(UUID.randomUUID().toString());
        }

        Usuario usuarioExistente = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioExistente != null && !usuarioExistente.getId().equals(usuario.getId())) {
            redirect.addFlashAttribute("error", "El email ya está registrado");
            return "redirect:/admin/usuarios";
        }

        if ((usuario.getId() == null || usuario.getId().isEmpty()) && 
            (usuario.getPassword() == null || usuario.getPassword().isEmpty())) {
            redirect.addFlashAttribute("error", "La contraseña es obligatoria para nuevos usuarios");
            return "redirect:/admin/usuarios";
        }

        try {
            usuarioRepository.save(usuario);
            redirect.addFlashAttribute("success", "Usuario guardado exitosamente");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al guardar usuario: " + e.getMessage());
        }

        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable String id,
                                RedirectAttributes redirect,
                                HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        try {
            usuarioRepository.deleteById(id);
            redirect.addFlashAttribute("success", "Usuario eliminado exitosamente");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
        }

        return "redirect:/admin/usuarios";
    }

    @GetMapping("/cursos")
    public String gestionCursos(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        List<Curso> cursos = cursoRepository.findAll();
        List<Usuario> instructores = usuarioRepository.findByRol("INSTRUCTOR");

        model.addAttribute("cursos", cursos);
        model.addAttribute("instructores", instructores);

        return "cursos";
    }

    @PostMapping("/cursos/guardar")
    public String guardarCurso(@RequestParam(required = false) String id,
                             @RequestParam String nombre,
                             @RequestParam String descripcion,
                             @RequestParam String instructorId,
                             HttpSession session,
                             RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        try {
            Curso curso;
            if (id != null && !id.isEmpty()) {
                curso = cursoRepository.findById(id).orElse(new Curso());
            } else {
                curso = new Curso();
                curso.setId(UUID.randomUUID().toString());
            }

            curso.setNombre(nombre);
            curso.setDescripcion(descripcion);
            curso.setInstructorId(instructorId);

            cursoRepository.save(curso);
            redirect.addFlashAttribute("success", "Curso guardado exitosamente");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al guardar curso: " + e.getMessage());
        }

        return "redirect:/admin/cursos";
    }

    @GetMapping("/cursos/eliminar/{id}")
    public String eliminarCurso(@PathVariable String id,
                              HttpSession session,
                              RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        try {
            cursoRepository.deleteById(id);
            redirect.addFlashAttribute("success", "Curso eliminado exitosamente");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al eliminar curso: " + e.getMessage());
        }

        return "redirect:/admin/cursos";
    }

    @GetMapping("/horarios")
    public String gestionHorarios(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        List<Horario> horarios = horarioRepository.findAll();
        List<Curso> cursos = cursoRepository.findAll();

        model.addAttribute("horarios", horarios);
        model.addAttribute("cursos", cursos);
        model.addAttribute("diasSemana", Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"));

        return "horarios";
    }

    @PostMapping("/horarios/guardar")
    public String guardarHorario(@RequestParam(required = false) String id,
                               @RequestParam String cursoId,
                               @RequestParam String diaSemana,
                               @RequestParam String horaInicio,
                               @RequestParam String horaFin,
                               HttpSession session,
                               RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        // Validación de campos requeridos
        if(cursoId.isEmpty() || diaSemana.isEmpty() || horaInicio.isEmpty() || horaFin.isEmpty()) {
            redirect.addFlashAttribute("error", "Todos los campos son obligatorios");
            return "redirect:/admin/horarios";
        }

        try {
            // Convertir y validar horas
            LocalTime horaInicioTime;
            LocalTime horaFinTime;
            
            try {
                horaInicioTime = LocalTime.parse(horaInicio);
                horaFinTime = LocalTime.parse(horaFin);
            } catch (Exception e) {
                redirect.addFlashAttribute("error", "Formato de hora inválido. Use HH:mm (ej: 08:30)");
                return "redirect:/admin/horarios";
            }

            // Validar que la hora de fin sea posterior a la de inicio
            if(horaFinTime.isBefore(horaInicioTime) || horaFinTime.equals(horaInicioTime)) {
                redirect.addFlashAttribute("error", "La hora de fin debe ser posterior a la hora de inicio");
                return "redirect:/admin/horarios";
            }

            Horario horario;
            if (id != null && !id.isEmpty()) {
                horario = horarioRepository.findById(id).orElse(new Horario());
            } else {
                horario = new Horario();
                horario.setId(UUID.randomUUID().toString());
            }

            horario.setCursoId(cursoId);
            horario.setDiaSemana(diaSemana);
            horario.setHoraInicio(horaInicioTime);
            horario.setHoraFin(horaFinTime);

            horarioRepository.save(horario);
            redirect.addFlashAttribute("success", "Horario guardado exitosamente");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al guardar horario: " + e.getMessage());
        }

        return "redirect:/admin/horarios";
    }

    @GetMapping("/horarios/eliminar/{id}")
    public String eliminarHorario(@PathVariable String id,
                                HttpSession session,
                                RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        try {
            horarioRepository.deleteById(id);
            redirect.addFlashAttribute("success", "Horario eliminado exitosamente");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al eliminar horario: " + e.getMessage());
        }

        return "redirect:/admin/horarios";
    }

    @GetMapping("/reportes")
    public String mostrarReportes(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null || !usuario.getRol().equals("ADMIN")) {
            return "redirect:/";
        }

        // Obtener estadísticas básicas
        long totalUsuarios = usuarioRepository.count();
        long totalCursos = cursoRepository.count();
        long totalHorarios = horarioRepository.count();
        
        // Obtener conteo de usuarios por rol
        Map<String, Long> usuariosPorRol = new HashMap<>();
        usuariosPorRol.put("ADMIN", usuarioRepository.countByRol("ADMIN"));
        usuariosPorRol.put("INSTRUCTOR", usuarioRepository.countByRol("INSTRUCTOR"));
        usuariosPorRol.put("ESTUDIANTE", usuarioRepository.countByRol("ESTUDIANTE"));
        usuariosPorRol.put("RECEPCIONISTA", usuarioRepository.countByRol("RECEPCIONISTA"));
        usuariosPorRol.put("SUPERVISOR", usuarioRepository.countByRol("SUPERVISOR"));
        
        // Obtener últimos cursos
        List<Curso> ultimosCursos = cursoRepository.findTop5ByOrderByFechaCreacionDesc();
        
        // Preparar datos de cursos con información de instructor
        List<Map<String, Object>> cursosConInfo = ultimosCursos.stream().map(curso -> {
            Map<String, Object> cursoInfo = new HashMap<>();
            cursoInfo.put("curso", curso);
            Usuario instructor = usuarioRepository.findById(curso.getInstructorId()).orElse(null);
            cursoInfo.put("instructorNombre", instructor != null ? instructor.getNombre() : "No asignado");
            cursoInfo.put("totalHorarios", horarioRepository.countByCursoId(curso.getId()));
            return cursoInfo;
        }).collect(Collectors.toList());

        // Agregar al modelo
        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalCursos", totalCursos);
        model.addAttribute("totalHorarios", totalHorarios);
        model.addAttribute("usuariosPorRol", usuariosPorRol);
        model.addAttribute("ultimosCursos", cursosConInfo);
        model.addAttribute("usuario", usuario);
        
        return "reportes";
    }
}