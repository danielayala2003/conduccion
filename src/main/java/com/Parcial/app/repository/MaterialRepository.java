package com.Parcial.app.repository;

import com.Parcial.app.model.Material;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MaterialRepository extends MongoRepository<Material, String> {
    List<Material> findByInstructorId(String instructorId);
}
