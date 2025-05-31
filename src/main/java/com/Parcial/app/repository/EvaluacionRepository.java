package com.Parcial.app.repository;

import com.Parcial.app.model.Evaluacion;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EvaluacionRepository extends MongoRepository<Evaluacion, String> {
    List<Evaluacion> findByInstructorId(String instructorId);
}
