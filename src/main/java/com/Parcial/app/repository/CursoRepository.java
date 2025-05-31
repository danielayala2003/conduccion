package com.Parcial.app.repository;

import com.Parcial.app.model.Curso;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoRepository extends MongoRepository<Curso, String> {
    List<Curso> findByInstructorId(String instructorId);
    List<Curso> findTop5ByOrderByFechaCreacionDesc();
    long count();
}