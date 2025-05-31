package com.Parcial.app.repository;

import com.Parcial.app.model.Clase;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ClaseRepository extends MongoRepository<Clase, String> {
    List<Clase> findByInstructorId(String instructorId);
}
