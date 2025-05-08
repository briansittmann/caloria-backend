package com.caloria.repository;

import com.caloria.model.Credencial;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredencialRepository extends MongoRepository<Credencial, String> {
    Credencial findByEmail(String email);
    boolean existsByEmail(String email);
    Credencial findByUsuarioId(String usuarioId);
}