package com.Parcial.app.repository;

import com.Parcial.app.model.Horario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioRepository extends MongoRepository<Horario, String> {
    List<Horario> findByCursoId(String cursoId);
    long countByCursoId(String cursoId);
    long count();
}