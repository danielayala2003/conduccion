package com.Parcial.app.repository;

import com.Parcial.app.model.Inscripcion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscripcionRepository extends MongoRepository<Inscripcion, String> {
    List<Inscripcion> findByEstudianteId(String estudianteId);
    boolean existsByEstudianteIdAndCursoId(String estudianteId, String cursoId);
    long countByCursoId(String cursoId);
    Inscripcion findByEstudianteIdAndCursoId(String estudianteId, String cursoId);

    // ➕ Agregado: buscar inscripciones por múltiples cursoIds
    List<Inscripcion> findByCursoIdIn(List<String> cursoIds);
}
