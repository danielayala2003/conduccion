package com.Parcial.app.repository;

import com.Parcial.app.model.Asistencia;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AsistenciaRepository extends MongoRepository<Asistencia, String> {
    List<Asistencia> findByInstructorId(String instructorId);
}
