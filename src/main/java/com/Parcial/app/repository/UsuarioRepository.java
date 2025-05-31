package com.Parcial.app.repository;

import com.Parcial.app.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Usuario findByEmail(String email);
    long countByRol(String rol);
    List<Usuario> findAllByEmail(String email);
    List<Usuario> findByRol(String rol);
    long count();

    // ➕ Agregados: buscar por múltiples IDs, y por IDs + Rol
    List<Usuario> findByIdIn(List<String> ids);
    List<Usuario> findByIdInAndRol(List<String> ids, String rol);
}
