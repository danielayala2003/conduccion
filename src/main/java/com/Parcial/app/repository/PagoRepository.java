package com.Parcial.app.repository;

import com.Parcial.app.model.Pago;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface PagoRepository extends MongoRepository<Pago, String> {
    List<Pago> findByEstudianteId(String estudianteId);
}
