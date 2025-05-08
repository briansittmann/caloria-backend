package com.caloria.repository;

import com.caloria.model.CatalogoAlimento;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface CatalogoAlimentoRepository extends MongoRepository<CatalogoAlimento, String> {
    Optional<CatalogoAlimento> findByNombreIgnoreCase(String nombre);
}